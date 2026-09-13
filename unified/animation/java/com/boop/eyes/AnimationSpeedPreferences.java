package com.boop.eyes;

import android.content.Context;
import android.content.SharedPreferences;

/** Device-local motion rate. No Android global settings, voice settings or network writes. */
public final class AnimationSpeedPreferences {
    static final String KEY = "animation_speed";
    private AnimationSpeedPreferences() { }
    static SharedPreferences store(Context context) {
        return context.getSharedPreferences("boop_appearance", Context.MODE_PRIVATE);
    }
    public static double load(Context context) {
        try { return AnimationClock.normaliseSpeed(store(context).getFloat(KEY, 1f)); }
        catch (ClassCastException malformed) { return 1; }
    }
    public static void save(Context context, double speed) {
        store(context).edit().putFloat(KEY, (float) AnimationClock.normaliseSpeed(speed)).apply();
    }
}
