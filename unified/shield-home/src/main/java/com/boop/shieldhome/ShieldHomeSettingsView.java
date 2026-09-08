package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Presentation-only launcher settings; platform actions are owned by the activity. */
public final class ShieldHomeSettingsView extends LinearLayout {
    public interface Callbacks {
        void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled);
        void onChooseHomeApp();
        void onBackHome();
    }

    public ShieldHomeSettingsView(Context context) {
        this(context, null);
    }

    public ShieldHomeSettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(52), dp(38), dp(52), dp(36));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void render(boolean playNext, boolean appChannels, Callbacks callbacks) {
        removeAllViews();

        TextView title = text("Launcher settings", 28);
        addView(title, wrap());
        addSpacer(dp(26));

        TextView section = text("Optional Home Rows", 20);
        addView(section, wrap());
        addSpacer(dp(12));

        TextView playNextRow = action("Play Next: " + (playNext ? "ON" : "OFF"));
        playNextRow.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onSetRowEnabled(OptionalRowRegistry.Key.PLAY_NEXT, !playNext);
            }
        });
        addView(playNextRow, rowParams());
        addSpacer(dp(12));

        TextView channelsRow = action("App content rows: " + (appChannels ? "ON" : "OFF"));
        channelsRow.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onSetRowEnabled(OptionalRowRegistry.Key.APP_CHANNELS, !appChannels);
            }
        });
        addView(channelsRow, rowParams());

        addSpacer(dp(30));

        TextView chooseHome = action("Choose Home app");
        chooseHome.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onChooseHomeApp();
        });
        addView(chooseHome, rowParams());
        addSpacer(dp(12));

        TextView back = action("Back to Home");
        back.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onBackHome();
        });
        addView(back, rowParams());
    }

    private TextView text(String value, int sp) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return view;
    }

    private TextView action(String value) {
        TextView view = text(value, 19);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setFocusable(true);
        view.setClickable(true);
        view.setPadding(dp(20), dp(12), dp(20), dp(12));
        view.setBackground(actionBackground());
        view.setOnFocusChangeListener((v, focused) -> v.animate()
                .scaleX(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                .scaleY(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                .setDuration(TvAppCardView.FOCUS_DURATION_MS)
                .start());
        return view;
    }

    private GradientDrawable actionBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(42, 42, 42));
        background.setCornerRadius(dp(10));
        return background;
    }

    private LayoutParams rowParams() {
        return new LayoutParams(dp(420), dp(68));
    }

    private LayoutParams wrap() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private void addSpacer(int height) {
        View spacer = new View(getContext());
        addView(spacer, new LayoutParams(1, height));
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
