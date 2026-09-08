package com.boop.shieldhome;

import static org.junit.Assert.assertNotNull;

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
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("openStockHomeAppInfo"));
        assertNotNull(ShieldLauncherActivity.class.getDeclaredMethod("restoreStockHome"));
    }
}
