package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;

/**
 * No-ADB fallback for Shield firmware that pins Android TV Home as persistent HOME.
 * Watches only for the stock HOME window and immediately brings BOOP to front.
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
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
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
