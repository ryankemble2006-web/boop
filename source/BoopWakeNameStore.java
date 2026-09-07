package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;

final class BoopWakeNameStore {
    static final String PREFS = "boop_voice";
    static final String KEY = "wake_name";

    private BoopWakeNameStore() { }

    static String load(Context context) {
        if (context == null) {
            return BoopWakeName.DEFAULT;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return BoopWakeName.normalize(prefs.getString(KEY, BoopWakeName.DEFAULT));
    }

    static String save(Context context, String value) {
        String normalized = BoopWakeName.normalize(value);
        if (context != null) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY, normalized)
                    .apply();
        }
        return normalized;
    }

    static String reset(Context context) {
        return save(context, BoopWakeName.DEFAULT);
    }
}
