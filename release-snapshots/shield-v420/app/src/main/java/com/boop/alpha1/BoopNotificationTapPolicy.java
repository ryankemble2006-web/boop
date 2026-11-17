package com.boop.alpha1;

import android.app.ActivityOptions;

final class BoopNotificationTapPolicy {
    static final int NO_BACKGROUND_START_OVERRIDE = -1;

    private BoopNotificationTapPolicy() { }

    static boolean shouldCancelAfterSuccessfulSend(boolean autoCancel, boolean sendSucceeded) {
        return autoCancel && sendSucceeded;
    }

    static int backgroundStartModeForSdk(int sdk) {
        if (sdk >= 36) {
            return ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE;
        }
        if (sdk >= 34) {
            return ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED;
        }
        return NO_BACKGROUND_START_OVERRIDE;
    }
}
