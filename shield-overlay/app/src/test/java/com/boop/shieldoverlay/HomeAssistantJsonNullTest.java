package com.boop.shieldoverlay;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class HomeAssistantJsonNullTest {
    @Test
    public void jsonNullDoesNotBecomeLiteralNullDeviceName() throws Exception {
        JSONObject object = new JSONObject()
                .put("name_by_user", JSONObject.NULL)
                .put("name", "Bedroom Fan");

        assertNull(HaJsonStrings.optional(object, "name_by_user"));
        assertEquals("Bedroom Fan", HaJsonStrings.optional(object, "name"));
    }
}
