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
    static final int BORDER_DP = BoopTvChrome.FOCUS_STROKE_DP;
    static final int ARTWORK_BORDER_DP = 5;

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
        return new ArtworkOutline(dp(context, cornerRadiusDp), dp(context, ARTWORK_BORDER_DP) + 2,
                accentColor(context));
    }

    static GradientDrawable artworkIdleOutline(Context context, int cornerRadiusDp) {
        return new ArtworkOutline(dp(context, cornerRadiusDp), Math.max(1, dp(context, 1)),
                Color.argb(42, 255, 255, 255));
    }

    /** Opaque ring fills all the way to the artwork's outer silhouette. */
    static final class ArtworkOutline extends GradientDrawable {
        final float radiusPx, strokePx;
        private final int colour;
        ArtworkOutline(float radius, float stroke, int colour) {
            radiusPx = radius; strokePx = stroke; this.colour = colour;
        }
        @Override public void draw(android.graphics.Canvas canvas) {
            drawArtworkOutline(canvas, new android.graphics.RectF(getBounds()), radiusPx, strokePx, colour);
        }
    }
    static void drawArtworkOutline(android.graphics.Canvas canvas, android.graphics.RectF bounds,
            float radius, float stroke, int colour) {
        android.graphics.Path ring = new android.graphics.Path();
        ring.setFillType(android.graphics.Path.FillType.EVEN_ODD);
        ring.addRoundRect(bounds, radius, radius, android.graphics.Path.Direction.CW);
        android.graphics.RectF inner = new android.graphics.RectF(bounds);
        inner.inset(stroke, stroke);
        if (inner.width() > 0 && inner.height() > 0)
            ring.addRoundRect(inner, Math.max(0, radius - stroke), Math.max(0, radius - stroke),
                    android.graphics.Path.Direction.CW);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colour); canvas.drawPath(ring, paint);
    }

    static float artworkStrokeRadius(float outerRadiusPx, float strokeWidthPx) {
        return Math.max(0f, outerRadiusPx - strokeWidthPx / 2f);
    }

    static GradientDrawable outline(Context context, int cornerRadiusDp) {
        GradientDrawable outline = new GradientDrawable();
        outline.setColor(Color.TRANSPARENT);
        outline.setCornerRadius(dp(context, cornerRadiusDp));
        outline.setStroke(Math.max(2, dp(context, BORDER_DP)), accentColor(context));
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
