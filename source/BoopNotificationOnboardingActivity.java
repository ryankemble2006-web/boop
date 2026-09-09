package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

public final class BoopNotificationOnboardingActivity extends Activity {
    private static final int REQ_NOTIFICATION_ACCESS = 2501;
    private static final int REQ_OVERLAY_ACCESS = 2502;

    private static final String STATE_PHASE = "phase";
    private static final String STATE_LISTENER_GRANTED = "listener_granted";
    private static final String STATE_OVERLAY_GRANTED = "overlay_granted";

    private enum Phase { INTRO, WAITING_LISTENER_SETTINGS, WAITING_OVERLAY_SETTINGS, DONE }

    private Phase phase = Phase.INTRO;
    private boolean listenerGranted;
    private boolean overlayGranted;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            String savedPhase = state.getString(STATE_PHASE, Phase.INTRO.name());
            try {
                phase = Phase.valueOf(savedPhase);
            } catch (IllegalArgumentException ignored) {
                phase = Phase.INTRO;
            }
            listenerGranted = state.getBoolean(STATE_LISTENER_GRANTED, false);
            overlayGranted = state.getBoolean(STATE_OVERLAY_GRANTED, false);
        }
        if (phase == Phase.INTRO) {
            showIntro();
        } else if (phase == Phase.DONE) {
            finishNormally();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_PHASE, phase.name());
        outState.putBoolean(STATE_LISTENER_GRANTED, listenerGranted);
        outState.putBoolean(STATE_OVERLAY_GRANTED, overlayGranted);
        super.onSaveInstanceState(outState);
    }

    private void showIntro() {
        new AlertDialog.Builder(this)
                .setTitle("BOOP notifications")
                .setMessage("BOOP can show selected notifications on the Wall, lock screen and over other apps. Android will ask separately for Notification Access and Display over other apps. Nothing is enabled automatically.")
                .setPositiveButton("Set up", (dialog, which) -> launchNotificationAccess())
                .setNegativeButton("Not now", (dialog, which) -> skipSetup())
                .setOnCancelListener(dialog -> skipSetup())
                .show();
    }

    private void launchNotificationAccess() {
        phase = Phase.WAITING_LISTENER_SETTINGS;
        try {
            startActivityForResult(
                    BoopNotificationPermissionState.notificationListenerSettingsIntent(),
                    REQ_NOTIFICATION_ACCESS);
        } catch (RuntimeException unavailable) {
            listenerGranted = false;
            launchOverlayAccess();
        }
    }

    private void launchOverlayAccess() {
        phase = Phase.WAITING_OVERLAY_SETTINGS;
        try {
            startActivityForResult(
                    BoopNotificationPermissionState.overlaySettingsIntent(this),
                    REQ_OVERLAY_ACCESS);
        } catch (RuntimeException unavailable) {
            try {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                        REQ_OVERLAY_ACCESS);
            } catch (RuntimeException alsoUnavailable) {
                overlayGranted = false;
                finishNormally();
            }
        }
    }

    private void skipSetup() {
        phase = Phase.DONE;
        BoopNotificationOnboardingState.markSeen(this);
        setResult(RESULT_CANCELED);
        finish();
    }

    private void finishNormally() {
        phase = Phase.DONE;
        BoopNotificationOnboardingState.markSeen(this);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_NOTIFICATION_ACCESS) {
            listenerGranted = BoopNotificationPermissionState.hasListenerAccess(this);
            launchOverlayAccess();
            return;
        }
        if (requestCode == REQ_OVERLAY_ACCESS) {
            overlayGranted = BoopNotificationPermissionState.hasOverlayAccess(this);
            finishNormally();
        }
    }
}
