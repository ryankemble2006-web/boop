package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AppCardChromePolicyTest {
    @Test public void idleCardsFloatWithoutPlate() {
        assertFalse(AppCardChromePolicy.showPlate(false, false, false));
    }

    @Test public void focusedSelectedOrGrabbedAppsCardsShowPlate() {
        assertTrue(AppCardChromePolicy.showPlate(true, false, false));
        assertTrue(AppCardChromePolicy.showPlate(false, true, false));
        assertTrue(AppCardChromePolicy.showPlate(false, false, true));
    }

    @Test public void homeCardsNeverShowPlate() {
        assertFalse(AppCardChromePolicy.showPlate(true, false, false, false));
        assertFalse(AppCardChromePolicy.showPlate(true, true, false, false));
        assertFalse(AppCardChromePolicy.showPlate(true, false, true, false));
        assertFalse(AppCardChromePolicy.showPlate(true, false, false, true));
    }

    @Test public void homeHidesFavouriteStarButKeepsGrabIndicator() {
        assertFalse(AppCardChromePolicy.showBadge(true, true, false));
        assertTrue(AppCardChromePolicy.showBadge(true, true, true));
        assertTrue(AppCardChromePolicy.showBadge(false, true, false));
    }

    @Test public void homeEmphasizesArtworkInsteadOfWholeCard() {
        assertTrue(AppCardChromePolicy.emphasizeArtworkOnly(true));
        assertFalse(AppCardChromePolicy.emphasizeArtworkOnly(false));
    }
}
