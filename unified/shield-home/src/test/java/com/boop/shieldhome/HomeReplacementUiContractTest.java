package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.provider.Settings;
import org.junit.Test;

public final class HomeReplacementUiContractTest {
    @Test public void homeRowsExposesConsumerHomeReplacementActions() throws Exception {
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onMakeBoopHome"));
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onRetireStockHome"));
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onRestoreStockHome"));
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onEnableHomeOverride"));
    }

    @Test public void activityOwnsHomeRoleStockRecoveryAndAccessibilitySetup() throws Exception {
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("maybePromptForHomeRole"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("requestHomeRole"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("resolvedHomePackage"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("openStockHomeAppInfo"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("restoreStockHome"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("openAccessibilitySettings"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("isHomeOverrideEnabled"));
    }

    @Test public void repairedSetupUsesFreshPromptGenerationAndExplicitHomeSettings() {
        assertEquals("home_prompt_shown_v2", ShieldLauncherActivity.homePromptKey());
        assertEquals(Settings.ACTION_HOME_SETTINGS, ShieldLauncherActivity.preferredHomeChooserAction());
    }

    @Test public void accessibilityOverrideUsesInvisibleTvSettingsRouter() {
        assertTrue(Activity.class.isAssignableFrom(ShieldAccessibilityRouteActivity.class));
        assertEquals("com.android.tv.settings", ShieldAccessibilityRouteActivity.tvSettingsPackage());
        assertEquals(
                "com.android.tv.settings.system.AccessibilityActivity",
                ShieldAccessibilityRouteActivity.preferredTvAccessibilityClassName());
        assertEquals(
                "com.android.tv.settings.oemlink.AccessibilitySettingsActivity",
                ShieldAccessibilityRouteActivity.modernTvAccessibilityClassName());
        assertEquals(
                "com.android.tv.settings.MainSettings",
                ShieldAccessibilityRouteActivity.tvSettingsFallbackClassName());
        assertTrue(AccessibilityService.class.isAssignableFrom(ShieldHomeOverrideService.class));
    }

    @Test public void accessibilityOverrideRearmsWhenAndroidReconnectsService() throws Exception {
        assertNotNull(ShieldHomeOverrideService.class.getDeclaredMethod("onServiceConnected"));
    }
}
