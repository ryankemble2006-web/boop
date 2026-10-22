package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BoopNotificationStartupGateTest {
    @Test
    public void unseenPhonesGetNotificationOnboarding() {
        assertEquals(BoopNotificationStartupGate.Target.NOTIFICATION_ONBOARDING,
                BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.WALL, false));
        assertEquals(BoopNotificationStartupGate.Target.NOTIFICATION_ONBOARDING,
                BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.LAUNCHER, false));
    }

    @Test
    public void shieldNeverGetsPhoneNotificationOnboarding() {
        assertEquals(BoopNotificationStartupGate.Target.NORMAL,
                BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.SHIELD, false));
    }

    @Test
    public void seenPhonesRouteNormally() {
        assertEquals(BoopNotificationStartupGate.Target.NORMAL,
                BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.WALL, true));
        assertEquals(BoopNotificationStartupGate.Target.NORMAL,
                BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.LAUNCHER, true));
    }
}
