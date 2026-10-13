package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopNotificationRuntimeModeTest {
    @Test
    public void phoneModesInitializeNotificationRuntimeButShieldDoesNot() {
        assertTrue(BoopNotificationRuntime.shouldInitializeForMode(BoopDeviceProfile.Mode.WALL));
        assertTrue(BoopNotificationRuntime.shouldInitializeForMode(BoopDeviceProfile.Mode.LAUNCHER));
        assertFalse(BoopNotificationRuntime.shouldInitializeForMode(BoopDeviceProfile.Mode.SHIELD));
    }
}
