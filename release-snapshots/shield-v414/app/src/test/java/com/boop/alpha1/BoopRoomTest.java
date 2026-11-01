package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

public class BoopRoomTest {
    @Test public void derivesHomeAssistantIdFromNameWhenIdMissing() {
        assertEquals("dining_room", new BoopRoom(null, "Dining Room").id());
    }

    @Test public void preservesAuthoritativeHomeAssistantId() {
        assertEquals("lounge", new BoopRoom("lounge", "Living Room").id());
    }
}
