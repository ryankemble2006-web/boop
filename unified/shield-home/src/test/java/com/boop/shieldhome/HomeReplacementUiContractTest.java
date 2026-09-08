package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
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

    @Test public void accessibilityOverrideTargetsTvSettingsDirectlyOnShield() {
        ComponentName preferred = ShieldLauncherActivity.preferredTvAccessibilityComponent();
        assertEquals("com.android.tv.settings", preferred.getPackageName());
        assertEquals("com.android.tv.settings.system.AccessibilityActivity", preferred.getClassName());

        ComponentName fallback = ShieldLauncherActivity.tvSettingsFallbackComponent();
        assertEquals("com.android.tv.settings", fallback.getPackageName());
        assertEquals("com.android.tv.settings.MainSettings", fallback.getClassName());
        assertTrue(AccessibilityService.class.isAssignableFrom(ShieldHomeOverrideService.class));
    }
}
