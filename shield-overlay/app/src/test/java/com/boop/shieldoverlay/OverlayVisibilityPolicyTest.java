package com.boop.shieldoverlay;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OverlayVisibilityPolicyTest {
    @Test
    public void hideAndShowAreDistinctExplicitServiceActions() {
        assertNotEquals(
                BoopOverlayService.ACTION_HIDE_EYES,
                BoopOverlayService.ACTION_SHOW_EYES);
        assertTrue(BoopOverlayService.ACTION_HIDE_EYES.endsWith("HIDE_EYES"));
        assertTrue(BoopOverlayService.ACTION_SHOW_EYES.endsWith("SHOW_EYES"));
    }
}
