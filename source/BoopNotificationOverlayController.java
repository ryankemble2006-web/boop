package com.boop.alpha1;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Toast;

final class BoopNotificationOverlayController implements BoopNotificationHost {
    private final Context context;
    private final BoopNotificationRuntime runtime;
    private final WindowManager windowManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable;
    private BoopNotificationPuppetView currentView;

    BoopNotificationOverlayController(Context context, BoopNotificationRuntime runtime) {
        if (context == null) throw new IllegalArgumentException("context required");
        if (runtime == null) throw new IllegalArgumentException("runtime required");
        this.context = context.getApplicationContext();
        this.runtime = runtime;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
        if (currentView != null && windowManager != null) {
            try {
                windowManager.removeViewImmediate(currentView);
            } catch (RuntimeException ignored) {
                // Already detached or Android reclaimed the window.
            }
        }
        currentView = null;
    }

    private void renderNew(BoopNotificationPresentation presentation, long timeoutMs) {
        hide();
        if (presentation == null || presentation.cards().isEmpty()) return;
        if (windowManager == null || !Settings.canDrawOverlays(context)) {
            runtime.onPresentationFailed(BoopNotificationSurface.OVERLAY);
            return;
        }

        BoopNotificationPuppetView view = new BoopNotificationPuppetView(
                context,
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
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        try {
            windowManager.addView(view, params);
            currentView = view;
            resetTimeout(timeoutMs);
        } catch (RuntimeException failure) {
            try {
                windowManager.removeViewImmediate(view);
            } catch (RuntimeException ignored) {
                // Nothing durable was attached.
            }
            currentView = null;
            runtime.onPresentationFailed(BoopNotificationSurface.OVERLAY);
        }
    }

    private void openSingle(String notificationKey) {
        BoopNotificationTapLauncher.Result result = runtime.openNotification(
                context, notificationKey);
        if (result == BoopNotificationTapLauncher.Result.OPENED) return;
        Toast.makeText(context, "Can't open that right now.", Toast.LENGTH_SHORT).show();
    }

    private void openBundle() {
        if (runtime.openInbox(context)) return;
        Toast.makeText(context, "Can't open that right now.", Toast.LENGTH_SHORT).show();
    }

    private void resetTimeout(long timeoutMs) {
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, Math.max(1L, timeoutMs));
    }

    private void dismissPresentation() {
        hide();
        runtime.onPresentationDismissed();
    }
}
