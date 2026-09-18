package com.boop.shieldoverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HomeAssistantRepository {
    public interface CommandPort {
        void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback);
    }

    public interface StateChangePort {
        interface Listener { void onStateChanged(String entityId, String state); }
        interface Subscription { void cancel(); }
        interface Callback { void onResult(Subscription subscription, String error); }
        void subscribe(Listener listener, Callback callback);
    }

    public interface AreasCallback { void onResult(List<AreaInfo> areas, String error); }
    public interface BinaryActionCallback {
        void onResult(boolean success, EntityCard card, String error);
        default void onObservedState(EntityCard card) { }
        default void onAccepted(EntityCard requestedState) { }
    }
    public interface DashboardCallback { void onResult(DashboardSnapshot snapshot, String error); }

    private final CommandPort commandPort;
    private final StateChangePort stateChangePort;

    public HomeAssistantRepository(CommandPort commandPort) { this(commandPort, null); }

    public HomeAssistantRepository(CommandPort commandPort, StateChangePort stateChangePort) {
        if (commandPort == null) throw new IllegalArgumentException("Home Assistant command port is required");
        this.commandPort = commandPort;
        this.stateChangePort = stateChangePort;
    }

    public void loadAreas(AreasCallback callback) {
        if (callback == null) throw new IllegalArgumentException("areas callback is required");
        commandPort.send("config/area_registry/list", new JSONObject(), (success, result, error) -> {
            if (!success) {
                callback.onResult(null, plainError(error, "I couldn't load your rooms from Home Assistant."));
                return;
            }
            if (!(result instanceof JSONArray)) {
                callback.onResult(null, "Home Assistant returned an unreadable room list.");
                return;
            }
            JSONArray array = (JSONArray) result;
            List<AreaInfo> areas = new ArrayList<>();
            for (int index = 0; index < array.length(); index++) {
                Object item = array.opt(index);
                if (!(item instanceof JSONObject)) continue;
                JSONObject object = (JSONObject) item;
                String id = clean(object.optString("area_id", null));
                String name = clean(object.optString("name", null));
                if (id != null && name != null) areas.add(new AreaInfo(id, name));
            }
            areas.sort(Comparator.comparing(AreaInfo::name, String.CASE_INSENSITIVE_ORDER));
            if (areas.isEmpty()) {
                callback.onResult(null, "I couldn't find any rooms in Home Assistant.");
                return;
            }
            callback.onResult(areas, null);
        });
    }

    public void loadDashboard(AreaInfo room, DashboardCallback callback) {
        if (room == null) throw new IllegalArgumentException("room is required");
        if (callback == null) throw new IllegalArgumentException("dashboard callback is required");

        final JSONObject targetBody;
        try {
            targetBody = new JSONObject()
                    .put("target", new JSONObject().put("area_id", room.id()))
                    .put("expand_group", false)
                    .put("primary_entities_only", false);
        } catch (JSONException jsonError) {
            callback.onResult(null, "I couldn't prepare that room request.");
            return;
        }

        commandPort.send("extract_from_target", targetBody, (success, result, error) -> {
            if (!success) {
                callback.onResult(null, plainError(error, "I couldn't find the things in that room."));
                return;
            }
            if (!(result instanceof JSONObject)) {
                callback.onResult(null, "Home Assistant returned an unreadable room membership list.");
                return;
            }
            Set<String> referenced = referencedEntities((JSONObject) result);
            loadDashboardDevices(room, referenced, callback);
        });
    }

    private void loadDashboardDevices(AreaInfo room, Set<String> referenced, DashboardCallback callback) {
        commandPort.send("config/device_registry/list", new JSONObject(), (success, result, error) -> {
            if (!success) {
                callback.onResult(null, plainError(error, "I couldn't read the devices in that room."));
                return;
            }
            if (!(result instanceof JSONArray)) {
                callback.onResult(null, "Home Assistant returned an unreadable device list.");
                return;
            }
            Map<String, DeviceInfo> devices = new HashMap<>();
            JSONArray array = (JSONArray) result;
            for (int index = 0; index < array.length(); index++) {
                Object item = array.opt(index);
                if (!(item instanceof JSONObject)) continue;
                JSONObject object = (JSONObject) item;
                String id = clean(object.optString("id", null));
                if (id == null) continue;
                String areaId = clean(object.optString("area_id", null));
                String name = clean(object.optString("name_by_user", null));
                if (name == null) name = clean(object.optString("name", null));
                devices.put(id, new DeviceInfo(id, areaId, name));
            }
            loadDashboardRegistry(room, referenced, devices, callback);
        });
    }

    private void loadDashboardRegistry(
            AreaInfo room,
            Set<String> referenced,
            Map<String, DeviceInfo> devices,
            DashboardCallback callback) {
        commandPort.send("config/entity_registry/list_for_display", new JSONObject(), (success, result, error) -> {
            if (!success) {
                callback.onResult(null, plainError(error, "I couldn't read the room controls from Home Assistant."));
                return;
            }
            if (!(result instanceof JSONObject)) {
                callback.onResult(null, "Home Assistant returned an unreadable control list.");
                return;
            }

            JSONObject registryResult = (JSONObject) result;
            JSONArray entities = registryResult.optJSONArray("entities");
            if (entities == null) {
                callback.onResult(null, "Home Assistant returned an unreadable control list.");
                return;
            }

            Object categories = registryResult.opt("entity_categories");
            Map<String, RegistryEntry> registry = new HashMap<>();
            for (int index = 0; index < entities.length(); index++) {
                Object item = entities.opt(index);
                if (!(item instanceof JSONObject)) continue;
                JSONObject object = (JSONObject) item;
                String entityId = clean(object.optString("ei", null));
                if (entityId == null || !referenced.contains(entityId)) continue;

                String deviceId = clean(object.optString("di", null));
                DeviceInfo device = deviceId == null ? null : devices.get(deviceId);
                String areaId = clean(object.optString("ai", null));
                if (areaId == null && device != null) areaId = device.areaId;
                // Fail closed. Target expansion can nominate members, but Home only exposes
                // entities whose direct/device-inherited area can still be confirmed here.
                if (areaId == null || !room.id().equals(areaId)) continue;

                String name = clean(object.optString("en", null));
                boolean hidden = object.optBoolean("hb", false);
                String category = HaEntityCategory.resolve(object.opt("ec"), categories);
                registry.put(entityId, new RegistryEntry(
                        entityId,
                        areaId,
                        name,
                        hidden,
                        category,
                        deviceId,
                        device == null ? null : device.name));
            }
            loadDashboardStates(room, referenced, registry, callback);
        });
    }

    private void loadDashboardStates(
            AreaInfo room,
            Set<String> referenced,
            Map<String, RegistryEntry> registry,
            DashboardCallback callback) {
        commandPort.send("get_states", new JSONObject(), (success, result, error) -> {
            if (!success) {
                callback.onResult(null, plainError(error, "I couldn't read the current room state."));
                return;
            }
            if (!(result instanceof JSONArray)) {
                callback.onResult(null, "Home Assistant returned an unreadable room state.");
                return;
            }

            JSONArray stateArray = (JSONArray) result;
            Map<String, EntityState> states = new HashMap<>();
            for (int index = 0; index < stateArray.length(); index++) {
                Object item = stateArray.opt(index);
                if (!(item instanceof JSONObject)) continue;
                JSONObject object = (JSONObject) item;
                String entityId = clean(object.optString("entity_id", null));
                String state = clean(object.optString("state", null));
                if (entityId == null || state == null || !referenced.contains(entityId)) continue;
                JSONObject attributes = object.optJSONObject("attributes");
                String friendlyName = attributes == null
                        ? null
                        : clean(attributes.optString("friendly_name", null));
                states.put(entityId, new EntityState(entityId, state, friendlyName));
            }

            List<EntityCard> candidates = new ArrayList<>();
            for (RegistryEntry entry : registry.values()) {
                EntityState state = states.get(entry.entityId);
                if (state == null) continue;
                String displayName = entry.name != null ? entry.name : state.friendlyName();
                if (displayName == null) continue;
                EntityCard card = new EntityCard(
                        entry.entityId,
                        entry.areaId,
                        displayName,
                        state.state(),
                        entry.hidden,
                        entry.category,
                        entry.deviceId,
                        entry.deviceName);
                if (isDashboardControl(card)) candidates.add(card);
            }

            List<EntityCard> cards = RoomDeviceControls.collapseToDevices(candidates);
            cards.sort(Comparator.comparing(EntityCard::displayName, String.CASE_INSENSITIVE_ORDER));
            callback.onResult(new DashboardSnapshot(room, cards), null);
        });
    }

    public void toggleBinary(EntityCard card, BinaryActionCallback callback) {
        if (card == null) throw new IllegalArgumentException("entity card is required");
        if (callback == null) throw new IllegalArgumentException("binary action callback is required");
        if (!isSupportedBinary(card)) {
            callback.onResult(false, null, "That control isn't a simple on/off thing.");
            return;
        }

        String expectedState = "off".equals(card.state()) ? "on" : "off";
        String service = "off".equals(card.state()) ? "turn_on" : "turn_off";
        final JSONObject body;
        try {
            JSONObject target = new JSONObject().put("entity_id", card.entityId());
            body = new JSONObject()
                    .put("domain", card.domain())
                    .put("service", service)
                    .put("target", target);
        } catch (JSONException jsonError) {
            callback.onResult(false, null, "I couldn't prepare that Home Assistant command.");
            return;
        }

        // The session already owns a live state_changed subscription. Home Assistant's
        // call_service result is the action acknowledgement, so a second per-click
        // subscription only adds latency and can leave slow-reporting devices stuck busy.
        try {
            commandPort.send("call_service", body, (success, result, error) -> {
                if (!success) {
                    callback.onResult(false, null, plainError(error, "Home Assistant didn't do that."));
                    return;
                }
                callback.onAccepted(card.withState(expectedState));
                callback.onResult(true, null, null);
            });
        } catch (RuntimeException couldNotSend) {
            callback.onResult(false, null, "Home Assistant didn't do that.");
        }
    }

    private static Set<String> referencedEntities(JSONObject result) {
        Set<String> referenced = new LinkedHashSet<>();
        JSONArray entities = result.optJSONArray("referenced_entities");
        if (entities == null) return referenced;
        for (int index = 0; index < entities.length(); index++) {
            String entityId = clean(entities.optString(index, null));
            if (entityId != null) referenced.add(entityId);
        }
        return referenced;
    }

    private static boolean isDashboardControl(EntityCard card) {
        return RoomDeviceControls.isActionable(card);
    }

    private static boolean isSupportedBinary(EntityCard card) {
        String domain = card.domain();
        boolean supportedDomain = "light".equals(domain)
                || "switch".equals(domain)
                || "fan".equals(domain)
                || "input_boolean".equals(domain);
        return supportedDomain && ("on".equals(card.state()) || "off".equals(card.state()));
    }

    private static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String plainError(String value, String fallback) {
        String clean = clean(value);
        return clean == null ? fallback : clean;
    }

    private static final class DeviceInfo {
        final String id;
        final String areaId;
        final String name;
        DeviceInfo(String id, String areaId, String name) {
            this.id = id;
            this.areaId = areaId;
            this.name = name;
        }
    }

    private static final class RegistryEntry {
        final String entityId;
        final String areaId;
        final String name;
        final boolean hidden;
        final String category;
        final String deviceId;
        final String deviceName;

        RegistryEntry(
                String entityId,
                String areaId,
                String name,
                boolean hidden,
                String category,
                String deviceId,
                String deviceName) {
            this.entityId = entityId;
            this.areaId = areaId;
            this.name = name;
            this.hidden = hidden;
            this.category = category;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
        }
    }
}
