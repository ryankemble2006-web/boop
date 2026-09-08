package com.boop.shieldhome;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public final class NowPlayingAccessSettingsPlanTest {
    @Test public void modernAndroidTriesDetailThenGenericNotificationListenerSettings() {
        assertArrayEquals(
                new NowPlayingAccessSettingsPlan.Route[] {
                        NowPlayingAccessSettingsPlan.Route.DETAIL,
                        NowPlayingAccessSettingsPlan.Route.GENERIC
                },
                NowPlayingAccessSettingsPlan.routesForSdk(30));
    }

    @Test public void olderAndroidUsesGenericNotificationListenerSettings() {
        assertArrayEquals(
                new NowPlayingAccessSettingsPlan.Route[] {
                        NowPlayingAccessSettingsPlan.Route.GENERIC
                },
                NowPlayingAccessSettingsPlan.routesForSdk(29));
    }
}
