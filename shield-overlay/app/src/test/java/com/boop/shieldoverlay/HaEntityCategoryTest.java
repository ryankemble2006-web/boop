package com.boop.shieldoverlay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public final class HaEntityCategoryTest {
    @Test public void readsTheKeyedLookupActuallyReturnedByHa() throws Exception {
        JSONObject lookup = new JSONObject().put("0", "config").put("1", "diagnostic");
        assertEquals("config", HaEntityCategory.resolve(0, lookup));
        assertEquals("diagnostic", HaEntityCategory.resolve(1, lookup));
    }
    @Test public void supportsArrayAndLiteralCategoryResponses() throws Exception {
        JSONArray lookup = new JSONArray().put("config").put("diagnostic");
        assertEquals("diagnostic", HaEntityCategory.resolve(1, lookup));
        assertNull(HaEntityCategory.resolve(JSONObject.NULL, null));
        assertEquals("diagnostic", HaEntityCategory.resolve("diagnostic", null));
    }
    @Test public void unknownCategoryNeverBecomesAnUncategorisedDevice() {
        assertEquals("unknown", HaEntityCategory.resolve(9, null));
        assertEquals("unknown", HaEntityCategory.resolve(1, new JSONObject()));
    }
}
