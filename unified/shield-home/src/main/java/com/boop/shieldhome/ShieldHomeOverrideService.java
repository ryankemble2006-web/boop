package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;

/**
 * No-ADB fallback for Shield firmware that pins Android TV Home as persistent HOME.
 *
 * The same already-enabled Accessibility service also accepts notification-state events for
 * Now Playing. It does not read notification text or actions: it extracts only Android's
 * MediaSession.Token from media-style notifications and hands that token to the media manager.
 */
public final class ShieldHomeOverrideService extends AccessibilityService {
    private static final long RELAUNCH_GUARD_MS = 350L;
    private long lastLaunchElapsed;

    @Override protected void onServiceConnected() {
        super.onServiceConnected();

        // Shield can present stock Home before Accessibility finishes binding after reboot.
        // Re-arm BOOP as soon as Android reconnects this already-enabled service, then let
        // the normal foreground-window path handle future Home presses.
        lastLaunchElapsed = SystemClock.elapsedRealtime();
        bringBoopToFront();
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        ShieldAccessibilityEventPolicy.Action action =
                ShieldAccessibilityEventPolicy.actionFor(event.getEventType());
        if (action == ShieldAccessibilityEventPolicy.Action.MEDIA_NOTIFICATION) {
            handleMediaNotification(event);
            return;
        }
        if (action != ShieldAccessibilityEventPolicy.Action.HOME_WINDOW) {
            return;
        }

        CharSequence packageName = event.getPackageName();
        String foregroundPackage = packageName == null ? null : packageName.toString();
        if (!HomeOverridePolicy.shouldReplaceForeground(foregroundPackage, getPackageName())) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastLaunchElapsed < RELAUNCH_GUARD_MS) {
            return;
        }
        lastLaunchElapsed = now;
        bringBoopToFront();
    }

    private void handleMediaNotification(AccessibilityEvent event) {
        Parcelable payload = event.getParcelableData();
        if (!(payload instanceof Notification)) {
            return;
        }
        MediaSession.Token token = mediaSessionToken((Notification) payload);
        if (token != null) {
            ShieldNowPlayingManager.get(this).onAccessibilityMediaSession(token);
        }
    }

    @SuppressWarnings("deprecation")
    private static MediaSession.Token mediaSessionToken(Notification notification) {
        if (notification == null || notification.extras == null) {
            return null;
        }
        try {
            Parcelable token = notification.extras.getParcelable(Notification.EXTRA_MEDIA_SESSION);
            return token instanceof MediaSession.Token ? (MediaSession.Token) token : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void bringBoopToFront() {
        Intent intent = new Intent(this, ShieldLauncherActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            // Fail closed. Android TV Home remains available as the recovery launcher.
        }
    }

    @Override public void onInterrupt() {
        // No continuous speech, gesture, or screen-reading work to interrupt.
    }
}
