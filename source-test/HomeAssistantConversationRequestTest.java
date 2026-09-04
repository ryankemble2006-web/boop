package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.json.JSONObject;
import org.junit.Test;

public final class HomeAssistantConversationRequestTest {
    @Test public void preservesSwitchPhraseExactly() throws Exception {
        JSONObject body = HomeAssistantConversationRequest.build(
                "switch the fan on", "en-GB", "ha-device-123");
        assertEquals("switch the fan on", body.getString("text"));
        assertEquals("en-GB", body.getString("language"));
        assertEquals("ha-device-123", body.getString("device_id"));
    }

    @Test public void preservesArbitraryFutureDevicePhraseExactly() throws Exception {
        String spoken = "put the new sonoff thing on";
        JSONObject body = HomeAssistantConversationRequest.build(
                spoken, "en-GB", "ha-device-123");
        assertEquals(spoken, body.getString("text"));
    }

    @Test public void assistantRequestCarriesAgentAndConversation() throws Exception {
        JSONObject body = HomeAssistantConversationRequest.build(
                "why is the sky blue?", "en-GB", "ha-device-123",
                "conversation.openai_conversation", "thread-7");
        assertEquals("conversation.openai_conversation", body.getString("agent_id"));
        assertEquals("thread-7", body.getString("conversation_id"));
    }

    @Test public void assistantRequestOmitsEmptyConversationId() throws Exception {
        JSONObject body = HomeAssistantConversationRequest.build(
                "tell me a joke", "en-GB", "ha-device-123",
                "conversation.openai_conversation", "");
        assertFalse(body.has("conversation_id"));
    }
}
