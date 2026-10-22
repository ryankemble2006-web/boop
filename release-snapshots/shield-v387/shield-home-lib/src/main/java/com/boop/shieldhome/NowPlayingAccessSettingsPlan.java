package com.boop.shieldhome;

/** Pure routing plan for BOOP Notification Listener special access on Android TV. */
final class NowPlayingAccessSettingsPlan {
    enum Route { TV_EXACT, GENERIC, DETAIL }

    private static final String TV_SETTINGS_PACKAGE = "com.android.tv.settings";
    private static final String TV_NOTIFICATION_ACCESS_CLASS =
            "com.android.tv.settings.privacy.NotificationAccessActivity";

    private NowPlayingAccessSettingsPlan() { }

    static Route[] routesForSdk(int sdk) {
        if (sdk >= 30) {
            return new Route[] { Route.TV_EXACT, Route.GENERIC, Route.DETAIL };
        }
        return new Route[] { Route.TV_EXACT, Route.GENERIC };
    }

    static String tvSettingsPackage() {
        return TV_SETTINGS_PACKAGE;
    }

    static String tvNotificationAccessClassName() {
        return TV_NOTIFICATION_ACCESS_CLASS;
    }
}
