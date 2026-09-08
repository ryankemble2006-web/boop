package com.boop.shieldhome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Invisible Shield/Android TV settings router for firmware that does not expose
 * the generic android.settings.ACCESSIBILITY_SETTINGS intent.
 */
public final class ShieldAccessibilityRouteActivity extends Activity {
    static ComponentName preferredTvAccessibilityComponent() {
        return new ComponentName(
                "com.android.tv.settings",
                "com.android.tv.settings.system.AccessibilityActivity");
    }

    static ComponentName modernTvAccessibilityComponent() {
        return new ComponentName(
                "com.android.tv.settings",
                "com.android.tv.settings.oemlink.AccessibilitySettingsActivity");
    }

    static ComponentName tvSettingsFallbackComponent() {
        return new ComponentName(
                "com.android.tv.settings",
                "com.android.tv.settings.MainSettings");
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (tryStart(preferredTvAccessibilityComponent())
                || tryStart(modernTvAccessibilityComponent())
                || tryStart(tvSettingsFallbackComponent())) {
            finish();
            return;
        }

        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (ActivityNotFoundException | SecurityException ignored) {
            // Nothing else to route to. Finish silently rather than showing a resolver error.
        }
        finish();
    }

    private boolean tryStart(ComponentName component) {
        try {
            startActivity(new Intent().setComponent(component));
            return true;
        } catch (ActivityNotFoundException | SecurityException ignored) {
            return false;
        }
    }
}
