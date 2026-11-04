package com.boop.alpha1;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

final class BoopNotificationPermissionState {
    private BoopNotificationPermissionState() { }

    static boolean hasListenerAccess(Context context) {
        if (context == null) return false;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return false;
        ComponentName listener = new ComponentName(context, BoopNotificationListenerService.class);
        return manager.isNotificationListenerAccessGranted(listener);
    }

    static boolean hasOverlayAccess(Context context) {
        return context != null && Settings.canDrawOverlays(context);
    }

    static Intent notificationListenerSettingsIntent() {
        return new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
    }

    static Intent overlaySettingsIntent(Context context) {
        if (context == null) throw new IllegalArgumentException("context required");
        return new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
    }
}
