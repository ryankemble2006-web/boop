package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class HomeAssistantResponseParserTest {
    @Test public void parsesActionDoneSuccess() throws Exception {
        String json = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[{\"name\":\"Fan\",\"type\":\"entity\",\"id\":\"fan.living_room\"}],\"failed\":[]},\"speech\":{\"plain\":{\"speech\":\"Turned on Fan\"}}}}";
        HomeAssistantResponse r = HomeAssistantResponseParser.parse(json);
        assertEquals(HomeAssistantResponse.Kind.ACTION_DONE, r.kind());
        assertEquals("fan.living_room", r.successTargets().get(0).id());
        assertTrue(r.failedTargets().isEmpty());
    }

    @Test public void parsesFailedTarget() throws Exception {
        String json = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[],\"failed\":[{\"name\":\"Fan\",\"type\":\"entity\",\"id\":\"fan.living_room\"}]}}}";
        HomeAssistantResponse r = HomeAssistantResponseParser.parse(json);
        assertEquals(1, r.failedTargets().size());
        assertTrue(r.successTargets().isEmpty());
    }

    @Test public void parsesNoValidTargets() throws Exception {
        String json = "{\"response\":{\"response_type\":\"error\",\"data\":{\"code\":\"no_valid_targets\"},\"speech\":{\"plain\":{\"speech\":\"No matching target\"}}}}";
        HomeAssistantResponse r = HomeAssistantResponseParser.parse(json);
        assertEquals(HomeAssistantResponse.Kind.NO_VALID_TARGETS, r.kind());
    }

    @Test public void malformedJsonBecomesUnknownError() {
        HomeAssistantResponse r = HomeAssistantResponseParser.parse("not-json");
        assertEquals(HomeAssistantResponse.Kind.UNKNOWN_ERROR, r.kind());
        assertTrue(r.successTargets().isEmpty());
        assertTrue(r.failedTargets().isEmpty());
    }
}
