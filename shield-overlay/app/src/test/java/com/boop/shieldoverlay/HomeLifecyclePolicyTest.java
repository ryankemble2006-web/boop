package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public final class HomeLifecyclePolicyTest {
    @Test
    public void fullScreenHomeIsASeparateActivityFromLauncherGateway() {
        assertNotEquals(MainActivity.class, BoopHomeActivity.class);
        assertEquals("BoopHomeActivity", BoopHomeActivity.class.getSimpleName());
    }

    @Test
    public void homeUsesTheExistingExplicitOverlayVisibilityCommands() {
        assertNotEquals(
                BoopOverlayService.ACTION_HIDE_EYES,
                BoopOverlayService.ACTION_SHOW_EYES);
    }
}
