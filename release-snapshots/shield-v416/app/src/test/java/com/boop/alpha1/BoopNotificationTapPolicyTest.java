package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.ActivityOptions;

import org.junit.Test;

public class BoopNotificationTapPolicyTest {
    @Test
    public void autoCancelOnlyAfterSuccessfulSend() {
        assertTrue(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(true, true));
        assertFalse(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(true, false));
        assertFalse(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(false, true));
    }

    @Test
    public void backgroundStartModesAreVersionSpecific() {
        assertEquals(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE,
                BoopNotificationTapPolicy.backgroundStartModeForSdk(36));
        assertEquals(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
                BoopNotificationTapPolicy.backgroundStartModeForSdk(34));
        assertEquals(
                BoopNotificationTapPolicy.NO_BACKGROUND_START_OVERRIDE,
                BoopNotificationTapPolicy.backgroundStartModeForSdk(33));
    }
}
