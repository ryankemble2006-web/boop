package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class FocusCardView extends TextView {
    public FocusCardView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);
        setGravity(Gravity.CENTER_VERTICAL);
        setTextColor(Color.WHITE);
        setTextSize(25f);
        setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        setPadding(dp(26), dp(18), dp(26), dp(18));
        setBackgroundColor(Color.rgb(36, 36, 36));
        setMinHeight(dp(78));
        setOnFocusChangeListener((view, hasFocus) -> applyFocus(hasFocus));
    }

    public FocusCardView label(String text) {
        setText(text == null ? "" : text);
        return this;
    }

    public void setActivatedVisual(boolean active) {
        setAlpha(active ? 1f : 0.78f);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            return performClick();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void applyFocus(boolean hasFocus) {
        setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
        setBackgroundColor(hasFocus ? Color.WHITE : Color.rgb(36, 36, 36));
        setTranslationZ(hasFocus ? dp(8) : 0f);
        animate()
                .scaleX(hasFocus ? 1.045f : 1f)
                .scaleY(hasFocus ? 1.045f : 1f)
                .setDuration(100L)
                .start();
    }

    protected int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
