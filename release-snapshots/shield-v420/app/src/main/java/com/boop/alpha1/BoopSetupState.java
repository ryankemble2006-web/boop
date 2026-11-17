package com.boop.alpha1;

import android.content.Context;

/** App-private first setup marker, written only by the user's Continue button. */
final class BoopSetupState {
    private static final String PREFS = "boop_app_setup_v1";
    private static final String COMPLETE = "setup_intro_completed";

    private BoopSetupState() { }

    static boolean complete(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(COMPLETE, false);
    }

    static boolean completeFromUser(Context context) {
        // Persist before routing so process death cannot falsely advance setup.
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(COMPLETE, true).commit();
    }
}
