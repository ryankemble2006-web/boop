package com.boop.shieldhome;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class NowPlayingAccessSettingsPlanTest {
    @Test public void modernAndroidTriesExactTvNotificationAccessBeforeFallbacks() {
        assertArrayEquals(
                new NowPlayingAccessSettingsPlan.Route[] {
                        NowPlayingAccessSettingsPlan.Route.TV_EXACT,
                        NowPlayingAccessSettingsPlan.Route.GENERIC,
                        NowPlayingAccessSettingsPlan.Route.DETAIL
                },
                NowPlayingAccessSettingsPlan.routesForSdk(30));
    }

    @Test public void olderAndroidTriesExactTvNotificationAccessThenGeneric() {
        assertArrayEquals(
                new NowPlayingAccessSettingsPlan.Route[] {
                        NowPlayingAccessSettingsPlan.Route.TV_EXACT,
                        NowPlayingAccessSettingsPlan.Route.GENERIC
                },
                NowPlayingAccessSettingsPlan.routesForSdk(29));
    }

    @Test public void exactTvRouteTargetsAndroidTvNotificationAccessActivity() {
        assertEquals("com.android.tv.settings", NowPlayingAccessSettingsPlan.tvSettingsPackage());
        assertEquals(
                "com.android.tv.settings.privacy.NotificationAccessActivity",
                NowPlayingAccessSettingsPlan.tvNotificationAccessClassName());
    }
}
