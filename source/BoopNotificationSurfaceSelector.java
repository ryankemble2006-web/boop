package com.boop.alpha1;

final class BoopNotificationSurfaceSelector {
    private BoopNotificationSurfaceSelector() { }

    static BoopNotificationSurface choose(
            boolean interactive,
            boolean keyguardLocked,
            boolean wallHostVisible) {
        if (!interactive || keyguardLocked) {
            return BoopNotificationSurface.LOCKED;
        }
        return wallHostVisible
                ? BoopNotificationSurface.IN_PLACE
                : BoopNotificationSurface.OVERLAY;
    }
}
