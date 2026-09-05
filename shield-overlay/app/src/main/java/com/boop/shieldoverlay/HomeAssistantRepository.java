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
