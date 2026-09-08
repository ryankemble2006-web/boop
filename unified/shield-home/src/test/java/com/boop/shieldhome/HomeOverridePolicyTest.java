package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HomeOverridePolicyTest {
    @Test public void onlyRealShieldStockHomeTriggersOverride() {
        assertTrue(HomeOverridePolicy.shouldReplaceForeground(
                "com.google.android.tvlauncher", "com.boop.shieldhome"));
        assertFalse(HomeOverridePolicy.shouldReplaceForeground(
                "com.google.android.tungsten.setupwraith", "com.boop.shieldhome"));
        assertFalse(HomeOverridePolicy.shouldReplaceForeground(
                "com.android.systemui", "com.boop.shieldhome"));
        assertFalse(HomeOverridePolicy.shouldReplaceForeground(
                "com.android.tv.settings", "com.boop.shieldhome"));
        assertFalse(HomeOverridePolicy.shouldReplaceForeground(
                "com.boop.shieldhome", "com.boop.shieldhome"));
        assertFalse(HomeOverridePolicy.shouldReplaceForeground(null, "com.boop.shieldhome"));
    }
}
