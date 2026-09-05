package com.boop.shieldoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

public final class BoopOverlayService extends Service {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "boop_overlay_poc";

    private WindowManager windowManager;
    private BoopOverlayView overlayView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Settings.canDrawOverlays(this)) {
            removeOverlay();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        ensureOverlay();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        removeOverlay();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

        OverlayGeometry.Geometry geometry = currentGeometry();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                geometry.width(),
                geometry.height(),
                OverlayWindowSpec.type(),
                OverlayWindowSpec.flags(),
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = geometry.x();
        params.y = geometry.y();

        overlayView = new BoopOverlayView(this);
        windowManager.addView(overlayView, params);
        overlayView.post(overlayView::wakeOnce);
    }

    private OverlayGeometry.Geometry currentGeometry() {
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
        return OverlayGeometry.calculate(width, height);
    }

    private void removeOverlay() {
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
