package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public final class UnifiedEntryActivity extends Activity {
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

    private void route() {
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
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
