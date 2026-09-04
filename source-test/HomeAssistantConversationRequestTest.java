package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
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
}
