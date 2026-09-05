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

    public interface AreasCallback {
        void onResult(List<AreaInfo> areas, String error);
    }

    public interface BinaryActionCallback {
        void onResult(boolean success, EntityCard card, String error);
    }

    public interface DashboardCallback {
        void onResult(DashboardSnapshot snapshot, String error);
    }

    private final CommandPort commandPort;

    public HomeAssistantRepository(CommandPort commandPort) {
        if (commandPort == null) {
            throw new IllegalArgumentException("Home Assistant command port is required");
        }
        this.commandPort = commandPort;
    }

    public void loadAreas(AreasCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("areas callback is required");
        }

        commandPort.send(
                "config/area_registry/list",
                new JSONObject(),
                (success, result, error) -> {
                    if (!success) {
                        callback.onResult(
                                null,
                                plainError(error, "I couldn't load your rooms from Home Assistant."));
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
                        if (!(item instanceof JSONObject)) {
                            continue;
                        }
                        JSONObject object = (JSONObject) item;
                        String id = clean(object.optString("area_id", null));
                        String name = clean(object.optString("name", null));
                        if (id == null || name == null) {
                            continue;
                        }
                        areas.add(new AreaInfo(id, name));
                    }

                    areas.sort(Comparator.comparing(
                            AreaInfo::name,
                            String.CASE_INSENSITIVE_ORDER));
                    if (areas.isEmpty()) {
                        callback.onResult(null, "I couldn't find any rooms in Home Assistant.");
                        return;
                    }
                    callback.onResult(areas, null);
                });
    }

    public void loadDashboard(AreaInfo room, DashboardCallback callback) {
        if (room == null) {
            throw new IllegalArgumentException("room is required");
        }
        if (callback == null) {
            throw new IllegalArgumentException("dashboard callback is required");
        }

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
            loadDashboardRegistry(room, referenced, callback);
        });
    }

    private void loadDashboardRegistry(
            AreaInfo room,
            Set<String> referenced,
            DashboardCallback callback) {
        commandPort.send(
                "config/entity_registry/list_for_display",
                new JSONObject(),
                (success, result, error) -> {
                    if (!success) {
                        callback.onResult(
                                null,
                                plainError(error, "I couldn't read the room controls from Home Assistant."));
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

                    JSONArray categories = registryResult.optJSONArray("entity_categories");
                    Map<String, RegistryEntry> registry = new HashMap<>();
                    for (int index = 0; index < entities.length(); index++) {
                        Object item = entities.opt(index);
                        if (!(item instanceof JSONObject)) {
                            continue;
                        }
                        JSONObject object = (JSONObject) item;
                        String entityId = clean(object.optString("ei", null));
                        if (entityId == null || !referenced.contains(entityId)) {
                            continue;
                        }

                        String areaId = clean(object.optString("ai", null));
                        String name = clean(object.optString("en", null));
                        boolean hidden = object.optBoolean("hb", false);
                        String category = entityCategory(object.opt("ec"), categories);
                        registry.put(
                                entityId,
                                new RegistryEntry(entityId, areaId, name, hidden, category));
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
                if (!(item instanceof JSONObject)) {
                    continue;
                }
                JSONObject object = (JSONObject) item;
                String entityId = clean(object.optString("entity_id", null));
                String state = clean(object.optString("state", null));
                if (entityId == null || state == null || !referenced.contains(entityId)) {
                    continue;
                }
                JSONObject attributes = object.optJSONObject("attributes");
                String friendlyName = attributes == null
                        ? null
                        : clean(attributes.optString("friendly_name", null));
                states.put(entityId, new EntityState(entityId, state, friendlyName));
            }

            List<EntityCard> cards = new ArrayList<>();
            for (RegistryEntry entry : registry.values()) {
                EntityState state = states.get(entry.entityId);
                if (state == null) {
                    continue;
                }
                String displayName = entry.name != null ? entry.name : state.friendlyName();
                if (displayName == null) {
                    continue;
                }
                EntityCard card = new EntityCard(
                        entry.entityId,
                        entry.areaId,
                        displayName,
                        state.state(),
                        entry.hidden,
                        entry.category);
                if (isDashboardControl(card)) {
                    cards.add(card);
                }
            }

            cards.sort(Comparator.comparing(
                    EntityCard::displayName,
                    String.CASE_INSENSITIVE_ORDER));
            callback.onResult(new DashboardSnapshot(room, cards), null);
        });
    }

    public void toggleBinary(EntityCard card, BinaryActionCallback callback) {
        if (card == null) {
            throw new IllegalArgumentException("entity card is required");
        }
        if (callback == null) {
            throw new IllegalArgumentException("binary action callback is required");
        }
        if (!isSupportedBinary(card)) {
            callback.onResult(false, null, "That control isn't a simple on/off thing.");
            return;
        }

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

        commandPort.send("call_service", body, (success, result, error) -> {
            if (!success) {
                callback.onResult(false, null, plainError(error, "Home Assistant didn't do that."));
                return;
            }
            refreshExactState(card, callback);
        });
    }

    private void refreshExactState(EntityCard original, BinaryActionCallback callback) {
        commandPort.send("get_states", new JSONObject(), (success, result, error) -> {
            if (!success) {
                callback.onResult(
                        false,
                        null,
                        plainError(error, "Home Assistant changed it, but I couldn't confirm the new state."));
                return;
            }
            if (!(result instanceof JSONArray)) {
                callback.onResult(false, null, "Home Assistant changed it, but I couldn't confirm the new state.");
                return;
            }

            JSONArray states = (JSONArray) result;
            for (int index = 0; index < states.length(); index++) {
                Object item = states.opt(index);
                if (!(item instanceof JSONObject)) {
                    continue;
                }
                JSONObject state = (JSONObject) item;
                if (!original.entityId().equals(clean(state.optString("entity_id", null)))) {
                    continue;
                }
                String newState = clean(state.optString("state", null));
                if (!"on".equals(newState) && !"off".equals(newState)) {
                    callback.onResult(false, null, "Home Assistant changed it, but I couldn't confirm the new state.");
                    return;
                }
                callback.onResult(true, original.withState(newState), null);
                return;
            }

            callback.onResult(false, null, "Home Assistant changed it, but I couldn't find that control afterwards.");
        });
    }

    private static Set<String> referencedEntities(JSONObject result) {
        Set<String> referenced = new LinkedHashSet<>();
        JSONArray entities = result.optJSONArray("referenced_entities");
        if (entities == null) {
            return referenced;
        }
        for (int index = 0; index < entities.length(); index++) {
            String entityId = clean(entities.optString(index, null));
            if (entityId != null) {
                referenced.add(entityId);
            }
        }
        return referenced;
    }

    private static String entityCategory(Object categoryRef, JSONArray categories) {
        if (categoryRef instanceof Number && categories != null) {
            int index = ((Number) categoryRef).intValue();
            if (index >= 0 && index < categories.length()) {
                return clean(categories.optString(index, null));
            }
            return null;
        }
        return categoryRef instanceof String ? clean((String) categoryRef) : null;
    }

    private static boolean isDashboardControl(EntityCard card) {
        if (card.hidden()) {
            return false;
        }
        String category = clean(card.entityCategory());
        if ("config".equalsIgnoreCase(category) || "diagnostic".equalsIgnoreCase(category)) {
            return false;
        }
        return isSupportedBinary(card);
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
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String plainError(String value, String fallback) {
        String clean = clean(value);
        return clean == null ? fallback : clean;
    }

    private static final class RegistryEntry {
        final String entityId;
        final String areaId;
        final String name;
        final boolean hidden;
        final String category;

        RegistryEntry(
                String entityId,
                String areaId,
                String name,
                boolean hidden,
                String category) {
            this.entityId = entityId;
            this.areaId = areaId;
            this.name = name;
            this.hidden = hidden;
            this.category = category;
        }
    }
}
