package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ColorFilter;

final class BoopEyeHue {
    private static final String PREFS_NAME = "boop_eyes";
    private static final String KEY_HUE_DEGREES = "hue_degrees";

    private BoopEyeHue() { }

    static int loadHue(Context context) {
        if (context == null) {
            return BoopEyeHueMath.DEFAULT_HUE_DEGREES;
        }
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return BoopEyeHueMath.clampHue(
                preferences.getInt(KEY_HUE_DEGREES, BoopEyeHueMath.DEFAULT_HUE_DEGREES));
    }

    static void saveHue(Context context, int hueDegrees) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_HUE_DEGREES, BoopEyeHueMath.clampHue(hueDegrees))
                .apply();
    }

    static ColorFilter colorFilterForHue(int hueDegrees) {
        float[] values = BoopEyeHueMath.matrixForHue(hueDegrees);
        if (values == null) {
            return null;
        }
        return new ColorMatrixColorFilter(new ColorMatrix(values));
    }
}
