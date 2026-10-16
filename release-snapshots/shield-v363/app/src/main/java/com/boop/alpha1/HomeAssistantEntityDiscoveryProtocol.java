package com.boop.alpha1;

import org.json.JSONObject;

final class HomeAssistantEntityDiscoveryProtocol {
    static JSONObject registryCommand(int id) throws Exception {
        return new JSONObject().put("id", id)
                .put("type", "config/entity_registry/list_for_display");
    }

    static JSONObject exposureCommand(int id) throws Exception {
        return new JSONObject().put("id", id)
                .put("type", "homeassistant/expose_entity/list");
    }

    static boolean isExplicitlyExposed(JSONObject result, String entityId) {
        if (result == null) return false;
        JSONObject all = result.optJSONObject("exposed_entities");
        JSONObject entity = all == null ? null : all.optJSONObject(entityId);
        return entity != null && entity.optBoolean("conversation", false);
    }

    static boolean isVisible(JSONObject entry) {
        return entry != null && !entry.optBoolean("hb", false)
                && !entry.optString("ei", "").isEmpty();
    }
}
