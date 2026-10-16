package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ShieldEntryRouteTest {
    @Test public void shieldHomeIntentUsesIntegratedHome() {
        ShieldEntryRoute.Target target = ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.SHIELD, true);
        assertEquals("com.boop.shieldhome.ShieldLauncherActivity", target.className());
        assertTrue(target.suppressEntryTransition());
    }

    @Test public void ordinaryShieldLaunchUsesSameHome() {
        ShieldEntryRoute.Target target = ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.SHIELD, false);
        assertEquals("com.boop.shieldhome.ShieldLauncherActivity", target.className());
        assertTrue(target.suppressEntryTransition());
    }

    @Test public void otherBodiesRemainUnchanged() {
        assertEquals("com.boop.alpha1.MainActivity",
                ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.WALL, true).className());
        assertEquals("com.boop.launcher.MainActivity",
                ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.LAUNCHER, true).className());
    }
}
