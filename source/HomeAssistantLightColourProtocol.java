package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

final class HomeAssistantLightColourProtocol {
    private HomeAssistantLightColourProtocol() { }

    static JSONObject serviceBody(List<String> entityIds, String colour) {
        JSONArray entities = new JSONArray();
        for (String entityId : entityIds) {
            entities.put(entityId);
        }
        return new JSONObject()
                .put("entity_id", entities)
                .put("color_name", colour);
    }
}
