package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public final class MediaCommandParserTest {
    @Test public void recognisesShortMediaControls() {
        assertEquals(MediaCommand.PAUSE, MediaCommandParser.parse("pause"));
        assertEquals(MediaCommand.PAUSE, MediaCommandParser.parse("pause music"));
        assertEquals(MediaCommand.RESUME, MediaCommandParser.parse("resume"));
        assertEquals(MediaCommand.RESUME, MediaCommandParser.parse("continue music"));
        assertEquals(MediaCommand.NEXT, MediaCommandParser.parse("skip"));
        assertEquals(MediaCommand.NEXT, MediaCommandParser.parse("next track"));
        assertEquals(MediaCommand.PREVIOUS, MediaCommandParser.parse("previous track"));
        assertEquals(MediaCommand.VOLUME_UP, MediaCommandParser.parse("volume up"));
        assertEquals(MediaCommand.VOLUME_DOWN, MediaCommandParser.parse("volume down"));
    }

    @Test public void doesNotHijackContentRequestsOrHouseCommands() {
        assertNull(MediaCommandParser.parse("play pink floyd"));
        assertNull(MediaCommandParser.parse("turn the lights off"));
        assertNull(MediaCommandParser.parse("why is the sky blue"));
    }
}
