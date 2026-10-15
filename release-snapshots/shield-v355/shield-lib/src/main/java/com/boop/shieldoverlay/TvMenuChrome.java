package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

/** Optional launcher appearance bridge, without a circular library dependency. */
final class TvMenuChrome {
    static int accent(Context context) {
        try {
            Class<?> chrome = context.getClassLoader().loadClass("com.boop.shieldhome.BoopTvChrome");
            return (Integer)chrome.getMethod("accentColor", Context.class).invoke(null, context);
        } catch (ReflectiveOperationException unavailable) { return 0xff4db8ff; }
    }
    static GradientDrawable background(Context context, boolean focused) {
        float density = context.getResources().getDisplayMetrics().density;
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.rgb(34,34,34)); shape.setCornerRadius(10*density);
        if (focused) shape.setStroke(Math.round(4*density), accent(context));
        return shape;
    }
}
