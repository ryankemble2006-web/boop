package com.boop.alpha1;

import org.json.JSONObject;

final class HomeAssistantConversationRequest {
    private HomeAssistantConversationRequest() { }

    static JSONObject build(String text, String language, String deviceId) throws Exception {
        return new JSONObject()
                .put("text", text)
                .put("language", language)
                .put("device_id", deviceId);
    }
}
