package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class BoopDeviceProfileTest {
    @Test public void shieldWinsOnTelevision() {
        assertEquals(BoopDeviceProfile.Mode.SHIELD,
                BoopDeviceProfile.resolve(true, "SHIELD Android TV", null));
    }

    @Test public void pixel7ProDefaultsToWall() {
        assertEquals(BoopDeviceProfile.Mode.WALL,
                BoopDeviceProfile.resolve(false, "Pixel 7 Pro", null));
    }

    @Test public void otherHandheldsDefaultToLauncher() {
        assertEquals(BoopDeviceProfile.Mode.LAUNCHER,
                BoopDeviceProfile.resolve(false, "Pixel 10 Pro XL", null));
    }

    @Test public void explicitOverrideWins() {
        assertEquals(BoopDeviceProfile.Mode.WALL,
                BoopDeviceProfile.resolve(true, "SHIELD Android TV", "wall"));
    }

    @Test public void androidTabletAt600DpDefaultsToWall() {
        assertEquals(BoopDeviceProfile.Mode.WALL,
                BoopDeviceProfile.resolve(false, "24018RPACC", null, 600));
    }

    @Test public void xiaomiPadWidthDefaultsToWall() {
        assertEquals(BoopDeviceProfile.Mode.WALL,
                BoopDeviceProfile.resolve(false, "24018RPACC", null, 800));
    }

    @Test public void phoneBelowTabletBoundaryStaysLauncher() {
        assertEquals(BoopDeviceProfile.Mode.LAUNCHER,
                BoopDeviceProfile.resolve(false, "Pixel 10 Pro XL", null, 599));
    }

    @Test public void televisionStillWinsAtTabletWidth() {
        assertEquals(BoopDeviceProfile.Mode.SHIELD,
                BoopDeviceProfile.resolve(true, "SHIELD Android TV", null, 800));
    }
}
