package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public final class UnifiedEntryActivity extends Activity {
    private static final int REQ_ASSISTANT_FIRST_RUN = 2400;
    private boolean waitingForAssistantChoice;

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
        if (requestCode == REQ_ASSISTANT_FIRST_RUN) {
            waitingForAssistantChoice = false;
            route();
        }
    }

    private void route() {
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
        if (mode == BoopDeviceProfile.Mode.SHIELD
                && BoopAssistantPreference.load(this) == null) {
            if (!waitingForAssistantChoice) {
                waitingForAssistantChoice = true;
                startActivityForResult(
                        new Intent().setClassName(getPackageName(),
                                "com.boop.alpha1.BoopAssistantSetupActivity"),
                        REQ_ASSISTANT_FIRST_RUN);
            }
            return;
        }

        String className;
        if (mode == BoopDeviceProfile.Mode.SHIELD) {
            className = "com.boop.shieldoverlay.MainActivity";
        } else if (mode == BoopDeviceProfile.Mode.WALL) {
            className = "com.boop.alpha1.MainActivity";
        } else {
            className = "com.boop.launcher.MainActivity";
        }

        Intent target = new Intent(Intent.ACTION_MAIN)
                .setClassName(getPackageName(), className)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(target);
        overridePendingTransition(0, 0);
        finish();
    }
}
