package com.boop.shieldhome;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Minimal Android authority bridge for media-session access.
 *
 * Notification content is deliberately ignored. BOOP only uses the listener grant to query active
 * MediaSessions through MediaSessionManager.
 */
public final class ShieldNowPlayingListenerService extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        ShieldNowPlayingManager.get(this).onListenerConnected();
    }

    @Override
    public void onListenerDisconnected() {
        ShieldNowPlayingManager.get(this).onListenerDisconnected();
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        // Intentionally ignored: notification payloads are not read, stored, or cancelled.
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        // Intentionally ignored: media state comes only from Android MediaSessions.
    }
}
