package com.boop.shieldoverlay;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public final class DeezerAccessSettingsPlanTest {
    @Test
    public void android11AndNewerTryAppDetailBeforeGenericList() {
        assertArrayEquals(
                new DeezerAccessSettingsPlan.Route[] {
                        DeezerAccessSettingsPlan.Route.DETAIL,
                        DeezerAccessSettingsPlan.Route.GENERIC
                },
                DeezerAccessSettingsPlan.routesForSdk(30));
    }

    @Test
    public void olderAndroidUsesGenericNotificationAccessList() {
        assertArrayEquals(
                new DeezerAccessSettingsPlan.Route[] {
                        DeezerAccessSettingsPlan.Route.GENERIC
                },
                DeezerAccessSettingsPlan.routesForSdk(29));
    }
}
