package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

final class HomeAssistantDeviceSetupProtocol {
    private static final String APP_ID = "com.boop.alpha1";
    private static final String APP_NAME = "BOOP";
    private static final String APP_VERSION = "0.3.1-alpha3";
    private static final String DEVICE_NAME = "BOOP Wall";

    private HomeAssistantDeviceSetupProtocol() { }

    static JSONObject registrationBody(
            String registrationId,
            String manufacturer,
            String model,
            String osVersion) throws Exception {
        return new JSONObject()
                .put("app_id", APP_ID)
                .put("app_name", APP_NAME)
                .put("app_version", APP_VERSION)
                .put("device_name", DEVICE_NAME)
                .put("device_id", registrationId)
                .put("manufacturer", manufacturer)
                .put("model", model)
                .put("os_name", "Android")
                .put("os_version", osVersion)
                .put("supports_encryption", false)
                .put("app_data", new JSONObject());
    }

    static String parseWebhookId(JSONObject response) {
        return requiredString(response, "webhook_id");
    }

    static JSONObject getConfigWebhookBody() throws Exception {
        return new JSONObject().put("type", "get_config");
    }

    static String parseHaDeviceId(JSONObject response) {
        return requiredString(response, "hass_device_id");
    }

    static String websocketUrl(String baseUrl) {
        String clean = HomeAssistantAuthUrls.trim(baseUrl);
        if (clean.startsWith("https://")) {
            return "wss://" + clean.substring("https://".length()) + "/api/websocket";
        }
        if (clean.startsWith("http://")) {
            return "ws://" + clean.substring("http://".length()) + "/api/websocket";
        }
        throw new IllegalArgumentException("Unsupported Home Assistant URL");
    }

    static JSONObject websocketAuthMessage(String accessToken) throws Exception {
        return new JSONObject()
                .put("type", "auth")
                .put("access_token", accessToken);
    }

    static JSONObject areaListMessage(int id) throws Exception {
        return new JSONObject()
                .put("id", id)
                .put("type", "config/area_registry/list");
    }

    static String findAreaId(JSONArray areas, String areaName) {
        if (areas == null || areaName == null) {
            return null;
        }
        String wanted = areaName.trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < areas.length(); i++) {
            JSONObject area = areas.optJSONObject(i);
            if (area == null) {
                continue;
            }
            String name = area.optString("name", "").trim().toLowerCase(Locale.ROOT);
            if (wanted.equals(name)) {
                String id = area.optString("area_id", "").trim();
                return id.isEmpty() ? null : id;
            }
        }
        return null;
    }

    static JSONObject deviceAreaUpdateMessage(
            int id, String deviceId, String areaId) throws Exception {
        return new JSONObject()
                .put("id", id)
                .put("type", "config/device_registry/update")
                .put("device_id", deviceId)
                .put("area_id", areaId);
    }

    private static String requiredString(JSONObject object, String key) {
        String value = object == null ? "" : object.optString(key, "").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing Home Assistant " + key);
        }
        return value;
    }
}
