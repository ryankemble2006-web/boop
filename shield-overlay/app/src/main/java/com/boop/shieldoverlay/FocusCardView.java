package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;

/** Shared TV focus treatment. Focus changes colour, never scale, alignment or padding. */
public class FocusCardView extends TextView {
    private final GradientDrawable idle;
    private final GradientDrawable focused;

    public FocusCardView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);
        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        setTextSize(25f);
        setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        setMinHeight(dp(78));
        setStateListAnimator(null);
        idle = background(Color.rgb(22, 22, 24), Color.rgb(58, 58, 64));
        focused = background(Color.rgb(61, 220, 255), Color.WHITE);
        applyFocus(false);
        setOnFocusChangeListener((view, hasFocus) -> applyFocus(hasFocus));
    }

    public FocusCardView label(String text) { setText(text == null ? "" : text); return this; }
    public void setActivatedVisual(boolean active) { setAlpha(active ? 1f : .78f); }

    private GradientDrawable background(int fill, int border) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(18));
        drawable.setStroke(dp(2), border);
        return drawable;
    }

    private void applyFocus(boolean hasFocus) {
        setBackground(hasFocus ? focused : idle);
        setPadding(dp(26), dp(18), dp(26), dp(18));
        setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
    }

    protected int dp(int value) { return Math.max(1, Math.round(value * getResources().getDisplayMetrics().density)); }
}
