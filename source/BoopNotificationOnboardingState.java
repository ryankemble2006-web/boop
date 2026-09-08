package com.boop.alpha1;

import android.content.Context;

final class BoopNotificationOnboardingState {
    private static final String PREFS_NAME = "boop_notifications";
    private static final String KEY_ONBOARDING_SEEN = "onboarding_seen_v1";

    private BoopNotificationOnboardingState() { }

    static boolean isSeen(Context context) {
        return context != null
                && context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ONBOARDING_SEEN, false);
    }

    static void markSeen(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ONBOARDING_SEEN, true)
                .apply();
    }
}
