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

/** Presentation-only Home rows controls; platform actions are owned by the activity. */
public final class ShieldHomeSettingsView extends LinearLayout {
    public interface Callbacks {
        void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled);
        void onChooseHomeApp();
        default void onMakeBoopHome() { onChooseHomeApp(); }
        default void onRetireStockHome() { }
        default void onRestoreStockHome() { onChooseHomeApp(); }
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
        setPadding(dp(52), dp(30), dp(52), dp(28));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void render(boolean playNext, boolean appChannels, Callbacks callbacks) {
        removeAllViews();

        TextView title = text("Home rows", 28);
        addView(title, wrap());
        addSpacer(dp(18));

        TextView homeSection = text("Shield Home", 20);
        addView(homeSection, wrap());
        addSpacer(dp(10));

        TextView makeHome = action("Make BOOP my Home");
        makeHome.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onMakeBoopHome();
        });
        addView(makeHome, rowParams());
        addSpacer(dp(10));

        TextView retireHome = action("Retire Android TV Home");
        retireHome.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onRetireStockHome();
        });
        addView(retireHome, rowParams());
        addSpacer(dp(10));

        TextView restoreHome = action("Restore Shield Home");
        restoreHome.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onRestoreStockHome();
        });
        addView(restoreHome, rowParams());

        addSpacer(dp(20));

        TextView section = text("Optional rows", 20);
        addView(section, wrap());
        addSpacer(dp(10));

        TextView playNextRow = action("Play Next: " + (playNext ? "ON" : "OFF"));
        playNextRow.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onSetRowEnabled(OptionalRowRegistry.Key.PLAY_NEXT, !playNext);
            }
        });
        addView(playNextRow, rowParams());
        addSpacer(dp(10));

        TextView channelsRow = action("App content rows: " + (appChannels ? "ON" : "OFF"));
        channelsRow.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onSetRowEnabled(OptionalRowRegistry.Key.APP_CHANNELS, !appChannels);
            }
        });
        addView(channelsRow, rowParams());

        addSpacer(dp(20));

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
        view.setPadding(dp(20), dp(10), dp(20), dp(10));
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
        return new LayoutParams(dp(440), dp(62));
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
