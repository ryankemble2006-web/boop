package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackageRefreshPolicyTest {
    @Test public void reloadsOnlyForPackageCatalogueChanges() {
        assertTrue(PackageRefreshPolicy.shouldReload("android.intent.action.PACKAGE_ADDED"));
        assertTrue(PackageRefreshPolicy.shouldReload("android.intent.action.PACKAGE_REMOVED"));
        assertTrue(PackageRefreshPolicy.shouldReload("android.intent.action.PACKAGE_CHANGED"));
        assertFalse(PackageRefreshPolicy.shouldReload("android.intent.action.TIME_TICK"));
        assertFalse(PackageRefreshPolicy.shouldReload("android.intent.action.SCREEN_ON"));
        assertFalse(PackageRefreshPolicy.shouldReload(null));
    }
}
