package com.boop.shieldoverlay;

public final class DeezerAccessSettingsPlan {
    public enum Route {
        DETAIL,
        GENERIC
    }

    private DeezerAccessSettingsPlan() {
    }

    public static Route[] routesForSdk(int sdk) {
        if (sdk >= 30) {
            return new Route[] {Route.DETAIL, Route.GENERIC};
        }
        return new Route[] {Route.GENERIC};
    }
}
