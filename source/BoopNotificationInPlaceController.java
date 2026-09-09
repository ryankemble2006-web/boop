package com.boop.alpha1;

import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.Toast;

final class BoopNotificationInPlaceController implements BoopNotificationHost {
    private final FrameLayout parent;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable;
    private BoopNotificationPuppetView currentView;

    BoopNotificationInPlaceController(FrameLayout parent) {
        if (parent == null) throw new IllegalArgumentException("parent required");
        this.parent = parent;
        this.timeoutRunnable = this::dismissPresentation;
    }

    @Override
    public void show(BoopNotificationPresentation presentation, long timeoutMs) {
        renderNew(presentation, timeoutMs);
    }

    @Override
    public void update(BoopNotificationPresentation presentation, long timeoutMs) {
        if (presentation == null || presentation.cards().isEmpty()) {
            hide();
            return;
        }
        if (currentView == null) {
            renderNew(presentation, timeoutMs);
            return;
        }
        currentView.updatePresentation(presentation);
        resetTimeout(timeoutMs);
    }

    @Override
    public void hide() {
        handler.removeCallbacks(timeoutRunnable);
        if (currentView != null) {
            parent.removeView(currentView);
            currentView = null;
        }
    }

    private void renderNew(BoopNotificationPresentation presentation, long timeoutMs) {
        hide();
        if (presentation == null || presentation.cards().isEmpty()) return;
        currentView = new BoopNotificationPuppetView(
                parent.getContext(),
                presentation,
                new BoopNotificationPuppetView.Callback() {
                    @Override
                    public void onOpen(String notificationKey) {
                        openSingle(notificationKey);
                    }

                    @Override
                    public void onOpenBundle() {
                        openBundle();
                    }

                    @Override
                    public void onDismiss() {
                        dismissPresentation();
                    }
                });
        parent.addView(currentView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        resetTimeout(timeoutMs);
    }

    private void openSingle(String notificationKey) {
        try {
            BoopNotificationTapLauncher.Result result = BoopNotificationRuntime
                    .get(parent.getContext())
                    .openNotification(parent.getContext(), notificationKey);
            if (result == BoopNotificationTapLauncher.Result.OPENED) return;
        } catch (RuntimeException ignored) {
            // Fall through to the non-destructive user message.
        }
        Toast.makeText(parent.getContext(), "Can't open that right now.", Toast.LENGTH_SHORT).show();
    }

    private void openBundle() {
        try {
            if (BoopNotificationRuntime.get(parent.getContext()).openInbox(parent.getContext())) {
                return;
            }
        } catch (RuntimeException ignored) {
            // Fall through to the non-destructive user message.
        }
        Toast.makeText(parent.getContext(), "Can't open that right now.", Toast.LENGTH_SHORT).show();
    }

    private void resetTimeout(long timeoutMs) {
        handler.removeCallbacks(timeoutRunnable);
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
}
