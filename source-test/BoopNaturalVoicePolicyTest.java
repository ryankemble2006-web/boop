package com.boop.alpha1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BoopNaturalVoicePolicyTest {
    @Test
    public void naturalVoicesStayInApprovedOrder() {
        BoopVoiceController.NaturalVoice[] voices = BoopVoiceController.naturalVoices();
        assertEquals(4, voices.length);
        assertEquals("bf_emma", voices[0].key());
        assertEquals(21, voices[0].sid());
        assertEquals("bf_isabella", voices[1].key());
        assertEquals(22, voices[1].sid());
        assertEquals("bm_george", voices[2].key());
        assertEquals(26, voices[2].sid());
        assertEquals("bm_fable", voices[3].key());
        assertEquals(25, voices[3].sid());
    }

    @Test
    public void kokoroSpeedUsesExistingCadenceBounds() {
        assertEquals(0.70f, BoopNaturalSpeechBackend.speedForRate(0.1f), 0.0001f);
        assertEquals(0.96f, BoopNaturalSpeechBackend.speedForRate(0.96f), 0.0001f);
        assertEquals(1.25f, BoopNaturalSpeechBackend.speedForRate(3.0f), 0.0001f);
    }

    @Test
    public void playbackPitchUsesExistingPitchBounds() {
        assertEquals(0.75f, BoopNaturalSpeechBackend.pitchForPlayback(0.1f), 0.0001f);
        assertEquals(1.12f, BoopNaturalSpeechBackend.pitchForPlayback(1.12f), 0.0001f);
        assertEquals(1.45f, BoopNaturalSpeechBackend.pitchForPlayback(3.0f), 0.0001f);
    }
}
