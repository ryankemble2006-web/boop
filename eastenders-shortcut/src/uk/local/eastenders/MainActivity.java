package uk.local.eastenders;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/** A launch-only shortcut: no obsolete selector or recovery screen on the back stack. */
public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingDeepLink;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) openProgramme();
        else finish();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        clearPending();
        openProgramme();
    }

    private void clearPending() {
        if (pendingDeepLink != null) handler.removeCallbacks(pendingDeepLink);
        pendingDeepLink = null;
    }

    @Override protected void onDestroy() {
        clearPending();
        // The accessibility helper owns the playback session after this activity exits.
        super.onDestroy();
    }

    private void openProgramme() {
        WatchNowService.cancel();
        if (!WatchNowService.ready()) {
            Toast.makeText(this, "Enable EastEnders auto-play in Shield Accessibility settings for automatic playback.", Toast.LENGTH_LONG).show();
        }
        Routes.open(this::openApp, this::unavailable);
    }

    private boolean openApp(String packageName, String url) {
        Intent deepLink = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(packageName);
        deepLink.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (getPackageManager().resolveActivity(deepLink, 0) == null) return false;
        WatchNowService.arm(packageName);
        long delay = LaunchPolicy.prewarmDelayMs(packageName);
        Intent warm = getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
        if (delay > 0 && warm != null && launch(warm)) {
            pendingDeepLink = () -> {
                pendingDeepLink = null;
                if (!launch(deepLink)) {
                    WatchNowService.cancel();
                    unavailable();
                } else finish();
            };
            handler.postDelayed(pendingDeepLink, delay);
            return true;
        }
        if (launch(deepLink)) {
            finish();
            return true;
        }
        WatchNowService.cancel();
        return false;
    }

    private boolean launch(Intent intent) {
        try { startActivity(intent); return true; }
        catch (ActivityNotFoundException | SecurityException rejected) { return false; }
    }

    private boolean unavailable() {
        WatchNowService.cancel();
        Toast.makeText(this, "BBC iPlayer could not open. Check that the official app is installed.", Toast.LENGTH_LONG).show();
        finish();
        return false;
    }
}
