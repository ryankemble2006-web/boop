package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class BoopWakeTranscriptNormalizerTest {
    @Test public void stripsOnlyLeadingWakeWord() {
        assertEquals("pause the music",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("boop pause the music"));
        assertEquals("turn the lamp off",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Boop, turn the lamp off"));
        assertEquals("",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord(" BOOP! "));
    }

    @Test public void leavesOrdinaryAndLaterBoopUntouched() {
        assertEquals("pause the music",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("pause the music"));
        assertEquals("tell me why boop is funny",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("tell me why boop is funny"));
        assertEquals("boopity boop",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("boopity boop"));
    }
}
