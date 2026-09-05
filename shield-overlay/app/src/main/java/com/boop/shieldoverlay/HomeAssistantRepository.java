package com.boop.shieldoverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        JSONObject target = new JSONObject().put("entity_id", card.entityId());
        JSONObject body = new JSONObject()
                .put("domain", card.domain())
                .put("service", service)
                .put("target", target);

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
}
