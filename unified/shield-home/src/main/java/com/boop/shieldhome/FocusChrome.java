package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;

/** Shared TV focus chrome so Home and every BOOP TV menu use one accent/style. */
final class FocusChrome {
    static final int BORDER_DP = BoopTvChrome.BORDER_DP;
    static final int ARTWORK_BORDER_DP = BoopTvChrome.BORDER_DP;

    private FocusChrome() { }

    static int accentColor(Context context) {
        return BoopTvChrome.accentColor(context);
    }

    static GradientDrawable filled(
            Context context,
            int fillColor,
            int cornerRadiusDp,
            boolean focused) {
        return BoopTvChrome.filled(context, fillColor, cornerRadiusDp, focused);
    }

    static void clipRounded(View view, int cornerRadiusDp) {
        if (view == null) return;
        final float radius = dp(view.getContext(), cornerRadiusDp);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View target, Outline outline) {
                outline.setRoundRect(0, 0, target.getWidth(), target.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
        view.invalidateOutline();
    }

    static GradientDrawable artworkOutline(Context context, int cornerRadiusDp) {
        GradientDrawable outline = new GradientDrawable();
        outline.setColor(Color.TRANSPARENT);
        int strokePx = dp(context, ARTWORK_BORDER_DP);
        outline.setCornerRadius(artworkStrokeRadius(dp(context, cornerRadiusDp), strokePx));
        outline.setStroke(strokePx, accentColor(context));
        return outline;
    }

    static float artworkStrokeRadius(float outerRadiusPx, float strokeWidthPx) {
        return Math.max(0f, outerRadiusPx - strokeWidthPx / 2f);
    }

    static GradientDrawable outline(Context context, int cornerRadiusDp) {
        GradientDrawable outline = new GradientDrawable();
        outline.setColor(Color.TRANSPARENT);
        outline.setCornerRadius(dp(context, cornerRadiusDp));
        outline.setStroke(dp(context, BORDER_DP), accentColor(context));
        return outline;
    }

    private static int dp(Context context, int value) {
        if (context == null) return value;
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()));
    }
}
