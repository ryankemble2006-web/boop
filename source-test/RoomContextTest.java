package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;

public final class RoomContextTest {
    private final RoomContext context = new RoomContext(
            "Living Room", List.of("Living Room", "Bedroom"));

    @Test public void unqualifiedFanGetsLivingRoom() {
        assertEquals("turn on the fan in the Living Room", context.qualify("turn on the fan"));
    }

    @Test public void trailingOnIsNormalizedBeforeRoomContext() {
        assertEquals("turn on the fan in the Living Room", context.qualify("turn the fan on"));
    }

    @Test public void unqualifiedLightsStillGetLivingRoom() {
        assertEquals("turn on the lights in the Living Room", context.qualify("turn on the lights"));
    }

    @Test public void explicitBedroomWins() {
        assertEquals("turn on the bedroom fan", context.qualify("turn on the bedroom fan"));
    }

    @Test public void explicitLivingRoomIsNotDuplicated() {
        assertEquals("turn off the living room fan", context.qualify("turn off the living room fan"));
    }

    @Test public void bothFansIsNotNarrowed() {
        assertEquals("turn on both fans", context.qualify("turn on both fans"));
    }

    @Test public void allLightsIsNotNarrowed() {
        assertEquals("turn off all lights", context.qualify("turn off all lights"));
    }
}
