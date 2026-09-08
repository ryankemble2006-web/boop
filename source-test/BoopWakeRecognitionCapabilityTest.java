package com.boop.alpha1;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class BoopWakeRecognitionCapabilityTest {
    @Test
    public void modernDeviceWithRecognizerCanAttemptWakeWithoutSupportProbe() {
        assertTrue(BoopWakeRecognitionCapability.canAttempt(33, true));
        assertTrue(BoopWakeRecognitionCapability.canAttempt(36, true));
    }

    @Test
    public void missingRecognizerOrOldPlatformCannotAttemptInjectedAudioWake() {
        assertFalse(BoopWakeRecognitionCapability.canAttempt(36, false));
        assertFalse(BoopWakeRecognitionCapability.canAttempt(32, true));
    }
}
