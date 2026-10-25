package com.boop.alpha1;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class HomeAssistantEntityDiscoveryProtocolTest {
    @Test public void buildsDocumentedReadOnlyCommands() throws Exception {
        assertEquals("config/entity_registry/list_for_display",
                HomeAssistantEntityDiscoveryProtocol.registryCommand(1).getString("type"));
        assertEquals("homeassistant/expose_entity/list",
                HomeAssistantEntityDiscoveryProtocol.exposureCommand(2).getString("type"));
    }

    @Test public void onlyExplicitConversationExposureIsSafe() throws Exception {
        JSONObject result = new JSONObject("{\"exposed_entities\":{" +
                "\"light.yes\":{\"conversation\":true}," +
                "\"light.no\":{\"conversation\":false}}}");
        assertTrue(HomeAssistantEntityDiscoveryProtocol.isExplicitlyExposed(result, "light.yes"));
        assertFalse(HomeAssistantEntityDiscoveryProtocol.isExplicitlyExposed(result, "light.no"));
        assertFalse(HomeAssistantEntityDiscoveryProtocol.isExplicitlyExposed(result, "light.default"));
    }

    @Test public void acceptsEnabledVisibleRegistryEntriesOnly() throws Exception {
        JSONObject visible = new JSONObject("{\"ei\":\"light.good\"}");
        JSONObject hidden = new JSONObject("{\"ei\":\"light.hidden\",\"hb\":true}");
        assertTrue(HomeAssistantEntityDiscoveryProtocol.isVisible(visible));
        assertFalse(HomeAssistantEntityDiscoveryProtocol.isVisible(hidden));
    }
}
