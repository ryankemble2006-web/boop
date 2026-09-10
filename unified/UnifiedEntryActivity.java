package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public final class UnifiedEntryActivity extends Activity {
    private static final int REQ_NOTIFICATION_FIRST_RUN = 2402;

    private boolean waitingForNotificationSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        route();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        route();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_NOTIFICATION_FIRST_RUN) {
            waitingForNotificationSetup = false;
            route();
            return;
        }

    }

    private void route() {
        if (!getSharedPreferences("boop_unified", MODE_PRIVATE).getBoolean("profile_choice_seen", false)) {
            startActivity(new Intent(this, BoopProfileActivity.class)); finish(); return;
        }
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
        com.boop.shieldhome.BoopMediaBridge.configure(this, mode == BoopDeviceProfile.Mode.SHIELD);
        if (BoopNotificationRuntime.shouldInitializeForMode(mode)) {
            BoopNotificationRuntime.initialize(getApplication());
        }

        if (BoopNotificationStartupGate.resolve(
                mode,
                BoopNotificationOnboardingState.isSeen(this))
                == BoopNotificationStartupGate.Target.NOTIFICATION_ONBOARDING) {
            if (!waitingForNotificationSetup) {
                waitingForNotificationSetup = true;
                startActivityForResult(
                        new Intent().setClassName(
                                getPackageName(),
                                "com.boop.alpha1.BoopNotificationOnboardingActivity"),
                        REQ_NOTIFICATION_FIRST_RUN);
            }
            return;
        }

        boolean homeIntent = getIntent() != null
                && getIntent().hasCategory(Intent.CATEGORY_HOME);
        ShieldEntryRoute.Target destination = ShieldEntryRoute.resolve(mode, homeIntent);

        Intent targetIntent = new Intent(Intent.ACTION_MAIN)
                .setClassName(getPackageName(), destination.className())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(targetIntent);
        if (destination.suppressEntryTransition()) {
            overridePendingTransition(0, 0);
        }
        finish();
    }
}
