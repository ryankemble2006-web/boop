package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Shared entry; each installed shell has one fixed body and its own fresh setup. */
public final class UnifiedEntryActivity extends Activity {
    private static final int REQ_NOTIFICATION_FIRST_RUN = 2402;
    private boolean waitingForNotificationSetup;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        route();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        route();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_NOTIFICATION_FIRST_RUN) {
            waitingForNotificationSetup = false;
            route();
        }
    }

    private void route() {
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
        if (!BoopSetupState.complete(this)) {
            startActivity(new Intent(this, BoopProfileActivity.class)
                    .putExtra("boop_first_setup", true));
            finish();
            return;
        }
        com.boop.shieldhome.BoopMediaBridge.configure(this, mode == BoopDeviceProfile.Mode.SHIELD);
        if (BoopNotificationRuntime.shouldInitializeForMode(mode)) {
            BoopNotificationRuntime.initialize(getApplication());
        }
        if (BoopNotificationStartupGate.resolve(mode, BoopNotificationOnboardingState.isSeen(this))
                == BoopNotificationStartupGate.Target.NOTIFICATION_ONBOARDING) {
            if (!waitingForNotificationSetup) {
                waitingForNotificationSetup = true;
                startActivityForResult(new Intent().setClassName(getPackageName(),
                        "com.boop.alpha1.BoopNotificationOnboardingActivity"), REQ_NOTIFICATION_FIRST_RUN);
            }
            return;
        }
        boolean homeIntent = getIntent() != null && getIntent().hasCategory(Intent.CATEGORY_HOME);
        ShieldEntryRoute.Target destination = ShieldEntryRoute.resolve(mode, homeIntent);
        startActivity(new Intent(Intent.ACTION_MAIN)
                .setClassName(getPackageName(), destination.className())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        if (destination.suppressEntryTransition()) overridePendingTransition(0, 0);
        finish();
    }
}
