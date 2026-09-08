package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists only the compact pronunciation profile. Raw enrolment audio is never stored. */
final class BoopWakeEnrollmentStore {
    static final String KEY = "wake_profile_v1";

    private BoopWakeEnrollmentStore() { }

    static void save(Context context, BoopWakeAcousticProfile profile) {
        if (context == null || profile == null) return;
        context.getSharedPreferences(BoopWakeNameStore.PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, profile.encode()).apply();
    }

    static BoopWakeAcousticProfile load(Context context, String selectedName) {
        if (context == null || BoopWakeName.isDefault(selectedName)) return null;
        SharedPreferences prefs = context.getSharedPreferences(BoopWakeNameStore.PREFS, Context.MODE_PRIVATE);
        BoopWakeAcousticProfile profile = BoopWakeAcousticProfile.decode(prefs.getString(KEY, null));
        if (profile == null || !BoopWakeName.normalize(selectedName).equalsIgnoreCase(profile.name())) return null;
        return profile;
    }

    static boolean hasProfile(Context context, String selectedName) {
        return load(context, selectedName) != null;
    }

    static void clear(Context context) {
        if (context == null) return;
        context.getSharedPreferences(BoopWakeNameStore.PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY).apply();
    }
}
