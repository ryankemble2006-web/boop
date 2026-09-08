package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BoopNotificationSurfaceSelectorTest {
    @Test
    public void screenOffUsesLockedSurface() {
        assertEquals(
                BoopNotificationSurface.LOCKED,
                BoopNotificationSurfaceSelector.choose(false, false, false));
    }

    @Test
    public void keyguardLockedUsesLockedSurface() {
        assertEquals(
                BoopNotificationSurface.LOCKED,
                BoopNotificationSurfaceSelector.choose(true, true, true));
    }

    @Test
    public void unlockedWallUsesInPlaceSurface() {
        assertEquals(
                BoopNotificationSurface.IN_PLACE,
                BoopNotificationSurfaceSelector.choose(true, false, true));
    }

    @Test
    public void unlockedOtherAppUsesOverlaySurface() {
        assertEquals(
                BoopNotificationSurface.OVERLAY,
                BoopNotificationSurfaceSelector.choose(true, false, false));
    }
}
