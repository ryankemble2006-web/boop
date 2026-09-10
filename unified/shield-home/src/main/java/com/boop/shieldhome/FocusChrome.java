package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

/** Shared TV focus chrome so the Now Playing progress fill and focus outline use one accent. */
final class FocusChrome {
    static final int BORDER_DP = 3;

    private FocusChrome() { }

    static int accentColor(Context context) {
        // Home uses BOOP blue consistently, independent of the firmware's default accent.
        return Color.rgb(77, 184, 255);
    }

    static GradientDrawable filled(
            Context context,
            int fillColor,
            int cornerRadiusDp,
            boolean focused) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fillColor);
        background.setCornerRadius(dp(context, cornerRadiusDp));
        if (focused) {
            background.setStroke(dp(context, BORDER_DP), accentColor(context));
        }
        return background;
    }

    static GradientDrawable outline(Context context, int cornerRadiusDp) {
        GradientDrawable outline = new GradientDrawable();
        outline.setColor(Color.TRANSPARENT);
        outline.setCornerRadius(dp(context, cornerRadiusDp));
        outline.setStroke(dp(context, BORDER_DP), accentColor(context));
        return outline;
    }

    private static int dp(Context context, int value) {
        if (context == null) {
            return value;
        }
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()));
    }
}
