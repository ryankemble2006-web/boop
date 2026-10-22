package com.boop.alpha1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BoopNaturalVoicePackTest {
    @Test
    public void stripsOnlyTheKnownArchiveRoot() {
        assertEquals("model.onnx",
                BoopNaturalVoicePack.safeEntryName(
                        "kokoro-multi-lang-v1_0/model.onnx",
                        "kokoro-multi-lang-v1_0"));
    }

    @Test
    public void rejectsTraversalAndAbsoluteEntries() {
        assertThrows(IllegalArgumentException.class,
                () -> BoopNaturalVoicePack.safeEntryName("../escape", "kokoro-multi-lang-v1_0"));
        assertThrows(IllegalArgumentException.class,
                () -> BoopNaturalVoicePack.safeEntryName("/tmp/escape", "kokoro-multi-lang-v1_0"));
        assertThrows(IllegalArgumentException.class,
                () -> BoopNaturalVoicePack.safeEntryName(
                        "kokoro-multi-lang-v1_0/../../escape",
                        "kokoro-multi-lang-v1_0"));
    }
}
