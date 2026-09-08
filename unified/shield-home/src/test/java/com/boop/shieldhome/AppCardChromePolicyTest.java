package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AppCardChromePolicyTest {
    @Test public void idleCardsFloatWithoutPlate() {
        assertFalse(AppCardChromePolicy.showPlate(false, false, false));
    }

    @Test public void focusedSelectedOrGrabbedCardsShowPlate() {
        assertTrue(AppCardChromePolicy.showPlate(true, false, false));
        assertTrue(AppCardChromePolicy.showPlate(false, true, false));
        assertTrue(AppCardChromePolicy.showPlate(false, false, true));
    }
}
