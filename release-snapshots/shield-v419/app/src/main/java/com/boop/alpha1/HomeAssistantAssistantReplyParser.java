package com.boop.alpha1;

import org.json.JSONObject;

final class HomeAssistantAssistantReplyParser {
    private HomeAssistantAssistantReplyParser() { }

    static HomeAssistantAssistantReply parse(String json) {
        try {
            HomeAssistantResponse response = HomeAssistantResponseParser.parse(json);
            JSONObject root = new JSONObject(json);
            return new HomeAssistantAssistantReply(
                    response.speech(),
                    root.optString("conversation_id", ""));
        } catch (Exception e) {
            return new HomeAssistantAssistantReply("", "");
        }
    }
}
