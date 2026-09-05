package com.boop.shieldhdrdebug;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public final class MainActivity extends Activity {
    private boolean permissionScreenLaunched;
    private boolean leftForPermissionScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Settings.canDrawOverlays(this)) {
            startDebugAndFinish();
            return;
        }
        launchOverlayPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (permissionScreenLaunched) {
            leftForPermissionScreen = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!permissionScreenLaunched || !leftForPermissionScreen) {
            return;
        }
        if (Settings.canDrawOverlays(this)) {
            startDebugAndFinish();
        } else {
            finish();
        }
    }

    private void launchOverlayPermission() {
        permissionScreenLaunched = true;
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void startDebugAndFinish() {
        Intent service = new Intent(this, BoopHdrDebugService.class)
                .putExtra(BoopHdrDebugService.EXTRA_MANUAL_LAUNCH, true);
        startForegroundService(service);
        finish();
    }
}
