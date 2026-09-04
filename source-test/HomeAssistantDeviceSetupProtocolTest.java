package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class HomeAssistantDeviceSetupProtocolTest {
    @Test public void websocketUrlUsesHaWebsocketEndpoint() {
        assertEquals("ws://homeassistant.local:8123/api/websocket",
                HomeAssistantDeviceSetupProtocol.websocketUrl(
                        "http://homeassistant.local:8123"));
        assertEquals("wss://ha.example.test/api/websocket",
                HomeAssistantDeviceSetupProtocol.websocketUrl(
                        "https://ha.example.test/"));
    }

    @Test public void registrationUsesBoopIdentity() throws Exception {
        JSONObject body = HomeAssistantDeviceSetupProtocol.registrationBody(
                "boop-registration-123", "Google", "Pixel 7 Pro", "16");
        assertEquals("com.boop.alpha1", body.getString("app_id"));
        assertEquals("BOOP", body.getString("app_name"));
        assertEquals("0.3.2-alpha3", body.getString("app_version"));
        assertEquals("BOOP Wall", body.getString("device_name"));
        assertEquals("boop-registration-123", body.getString("device_id"));
        assertEquals("Google", body.getString("manufacturer"));
        assertEquals("Pixel 7 Pro", body.getString("model"));
        assertEquals("Android", body.getString("os_name"));
        assertEquals("16", body.getString("os_version"));
        assertEquals(false, body.getBoolean("supports_encryption"));
    }

    @Test public void parsesRegistrationWebhookAndHaDeviceId() throws Exception {
        assertEquals("hook-123",
                HomeAssistantDeviceSetupProtocol.parseWebhookId(
                        new JSONObject().put("webhook_id", "hook-123")));
        assertEquals("ha-device-456",
                HomeAssistantDeviceSetupProtocol.parseHaDeviceId(
                        new JSONObject().put("hass_device_id", "ha-device-456")));
    }

    @Test public void getConfigWebhookUsesHomeAssistantCommand() throws Exception {
        assertEquals("get_config",
                HomeAssistantDeviceSetupProtocol.getConfigWebhookBody().getString("type"));
    }

    @Test public void websocketMessagesAreExact() throws Exception {
        JSONObject auth = HomeAssistantDeviceSetupProtocol.websocketAuthMessage("token-123");
        assertEquals("auth", auth.getString("type"));
        assertEquals("token-123", auth.getString("access_token"));

        JSONObject areas = HomeAssistantDeviceSetupProtocol.areaListMessage(1);
        assertEquals(1, areas.getInt("id"));
        assertEquals("config/area_registry/list", areas.getString("type"));
    }

    @Test public void resolvesLivingRoomFromRealHomeAssistantAreaShape() throws Exception {
        JSONArray areas = new JSONArray()
                .put(new JSONObject().put("area_id", "bedroom").put("name", "Bedroom"))
                .put(new JSONObject().put("area_id", "living_room").put("name", "Living Room"));
        assertEquals("living_room",
                HomeAssistantDeviceSetupProtocol.findAreaId(areas, "living room"));
        assertNull(HomeAssistantDeviceSetupProtocol.findAreaId(areas, "Kitchen"));
    }

    @Test public void deviceAreaUpdateTargetsOnlyBoopDevice() throws Exception {
        JSONObject msg = HomeAssistantDeviceSetupProtocol.deviceAreaUpdateMessage(
                2, "ha-device-456", "living_room");
        assertEquals(2, msg.getInt("id"));
        assertEquals("config/device_registry/update", msg.getString("type"));
        assertEquals("ha-device-456", msg.getString("device_id"));
        assertEquals("living_room", msg.getString("area_id"));
    }
}
