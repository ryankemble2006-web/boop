package uk.local.eastenders;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final String TAG = "EastEnders";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        selfHealAccessibility();
        armAndLaunch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        selfHealAccessibility();
        armAndLaunch();
    }

    private void selfHealAccessibility() {
        try {
            if (checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED) {
                String existing = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                Set<String> set = new LinkedHashSet<>();
                if (existing != null) {
                    for (String s : existing.split(":")) {
                        if (!s.trim().isEmpty()) set.add(s.trim());
                    }
                }
                set.add("uk.local.eastenders/uk.local.eastenders.EastEndersAccessibilityService");
                set.add("uk.local.eastenders/uk.local.eastenders.WatchNowService");
                Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, TextUtils.join(":", set));
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
                Log.i(TAG, "Self-healed accessibility service registration via WRITE_SECURE_SETTINGS");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Could not self-heal accessibility settings: " + t.getMessage());
        }
    }

    private void armAndLaunch() {
        boolean enabled = WatchNowService.isServiceEnabled(this);

        if (!enabled) {
            // First run on a new TV without accessibility turned on yet: prompt native TV settings once
            Log.w(TAG, "Accessibility not enabled yet on this device. Prompting native TV settings.");
            try {
                Toast.makeText(this, "Turn ON 'EastEnders auto-play' in Accessibility for 1-click playback", Toast.LENGTH_LONG).show();
                Intent a11yIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a11yIntent);
            } catch (Throwable ignored) { }
        }

        // Arm the automation engine
        WatchNowService.arm("com.nvidia.bbciplayer");

        // Immediately launch BBC iPlayer directly to the EastEnders programme page
        openProgramme();

        // Finish immediately — zero custom menus, zero extra clicks
        finish();
    }

    private void openProgramme() {
        Intent player = new Intent(Intent.ACTION_VIEW, Uri.parse(Routes.TV_URL))
                .setPackage("com.nvidia.bbciplayer")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (getPackageManager().resolveActivity(player, 0) != null) {
            try {
                startActivity(player);
                return;
            } catch (ActivityNotFoundException | SecurityException ignored) { }
        }

        // Fallback for devices running generic BBC iPlayer packages (Sony TV, Google TV, Fire TV)
        boolean launched = Routes.open(
                (pkg, url) -> {
                    Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .setPackage(pkg)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (getPackageManager().resolveActivity(fallback, 0) == null) return false;
                    WatchNowService.arm(pkg);
                    try {
                        startActivity(fallback);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                },
                () -> {
                    Toast.makeText(this, "BBC iPlayer is not installed.", Toast.LENGTH_LONG).show();
                    return false;
                }
        );

        if (!launched) {
            WatchNowService.cancel();
        }
    }
}
