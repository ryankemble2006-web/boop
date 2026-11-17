package com.boop.shieldoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public final class BoopOverlayService extends Service {
    public static final String ACTION_HIDE_EYES =
            "com.boop.shieldoverlay.action.HIDE_EYES";
    public static final String ACTION_SHOW_EYES =
            "com.boop.shieldoverlay.action.SHOW_EYES";

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "boop_overlay_poc";

    private WindowManager windowManager;
    private DisplayManager displayManager;
    private DisplayManager.DisplayListener displayListener;
    private BoopOverlayView overlayView;
    private MediaPuppetState.Snapshot puppetSnapshot;
    private Runnable unsubscribePuppet;
    private ContentObserver animationObserver;
    private boolean animationObserverRegistered;
    private BroadcastReceiver powerSaveReceiver;
    private boolean powerSaveReceiverRegistered;
    private WindowManager.LayoutParams overlayParams;
    private int layoutDisplayWidth = -1;
    private int layoutDisplayHeight = -1;
    private boolean layoutHeadphones;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        registerDisplayListener();
        registerAnimationObserver();
        registerPowerSaveReceiver();
        unsubscribePuppet = DeezerPuppetAccess.get(this).state().subscribe(snapshot -> {
            puppetSnapshot = snapshot;
            if (overlayView != null) {
                overlayView.setPuppetSnapshot(snapshot);
                updateOverlayLayout();
            }
        });
        promoteToForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Settings.canDrawOverlays(this)) {
            removeOverlay();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent == null ? null : intent.getAction();
        if (ACTION_HIDE_EYES.equals(action)) {
            if (overlayView != null) {
                overlayView.setVisibility(View.GONE);
            }
            return START_STICKY;
        }

        if (ACTION_SHOW_EYES.equals(action)) {
            ensureOverlay();
            if (overlayView != null) {
                updateDisplayState();
                overlayView.setVisibility(View.VISIBLE);
                if (isDisplayActive()) {
                    overlayView.postInvalidateOnAnimation();
                }
            }
            return START_STICKY;
        }

        ensureOverlay();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (unsubscribePuppet != null) {
            unsubscribePuppet.run();
            unsubscribePuppet = null;
        }
        if (animationObserverRegistered) {
            getContentResolver().unregisterContentObserver(animationObserver);
            animationObserverRegistered = false;
        }
        if (powerSaveReceiverRegistered) {
            try {
                unregisterReceiver(powerSaveReceiver);
            } catch (IllegalArgumentException ignored) {
                // The receiver was already removed by the system.
            }
            powerSaveReceiverRegistered = false;
        }
        powerSaveReceiver = null;
        if (displayManager != null && displayListener != null) {
            displayManager.unregisterDisplayListener(displayListener);
        }
        removeOverlay();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerDisplayListener() {
        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                // No action needed for the single-display Shield POC.
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                // No action needed for the single-display Shield POC.
            }

            @Override
            public void onDisplayChanged(int displayId) {
                if (overlayView != null) {
                    updateDisplayState();
                    updateOverlayLayout();
                    if (overlayView.isShown() && isDisplayActive()) {
                        overlayView.postInvalidateOnAnimation();
                    }
                }
            }
        };
        displayManager.registerDisplayListener(displayListener, null);
    }

    private void registerAnimationObserver() {
        animationObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                if (overlayView != null) {
                    overlayView.refreshAnimationPreference();
                }
            }
        };
        try {
            getContentResolver().registerContentObserver(
                    Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
                    false, animationObserver);
            animationObserverRegistered = true;
        } catch (SecurityException ignored) {
            // The view holds still if animator-setting changes cannot be observed.
        }
    }

    private boolean isDisplayActive() {
        Display display = windowManager == null ? null : windowManager.getDefaultDisplay();
        return display != null && display.getState() == Display.STATE_ON;
    }

    private void registerPowerSaveReceiver() {
        powerSaveReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())
                        && overlayView != null) {
                    overlayView.refreshAnimationPreference();
                }
            }
        };
        IntentFilter filter = new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(powerSaveReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(powerSaveReceiver, filter);
            }
            powerSaveReceiverRegistered = true;
        } catch (SecurityException | IllegalArgumentException unavailable) {
            // The view holds still if power changes cannot be observed safely.
            powerSaveReceiver = null;
        }
    }

    private void updateDisplayState() {
        if (overlayView != null) {
            overlayView.setDisplayActive(isDisplayActive());
            overlayView.refreshAnimationPreference();
        }
    }

    private void promoteToForeground() {
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));
        manager.createNotificationChannel(channel);

        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_boop_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }

    private void ensureOverlay() {
        if (overlayView != null || windowManager == null) {
            return;
        }

        overlayParams = new WindowManager.LayoutParams(
                1,
                1,
                OverlayWindowSpec.type(),
                OverlayWindowSpec.flags(),
                PixelFormat.TRANSLUCENT);
        overlayParams.gravity = Gravity.TOP | Gravity.END;

        overlayView = new BoopOverlayView(this);
        overlayView.setAnimationObservationAvailable(animationObserverRegistered);
        overlayView.setPowerObservationAvailable(powerSaveReceiverRegistered);
        if (puppetSnapshot != null) {
            overlayView.setPuppetSnapshot(puppetSnapshot);
        }
        updateDisplayState();
        applyCurrentGeometry();
        windowManager.addView(overlayView, overlayParams);
        overlayView.post(overlayView::wakeOnce);
    }

    private void updateOverlayLayout() {
        if (overlayView != null && overlayParams != null && applyCurrentGeometry()) {
            windowManager.updateViewLayout(overlayView, overlayParams);
        }
    }

    private boolean applyCurrentGeometry() {
        int width;
        int height;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
            width = bounds.width();
            height = bounds.height();
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            width = metrics.widthPixels;
            height = metrics.heightPixels;
        }
        boolean headphones = puppetSnapshot != null
                && puppetSnapshot.mode != DeezerPuppetPolicy.Mode.EYES;
        if (width == layoutDisplayWidth && height == layoutDisplayHeight
                && headphones == layoutHeadphones) {
            return false;
        }
        layoutDisplayWidth = width;
        layoutDisplayHeight = height;
        layoutHeadphones = headphones;
        if (headphones) {
            // Deezer music-puppet mode owns the entire picture. The window flags remain
            // non-focusable/non-touchable, so this is a visual curtain rather than a new foreground UI.
            HeadphoneGeometry.Layout layout = FullscreenDeezerGeometry.calculate(width, height);
            overlayParams.width = width;
            overlayParams.height = height;
            overlayParams.x = 0;
            overlayParams.y = 0;
            overlayView.setHeadphoneLayout(layout);
        } else {
            OverlayGeometry.Geometry geometry = OverlayGeometry.calculate(width, height);
            overlayParams.width = geometry.width();
            overlayParams.height = geometry.height();
            overlayParams.x = geometry.x();
            overlayParams.y = geometry.y();
        }
        return true;
    }

    private void removeOverlay() {
        overlayParams = null;
        layoutDisplayWidth = -1;
        layoutDisplayHeight = -1;
        if (overlayView == null || windowManager == null) {
            overlayView = null;
            return;
        }
        try {
            windowManager.removeView(overlayView);
        } catch (IllegalArgumentException ignored) {
            // View was already detached by the system.
        } finally {
            overlayView = null;
        }
    }
}
