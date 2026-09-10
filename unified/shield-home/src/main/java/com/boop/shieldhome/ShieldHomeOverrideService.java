package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.content.SharedPreferences;
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

        // Screen inspection can reconnect enabled accessibility services. Rearm once per
        // boot, not on every reconnect, so an unrelated foreground app keeps its place.
        int boot = Settings.Global.getInt(getContentResolver(), Settings.Global.BOOT_COUNT, -1);
        SharedPreferences prefs = getSharedPreferences("home-rearm", MODE_PRIVATE);
        if (HomeOverridePolicy.shouldRearmOnConnect(boot, prefs.getInt("boot", -1))) {
            prefs.edit().putInt("boot", boot).apply();
            lastLaunchElapsed = SystemClock.elapsedRealtime();
            bringBoopToFront();
        }
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
