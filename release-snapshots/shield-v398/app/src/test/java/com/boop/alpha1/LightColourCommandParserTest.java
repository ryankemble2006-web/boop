package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public final class LightColourCommandParserTest {
    @Test public void recognisesNaturalRoomColourCommands() {
        assertEquals("purple", LightColourCommandParser.parseColour("lights purple"));
        assertEquals("purple", LightColourCommandParser.parseColour("make the lights purple"));
        assertEquals("purple", LightColourCommandParser.parseColour("make all the lights purple"));
        assertEquals("purple", LightColourCommandParser.parseColour("set the lights to purple"));
        assertEquals("blue", LightColourCommandParser.parseColour("turn the lights blue"));
    }

    @Test public void doesNotHijackPowerCommandsOrGeneralQuestions() {
        assertNull(LightColourCommandParser.parseColour("turn the lights off"));
        assertNull(LightColourCommandParser.parseColour("turn the lights on"));
        assertNull(LightColourCommandParser.parseColour("why is the sky blue"));
        assertNull(LightColourCommandParser.parseColour("play purple rain"));
    }
}
