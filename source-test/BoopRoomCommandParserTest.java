package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

public class BoopRoomCommandParserTest {
    @Test public void parsesSimpleLocalBinaryCommands() {
        GenericHomeCommand c = BoopRoomCommandParser.parse("turn the floor lamp on", "living_room", "Living Room");
        assertNotNull(c);
        assertEquals("floor lamp", c.target());
        assertEquals("turn_on", c.service());
    }

    @Test public void marksExplicitOtherRoomForAssist() {
        GenericHomeCommand c = BoopRoomCommandParser.parse("turn on the lamp in the bedroom", "living_room", "Living Room");
        assertNotNull(c);
        assertTrue(c.explicitOtherRoom());
    }

    @Test public void rejectsUnsupportedCommands() {
        assertNull(BoopRoomCommandParser.parse("set the thermostat to 20", "living_room", "Living Room"));
    }

    @Test public void preservesPluralGroupCommandsForAssist() {
        assertTrue(BoopRoomCommandParser.parse(
                "turn off the lights", "living_room", "Living Room").groupTarget());
        assertNull(BoopRoomCommandParser.parse(
                "lights off", "living_room", "Living Room"));
    }

    @Test public void detectsOtherRoomAcrossDirectCommandFamilies() {
        assertTrue(BoopRoomCommandParser.namesOtherRoom(
                "set the lights blue in the bedroom", "living_room", "Living Room"));
        assertFalse(BoopRoomCommandParser.namesOtherRoom(
                "set the lights blue in the living room", "living_room", "Living Room"));
    }
}
