package com.boop.shieldoverlay;

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

        LaunchCrashRecorder crashRecorder = new LaunchCrashRecorder(this);
        String crashReport = crashRecorder.consume();
        if (crashReport != null) {
            Intent reportIntent = new Intent(this, CrashReportActivity.class);
            reportIntent.putExtra(CrashReportActivity.EXTRA_REPORT, crashReport);
            startActivity(reportIntent);
            finish();
            return;
        }

        if (Settings.canDrawOverlays(this)) {
            startOverlayAndOpenHome();
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

        permissionScreenLaunched = false;
        leftForPermissionScreen = false;
        if (Settings.canDrawOverlays(this)) {
            startOverlayAndOpenHome();
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

    private void startOverlayAndOpenHome() {
        startForegroundService(new Intent(this, BoopOverlayService.class));
        startActivity(new Intent(this, BoopHomeActivity.class));
        finish();
    }
}
