package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.Set;

public final class ShieldStartupManagerView extends LinearLayout {
    public interface Callbacks {
        void onCheckLocalLink();
        void onSetAuto(boolean enabled);
        void onTogglePrevention(String packageName, boolean enabled);
        void onToggleTarget(String packageName, boolean enabled);
        void onRunNow();
        void onBack();
    }

    public ShieldStartupManagerView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(52), dp(30), dp(52), dp(28));
    }

    public void render(boolean auto, boolean hasIdentity, Set<String> prevented, Set<String> selected,
                       List<TvAppEntry> preventionCandidates, List<TvAppEntry> cleanupCandidates, String lastSummary, Callbacks callbacks) {
        removeAllViews();
        addView(text("Startup Manager", 28), wrap());
        addSpacer(10);
        addView(text("One clean-up pass after boot. Apps you open later stay open.", 16), wrap());
        addSpacer(16);

        addAction("Local cleanup link: " + (hasIdentity ? "SET UP" : "NEEDS APPROVAL"), callbacks::onCheckLocalLink);
        addAction("Clean after boot: " + (auto ? "ON" : "OFF"), () -> callbacks.onSetAuto(!auto));
        addAction("Run clean-up now", callbacks::onRunNow);

        addSpacer(18);
        addView(text("Prevent background start", 20), wrap());
        addSpacer(6);
        addView(text("Recommended. Apps still open normally when you choose them. Turning this OFF restores the exact state BOOP found before changing it.", 15), wrap());
        addSpacer(8);
        for (TvAppEntry entry : preventionCandidates) {
            boolean on = prevented.contains(entry.packageName());
            addAction(entry.label() + " ? prevent: " + (on ? "ON" : "OFF"),
                    () -> callbacks.onTogglePrevention(entry.packageName(), !on));
        }

        addSpacer(18);
        addView(text("Clean after boot fallback (max " + StartupCleanupPolicy.MAX_TARGETS + ")", 20), wrap());
        addSpacer(8);
        for (TvAppEntry entry : cleanupCandidates) {
            boolean on = selected.contains(entry.packageName());
            addAction(entry.label() + ": " + (on ? "ON" : "OFF"),
                    () -> callbacks.onToggleTarget(entry.packageName(), !on));
        }

        addSpacer(18);
        addView(text("Left alone automatically", 20), wrap());
        addSpacer(6);
        addView(text("Android, NVIDIA, Google services, BOOP, Netflix, Plex and Deezer.", 15), wrap());
        addSpacer(8);
        addView(text("Old SHIELD TURBO startup blocks keep their original Undo there. BOOP does not fake-migrate them.", 15), wrap());

        if (lastSummary != null && !lastSummary.isBlank()) {
            addSpacer(18);
            addView(text("Last result: " + lastSummary, 15), wrap());
        }
        addSpacer(20);
        addAction("Back to Launcher Settings", callbacks::onBack);
    }

    private void addAction(String label, Runnable action) {
        TextView view = text(label, 19);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setFocusable(true);
        view.setClickable(true);
        view.setPadding(dp(20), dp(10), dp(20), dp(10));
        view.setBackground(background(false));
        view.setOnFocusChangeListener((v, focused) -> {
            v.setBackground(background(focused));
            v.animate().scaleX(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                    .scaleY(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                    .setDuration(TvAppCardView.FOCUS_DURATION_MS).start();
        });
        view.setOnClickListener(v -> action.run());
        addView(view, new LayoutParams(dp(620), dp(62)));
        addSpacer(10);
    }

    private TextView text(String value, int sp) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return view;
    }

    private GradientDrawable background(boolean focused) {
        return FocusChrome.filled(getContext(), Color.rgb(42,42,42), 10, focused);
    }
    private LayoutParams wrap() { return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
    private void addSpacer(int amount) { addView(new View(getContext()), new LayoutParams(1, dp(amount))); }
    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
