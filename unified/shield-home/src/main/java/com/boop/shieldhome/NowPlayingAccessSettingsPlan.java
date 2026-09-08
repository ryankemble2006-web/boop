package com.boop.shieldhome;

/** Pure Android-version routing for Notification Listener special access. */
final class NowPlayingAccessSettingsPlan {
    enum Route { DETAIL, GENERIC }

    private NowPlayingAccessSettingsPlan() { }

    static Route[] routesForSdk(int sdk) {
        if (sdk >= 30) {
            return new Route[] { Route.DETAIL, Route.GENERIC };
        }
        return new Route[] { Route.GENERIC };
    }
}
