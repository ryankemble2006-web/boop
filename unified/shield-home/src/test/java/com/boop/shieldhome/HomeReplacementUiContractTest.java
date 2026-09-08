package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.provider.Settings;
import org.junit.Test;

public final class HomeReplacementUiContractTest {
    @Test public void homeRowsExposesConsumerHomeReplacementActions() throws Exception {
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onMakeBoopHome"));
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onRetireStockHome"));
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getDeclaredMethod("onRestoreStockHome"));
    }

    @Test public void activityOwnsHomeRoleAndStockLauncherRecoveryFlow() throws Exception {
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("maybePromptForHomeRole"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("requestHomeRole"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("resolvedHomePackage"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("openStockHomeAppInfo"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("restoreStockHome"));
    }

    @Test public void repairedSetupUsesFreshPromptGenerationAndExplicitHomeSettings() {
        assertEquals("home_prompt_shown_v2", ShieldLauncherActivity.homePromptKey());
        assertEquals(Settings.ACTION_HOME_SETTINGS, ShieldLauncherActivity.preferredHomeChooserAction());
    }
}
