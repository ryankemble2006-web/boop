package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopNotificationCuePolicyTest {
    @Test
    public void neverDoubleAlertsWithNativeEffectsStillOn() {
        BoopNotificationChannelInfo noisy = new BoopNotificationChannelInfo(
                "com.chat", "messages", "Messages", true, true, false, 1L);
        BoopNotificationChannelInfo silent = new BoopNotificationChannelInfo(
                "com.chat", "messages", "Messages", true, false, false, 1L);
        BoopNotificationChannelInfo unknown = new BoopNotificationChannelInfo(
                "com.chat", "messages", "Messages", false, false, false, 1L);

        assertFalse(BoopNotificationCuePolicy.shouldPlay(true, noisy));
        assertTrue(BoopNotificationCuePolicy.shouldPlay(true, silent));
        assertFalse(BoopNotificationCuePolicy.shouldPlay(false, silent));
        assertFalse(BoopNotificationCuePolicy.shouldPlay(true, unknown));
    }
}
