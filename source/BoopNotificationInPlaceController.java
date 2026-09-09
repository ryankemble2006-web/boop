package com.boop.alpha1;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Set;

final class BoopNotificationInPlaceController implements BoopNotificationHost {
    private final FrameLayout parent;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable;
    private View currentView;

    BoopNotificationInPlaceController(FrameLayout parent) {
        if (parent == null) throw new IllegalArgumentException("parent required");
        this.parent = parent;
        this.timeoutRunnable = this::dismissPresentation;
    }

    @Override
    public void show(BoopNotificationPresentation presentation, long timeoutMs) {
        render(presentation, timeoutMs);
    }

    @Override
    public void update(BoopNotificationPresentation presentation, long timeoutMs) {
        render(presentation, timeoutMs);
    }

    @Override
    public void hide() {
        handler.removeCallbacks(timeoutRunnable);
        if (currentView != null) {
            parent.removeView(currentView);
            currentView = null;
        }
    }

    private void render(BoopNotificationPresentation presentation, long timeoutMs) {
        hide();
        if (presentation == null || presentation.cards().isEmpty()) return;
        currentView = createPlainPresentationView(parent.getContext(), presentation);
        BoopNotificationSwipeGesture.attach(currentView, this::dismissPresentation);
        parent.addView(currentView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        handler.postDelayed(timeoutRunnable, Math.max(1L, timeoutMs));
    }

    private void dismissPresentation() {
        hide();
        try {
            BoopNotificationRuntime.get(parent.getContext()).onPresentationDismissed();
        } catch (RuntimeException ignored) {
            // Android's source notification remains authoritative.
        }
    }

    static View createPlainPresentationView(
            Context context,
            BoopNotificationPresentation presentation) {
        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Color.argb(180, 0, 0, 0));
        root.setClickable(true);
        root.setFocusable(true);
        root.setContentDescription("BOOP notification");

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        int pad = dp(context, 24);
        card.setPadding(pad, pad, pad, pad);
        card.setBackgroundColor(Color.rgb(32, 32, 32));

        int count = presentation == null ? 0 : presentation.cards().size();
        TextView heading = text(context,
                count == 1 ? appLabel(presentation) : count + " notifications",
                24f,
                true);
        card.addView(heading, wrap());

        if (presentation != null && !presentation.locked()) {
            if (count == 1) {
                BoopNotificationEnvelope item = presentation.cards().get(0);
                if (item.title() != null && !item.title().trim().isEmpty()) {
                    card.addView(text(context, item.title(), 19f, true), wrap());
                }
                if (item.text() != null && !item.text().trim().isEmpty()) {
                    card.addView(text(context, item.text(), 17f, false), wrap());
                }
            } else {
                Set<String> apps = new LinkedHashSet<>();
                for (BoopNotificationEnvelope item : presentation.cards()) {
                    if (item != null && !item.appLabel().trim().isEmpty()) {
                        apps.add(item.appLabel());
                    }
                }
                if (!apps.isEmpty()) {
                    card.addView(text(context, String.join(" • ", apps), 17f, false), wrap());
                }
            }
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        int margin = dp(context, 32);
        params.setMargins(margin, margin, margin, margin);
        root.addView(card, params);
        return root;
    }

    private static String appLabel(BoopNotificationPresentation presentation) {
        if (presentation == null || presentation.cards().isEmpty()) return "Notification";
        String label = presentation.cards().get(0).appLabel();
        return label == null || label.trim().isEmpty() ? "Notification" : label;
    }

    private static TextView text(Context context, String value, float sizeSp, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(sizeSp);
        view.setGravity(Gravity.CENTER);
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }

    private static LinearLayout.LayoutParams wrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 8);
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
