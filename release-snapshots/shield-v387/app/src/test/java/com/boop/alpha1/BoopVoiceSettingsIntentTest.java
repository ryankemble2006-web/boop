package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopVoiceSettingsIntentTest {
    @Test
    public void naturalSettingsRequestsOpenTheSliders() {
        assertTrue(BoopVoiceSettingsIntent.matches("voice settings"));
        assertTrue(BoopVoiceSettingsIntent.matches("Can I adjust your voice?"));
        assertTrue(BoopVoiceSettingsIntent.matches("change your pitch"));
        assertTrue(BoopVoiceSettingsIntent.matches("adjust your cadence please"));
        assertTrue(BoopVoiceSettingsIntent.matches("change your speech speed"));
    }

    @Test
    public void plainVoiceChangeStillBelongsToVoiceCycling() {
        assertFalse(BoopVoiceSettingsIntent.matches("change your voice"));
        assertFalse(BoopVoiceSettingsIntent.matches("try another voice"));
    }

    @Test
    public void unrelatedCommandsDoNotOpenSettings() {
        assertFalse(BoopVoiceSettingsIntent.matches("volume up"));
        assertFalse(BoopVoiceSettingsIntent.matches("pause the music"));
        assertFalse(BoopVoiceSettingsIntent.matches("why is the sky blue"));
    }
}
