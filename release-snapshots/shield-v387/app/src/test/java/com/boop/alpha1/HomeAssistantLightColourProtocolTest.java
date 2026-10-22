package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

public final class HomeAssistantLightColourProtocolTest {
    @Test public void serviceBodyTargetsSelectedLightsWithNamedColour() throws Exception {
        JSONObject body = HomeAssistantLightColourProtocol.serviceBody(
                List.of("light.govee_strip", "light.hue_lamp"),
                "purple");

        assertEquals("purple", body.getString("color_name"));
        JSONArray entities = body.getJSONArray("entity_id");
        assertEquals(2, entities.length());
        assertEquals("light.govee_strip", entities.getString(0));
        assertEquals("light.hue_lamp", entities.getString(1));
    }
}
