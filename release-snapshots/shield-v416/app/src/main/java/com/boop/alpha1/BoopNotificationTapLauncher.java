package com.boop.alpha1;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;

final class BoopNotificationTapLauncher {
    enum Result { OPENED, CANCELLED }

    private BoopNotificationTapLauncher() { }

    static Result send(Context context, PendingIntent intent, int sdk) {
        if (context == null || intent == null) {
            return Result.CANCELLED;
        }
        try {
            int mode = BoopNotificationTapPolicy.backgroundStartModeForSdk(sdk);
            if (mode == BoopNotificationTapPolicy.NO_BACKGROUND_START_OVERRIDE) {
                intent.send();
            } else {
                ActivityOptions options = ActivityOptions.makeBasic();
                options.setPendingIntentBackgroundActivityStartMode(mode);
                Bundle bundle = options.toBundle();
                intent.send(context, 0, null, null, null, null, bundle);
            }
            return Result.OPENED;
        } catch (PendingIntent.CanceledException cancelled) {
            return Result.CANCELLED;
        }
    }
}
