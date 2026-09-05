package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

public final class HomeAssistantLightColourSelectorTest {
    @Test public void selectsOnlyOnColourCapableLightsInArea() throws Exception {
        JSONArray areaEntities = new JSONArray()
                .put("light.govee_strip")
                .put("light.hue_lamp")
                .put("light.off_colour_bulb")
                .put("light.dimmable_only")
                .put("switch.not_a_light");

        JSONArray states = new JSONArray()
                .put(light("light.govee_strip", "on", "rgb"))
                .put(light("light.hue_lamp", "on", "hs"))
                .put(light("light.off_colour_bulb", "off", "rgb"))
                .put(light("light.dimmable_only", "on", "brightness"))
                .put(new JSONObject()
                        .put("entity_id", "light.other_room")
                        .put("state", "on")
                        .put("attributes", new JSONObject()
                                .put("supported_color_modes", new JSONArray().put("rgb"))));

        List<String> selected = HomeAssistantLightColourSelector.select(areaEntities, states);

        assertEquals(List.of("light.govee_strip", "light.hue_lamp"), selected);
    }

    @Test public void acceptsHomeAssistantColourModesThatCanRenderNamedColours() throws Exception {
        JSONArray areaEntities = new JSONArray()
                .put("light.hs")
                .put("light.xy")
                .put("light.rgb")
                .put("light.rgbw")
                .put("light.rgbww")
                .put("light.temp_only")
                .put("light.white_only");

        JSONArray states = new JSONArray()
                .put(light("light.hs", "on", "hs"))
                .put(light("light.xy", "on", "xy"))
                .put(light("light.rgb", "on", "rgb"))
                .put(light("light.rgbw", "on", "rgbw"))
                .put(light("light.rgbww", "on", "rgbww"))
                .put(light("light.temp_only", "on", "color_temp"))
                .put(light("light.white_only", "on", "white"));

        assertEquals(
                List.of("light.hs", "light.rgb", "light.rgbw", "light.rgbww", "light.xy"),
                HomeAssistantLightColourSelector.select(areaEntities, states));
    }

    private static JSONObject light(String entityId, String state, String colourMode)
            throws Exception {
        return new JSONObject()
                .put("entity_id", entityId)
                .put("state", state)
                .put("attributes", new JSONObject()
                        .put("supported_color_modes", new JSONArray().put(colourMode)));
    }
}
