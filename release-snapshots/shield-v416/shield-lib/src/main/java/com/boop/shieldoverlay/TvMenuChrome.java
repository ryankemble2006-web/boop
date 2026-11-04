package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

/** Optional launcher appearance bridge, without a circular library dependency. */
final class TvMenuChrome {
    static final int SURFACE_PANEL = Color.rgb(14, 16, 20);
    static final int SURFACE_CARD = Color.rgb(21, 24, 30);
    static final int SURFACE_ELEVATED = Color.rgb(29, 34, 42);
    static final int BORDER_SUBTLE = Color.rgb(42, 47, 57);
    static final int TEXT_SECONDARY = Color.rgb(168, 176, 190);
    static final Typeface FONT_MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    static final Typeface FONT_REGULAR = Typeface.create("sans-serif", Typeface.NORMAL);

    static int accent(Context context) {
        try {
            Class<?> chrome = context.getClassLoader().loadClass("com.boop.shieldhome.BoopTvChrome");
            return (Integer)chrome.getMethod("accentColor", Context.class).invoke(null, context);
        } catch (ReflectiveOperationException unavailable) { return 0xff4db8ff; }
    }
    static GradientDrawable background(Context context, boolean focused) {
        float density = context.getResources().getDisplayMetrics().density;
        int accent = accent(context);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(focused ? blend(SURFACE_ELEVATED, accent, 0.14f) : SURFACE_CARD);
        shape.setCornerRadius(14 * density);
        shape.setStroke(Math.max(1, Math.round((focused ? 2f : 1f) * density)), focused ? accent : BORDER_SUBTLE);
        return shape;
    }
    private static int blend(int base, int tint, float ratio) {
        float inv = 1f - ratio;
        return Color.rgb(
                Math.round(Color.red(base) * inv + Color.red(tint) * ratio),
                Math.round(Color.green(base) * inv + Color.green(tint) * ratio),
                Math.round(Color.blue(base) * inv + Color.blue(tint) * ratio));
    }
}
