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

    static JSONObject build(
            String text,
            String language,
            String deviceId,
            String agentId,
            String conversationId) throws Exception {
        JSONObject body = build(text, language, deviceId);
        if (agentId != null && !agentId.isBlank()) {
            body.put("agent_id", agentId);
        }
        if (conversationId != null && !conversationId.isBlank()) {
            body.put("conversation_id", conversationId);
        }
        return body;
    }
}
