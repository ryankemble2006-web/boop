package com.boop.alpha1;

final class BoopNotificationStartupGate {
    enum Target { NOTIFICATION_ONBOARDING, NORMAL }

    private BoopNotificationStartupGate() { }

    static Target resolve(BoopDeviceProfile.Mode mode, boolean onboardingSeen) {
        if (!onboardingSeen
                && (mode == BoopDeviceProfile.Mode.WALL
                || mode == BoopDeviceProfile.Mode.LAUNCHER)) {
            return Target.NOTIFICATION_ONBOARDING;
        }
        return Target.NORMAL;
    }
}
