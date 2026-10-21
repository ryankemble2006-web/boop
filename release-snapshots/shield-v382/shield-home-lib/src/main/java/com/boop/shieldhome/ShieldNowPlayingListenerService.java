package com.boop.shieldhome;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Minimal Android authority bridge for media-session access.
 *
 * BOOP uses Notification Listener access to query active MediaSessions. Notification text, body,
 * actions and messages are ignored. For media notifications only, artwork may be extracted as a
 * fallback when the MediaSession itself supplies no artwork.
 */
public final class ShieldNowPlayingListenerService extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        StatusBarNotification[] active = getActiveNotifications();
        if (active != null) {
            for (StatusBarNotification existing : active) {
                onNotificationPosted(existing);
            }
        }
        ShieldNowPlayingManager.get(this).onListenerConnected();
    }

    @Override
    public void onListenerDisconnected() {
        ShieldNowPlayingManager.get(this).onListenerDisconnected();
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (!isMediaNotification(statusBarNotification)) {
            return;
        }
        Notification notification = statusBarNotification.getNotification();
        ShieldNowPlayingManager.get(this).onNotificationContentIntent(
                statusBarNotification.getPackageName(), notification.contentIntent);
        Bitmap artwork = artworkFrom(notification);
        if (artwork != null) {
            ShieldNowPlayingManager.get(this).onNotificationArtwork(
                    statusBarNotification.getPackageName(), artwork);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        if (!isMediaNotification(statusBarNotification)) {
            return;
        }
        ShieldNowPlayingManager.get(this).onNotificationContentIntent(
                statusBarNotification.getPackageName(), null);
        ShieldNowPlayingManager.get(this).onNotificationArtworkRemoved(
                statusBarNotification.getPackageName());
    }

    private static boolean isMediaNotification(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null || statusBarNotification.getNotification() == null) {
            return false;
        }
        Notification notification = statusBarNotification.getNotification();
        boolean hasMediaSession = notification.extras != null
                && notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION);
        return NowPlayingArtworkSourcePolicy.isMediaNotification(
                hasMediaSession, notification.category);
    }

    private Bitmap artworkFrom(Notification notification) {
        if (notification == null) {
            return null;
        }

        Bitmap fromLargeIcon = bitmapFrom(notification.getLargeIcon());
        if (fromLargeIcon != null) {
            return fromLargeIcon;
        }

        @SuppressWarnings("deprecation")
        Bitmap legacyLargeIcon = notification.largeIcon;
        if (legacyLargeIcon != null) {
            return legacyLargeIcon;
        }

        if (notification.extras == null) {
            return null;
        }
        for (String key : new String[] {
                Notification.EXTRA_LARGE_ICON_BIG,
                Notification.EXTRA_LARGE_ICON,
                Notification.EXTRA_PICTURE,
                Notification.EXTRA_PICTURE_ICON
        }) {
            Bitmap bitmap = bitmapFrom(notification.extras.get(key));
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    private Bitmap bitmapFrom(Object value) {
        if (value instanceof Bitmap) {
            return (Bitmap) value;
        }
        if (value instanceof Icon) {
            try {
                return bitmapFrom(((Icon) value).loadDrawable(this));
            } catch (RuntimeException unavailable) {
                return null;
            }
        }
        if (value instanceof Parcelable && value instanceof Drawable) {
            return bitmapFrom((Drawable) value);
        }
        if (value instanceof Drawable) {
            return bitmapFrom((Drawable) value);
        }
        return null;
    }

    private Bitmap bitmapFrom(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                return bitmap;
            }
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0) width = 512;
        if (height <= 0) height = 512;
        width = Math.min(width, 1024);
        height = Math.min(height, 1024);

        try {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        } catch (RuntimeException unavailable) {
            return null;
        }
    }
}
