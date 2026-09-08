package com.boop.alpha1;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

final class BoopNotificationOverlayController implements BoopNotificationHost {
    private final Context context;
    private final BoopNotificationRuntime runtime;
    private final WindowManager windowManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable;
    private View currentView;

    BoopNotificationOverlayController(Context context, BoopNotificationRuntime runtime) {
        if (context == null) throw new IllegalArgumentException("context required");
        if (runtime == null) throw new IllegalArgumentException("runtime required");
        this.context = context.getApplicationContext();
        this.runtime = runtime;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.timeoutRunnable = () -> {
            hide();
            this.runtime.onPresentationDismissed();
        };
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
        if (currentView != null && windowManager != null) {
            try {
                windowManager.removeViewImmediate(currentView);
            } catch (RuntimeException ignored) {
                // Already detached or Android reclaimed the window.
            }
        }
        currentView = null;
    }

    private void render(BoopNotificationPresentation presentation, long timeoutMs) {
        hide();
        if (presentation == null || presentation.cards().isEmpty()) return;
        if (windowManager == null || !Settings.canDrawOverlays(context)) {
            runtime.onPresentationFailed(BoopNotificationSurface.OVERLAY);
            return;
        }

        View view = BoopNotificationInPlaceController.createPlainPresentationView(
                context, presentation);
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
            handler.postDelayed(timeoutRunnable, Math.max(1L, timeoutMs));
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
}
