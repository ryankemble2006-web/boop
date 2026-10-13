package com.boop.alpha1;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.TextView;

/** Shared lab control styling, independent of the Home app's implementation. */
public final class BoopLabFocus {
    private BoopLabFocus() { }

    public static void apply(TextView control, int accent) {
        int left = control.getPaddingLeft(), top = control.getPaddingTop();
        int right = control.getPaddingRight(), bottom = control.getPaddingBottom();
        float density = control.getContext().getResources().getDisplayMetrics().density;
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[] {android.R.attr.state_focused}, fill(density, accent, true));
        background.addState(new int[] {android.R.attr.state_pressed}, fill(density, accent, true));
        background.addState(new int[0], fill(density, accent, false));
        control.setDefaultFocusHighlightEnabled(false);
        control.setStateListAnimator(null);
        control.setBackgroundTintList(null);
        control.setBackground(background);
        control.setTextColor(Color.WHITE);
        control.setPadding(left, top, right, bottom);
    }

    private static GradientDrawable fill(float density, int accent, boolean focused) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(34, 34, 34));
        drawable.setCornerRadius(10 * density);
        drawable.setStroke(Math.max(1, Math.round((focused ? 4 : 1) * density)),
                focused ? accent : Color.rgb(82, 82, 82));
        return drawable;
    }
}
