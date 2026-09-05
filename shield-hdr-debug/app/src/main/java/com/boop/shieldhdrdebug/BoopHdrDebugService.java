package com.boop.shieldhdrdebug;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.hardware.display.DisplayManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public final class BoopHdrDebugService extends Service {
    static final String EXTRA_MANUAL_LAUNCH = "manual_launch";

    private static final int NOTIFICATION_ID = 1101;
    private static final String CHANNEL_ID = "boop_hdr_debug";
    private static final long HEARTBEAT_INTERVAL_MS = 1000L;

    private final Handler heartbeatHandler = new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private DisplayManager displayManager;
    private DiagnosticJournal journal;
    private BoopEyeView eyesView;
    private DiagnosticPanelView panelView;
    private DisplaySnapshot lastDisplaySnapshot;
    private Boolean lastEyesAttached;
    private Boolean lastPanelAttached;
    private DisplayManager.DisplayListener displayListener;
    private Thread.UncaughtExceptionHandler previousCrashHandler;
    private Thread.UncaughtExceptionHandler debugCrashHandler;

    private final Runnable heartbeat = new Runnable() {
        @Override
        public void run() {
            journal.heartbeat();
            boolean eyesAttached = eyesView != null && eyesView.isAttachedToWindow();
            boolean panelAttached = panelView != null && panelView.isAttachedToWindow();
            if (lastEyesAttached == null || lastEyesAttached != eyesAttached
                    || lastPanelAttached == null || lastPanelAttached != panelAttached) {
                journal.appendEvent("HEARTBEAT overlay eyes=" + eyesAttached + " panel=" + panelAttached);
                lastEyesAttached = eyesAttached;
                lastPanelAttached = panelAttached;
            }

            DisplaySnapshot now = safeDisplaySnapshot();
            if (lastDisplaySnapshot == null || !lastDisplaySnapshot.signature().equals(now.signature())) {
                journal.appendEvent("HEARTBEAT DISPLAY change " + now.compact());
                lastDisplaySnapshot = now;
            }
            if (panelView != null) {
                panelView.update(now, eyesAttached, panelAttached);
            }
            heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        journal = new DiagnosticJournal(this);
        journal.beginSession(Process.myPid());
        journal.appendEvent("SERVICE onCreate");
        installCrashRecorder();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        promoteToForeground();
        registerDisplayDiagnostics();
        lastDisplaySnapshot = safeDisplaySnapshot();
        journal.appendEvent("INITIAL DISPLAY " + lastDisplaySnapshot.compact());
        heartbeatHandler.post(heartbeat);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean manualLaunch = intent != null && intent.getBooleanExtra(EXTRA_MANUAL_LAUNCH, false);
        journal.appendEvent("SERVICE onStartCommand id=" + startId + " flags=" + flags
                + " manual=" + manualLaunch + " intentNull=" + (intent == null));

        if (!Settings.canDrawOverlays(this)) {
            journal.appendEvent("OVERLAY permission missing - service stopping");
            removeOverlays("permission missing");
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (manualLaunch && (eyesView != null || panelView != null)) {
            journal.appendEvent("MANUAL RELAUNCH forcing window rebuild");
            removeOverlays("manual relaunch");
        }
        ensureOverlays();
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        journal.appendEvent("CONFIG changed orientation=" + newConfig.orientation
                + " uiMode=" + newConfig.uiMode + " density=" + newConfig.densityDpi);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        journal.appendEvent("SERVICE onTrimMemory level=" + level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        journal.appendEvent("SERVICE onLowMemory");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        journal.appendEvent("SERVICE onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        if (journal != null) journal.appendEvent("SERVICE onDestroy");
        heartbeatHandler.removeCallbacksAndMessages(null);
        if (displayManager != null && displayListener != null) {
            displayManager.unregisterDisplayListener(displayListener);
        }
        removeOverlays("service destroy");
        if (debugCrashHandler != null && Thread.getDefaultUncaughtExceptionHandler() == debugCrashHandler) {
            Thread.setDefaultUncaughtExceptionHandler(previousCrashHandler);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerDisplayDiagnostics() {
        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                journal.appendEvent("DISPLAY added id=" + displayId + " " + safeDisplaySnapshot().compact());
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                journal.appendEvent("DISPLAY removed id=" + displayId);
            }

            @Override
            public void onDisplayChanged(int displayId) {
                DisplaySnapshot now = safeDisplaySnapshot();
                journal.appendEvent("DISPLAY onDisplayChanged id=" + displayId + " " + now.compact());
                lastDisplaySnapshot = now;
                updatePanel();
            }
        };
        displayManager.registerDisplayListener(displayListener, heartbeatHandler);
        journal.appendEvent("DISPLAY listener registered");
    }

    private void installCrashRecorder() {
        previousCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        debugCrashHandler = (thread, throwable) -> {
            try {
                journal.appendEvent("UNCAUGHT " + throwable.getClass().getSimpleName()
                        + " thread=" + thread.getName() + " msg=" + String.valueOf(throwable.getMessage()));
            } catch (Throwable ignored) {
                // Preserve the original crash even if recording itself fails.
            }
            if (previousCrashHandler != null) {
                previousCrashHandler.uncaughtException(thread, throwable);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(debugCrashHandler);
        journal.appendEvent("CRASH recorder installed");
    }

    private void promoteToForeground() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_boop_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        journal.appendEvent("FOREGROUND promoted specialUse="
                + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE));
    }

    private void ensureOverlays() {
        OverlayGeometry.Geometry eyes = OverlayGeometry.eyes(displayWidth(), displayHeight());
        OverlayGeometry.Geometry panel = OverlayGeometry.panel(displayWidth(), displayHeight());

        if (eyesView == null) {
            eyesView = new BoopEyeView(this);
            eyesView.addOnAttachStateChangeListener(attachmentListener("EYES"));
            WindowManager.LayoutParams params = overlayParams(eyes.width, eyes.height);
            params.gravity = Gravity.TOP | Gravity.END;
            params.x = eyes.x;
            params.y = eyes.y;
            try {
                windowManager.addView(eyesView, params);
                journal.appendEvent("OVERLAY eyes addView requested " + eyes.width + "x" + eyes.height);
                eyesView.post(eyesView::wakeOnce);
            } catch (RuntimeException failure) {
                journal.appendEvent("OVERLAY eyes addView FAILED " + failure.getClass().getSimpleName());
                eyesView = null;
            }
        }

        if (panelView == null) {
            panelView = new DiagnosticPanelView(this, journal);
            panelView.addOnAttachStateChangeListener(attachmentListener("PANEL"));
            WindowManager.LayoutParams params = overlayParams(panel.width, panel.height);
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = panel.x;
            params.y = panel.y;
            try {
                windowManager.addView(panelView, params);
                journal.appendEvent("OVERLAY panel addView requested " + panel.width + "x" + panel.height);
            } catch (RuntimeException failure) {
                journal.appendEvent("OVERLAY panel addView FAILED " + failure.getClass().getSimpleName());
                panelView = null;
            }
        }
        updatePanel();
    }

    private View.OnAttachStateChangeListener attachmentListener(String name) {
        return new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                journal.appendEvent("OVERLAY " + name + " ATTACHED token=" + view.getWindowToken());
                updatePanel();
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                journal.appendEvent("OVERLAY " + name + " DETACHED");
                updatePanel();
            }
        };
    }

    private WindowManager.LayoutParams overlayParams(int width, int height) {
        return new WindowManager.LayoutParams(
                width,
                height,
                OverlayWindowSpec.type(),
                OverlayWindowSpec.flags(),
                PixelFormat.TRANSLUCENT);
    }

    private void removeOverlays(String reason) {
        if (journal != null) journal.appendEvent("OVERLAY remove reason=" + reason);
        removeOne(panelView, "PANEL");
        panelView = null;
        removeOne(eyesView, "EYES");
        eyesView = null;
        lastEyesAttached = null;
        lastPanelAttached = null;
    }

    private void removeOne(View view, String name) {
        if (view == null || windowManager == null) return;
        try {
            windowManager.removeView(view);
            journal.appendEvent("OVERLAY " + name + " removeView requested");
        } catch (IllegalArgumentException failure) {
            journal.appendEvent("OVERLAY " + name + " removeView already detached");
        }
    }

    private void updatePanel() {
        if (panelView == null) return;
        DisplaySnapshot snapshot = safeDisplaySnapshot();
        panelView.update(snapshot,
                eyesView != null && eyesView.isAttachedToWindow(),
                panelView.isAttachedToWindow());
    }

    private DisplaySnapshot safeDisplaySnapshot() {
        try {
            return DisplaySnapshot.capture(windowManager);
        } catch (RuntimeException failure) {
            journal.appendEvent("DISPLAY snapshot FAILED " + failure.getClass().getSimpleName());
            return new DisplaySnapshotForFailure().snapshot();
        }
    }

    private int displayWidth() {
        return windowManager.getDefaultDisplay().getMode().getPhysicalWidth();
    }

    private int displayHeight() {
        return windowManager.getDefaultDisplay().getMode().getPhysicalHeight();
    }

    private static final class DisplaySnapshotForFailure {
        DisplaySnapshot snapshot() {
            return DisplaySnapshotFailureFactory.create();
        }
    }
}
