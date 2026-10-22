package com.boop.alpha1;

final class HomeAssistantAssistantReply {
    private final String speech;
    private final String conversationId;

    HomeAssistantAssistantReply(String speech, String conversationId) {
        this.speech = speech == null ? "" : speech;
        this.conversationId = conversationId == null ? "" : conversationId;
    }

    String speech() { return speech; }
    String conversationId() { return conversationId; }
}
