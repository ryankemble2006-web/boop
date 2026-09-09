package com.boop.alpha1;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

final class BoopDeviceProfile {
    enum Mode { WALL, LAUNCHER, SHIELD }

    private static final String PREFS = "boop_unified";
    private static final String KEY_OVERRIDE = "device_profile_override";
    private static final int TABLET_MIN_SMALLEST_WIDTH_DP = 600;

    private BoopDeviceProfile() { }

    static Mode resolve(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String override = prefs.getString(KEY_OVERRIDE, null);
        boolean tv = isTelevision(context);
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;
        return resolve(tv, Build.MODEL, override, smallestScreenWidthDp);
    }

    static Mode resolve(boolean television, String model, String override) {
        return resolve(television, model, override, 0);
    }

    static Mode resolve(boolean television, String model, String override, int smallestScreenWidthDp) {
        Mode forced = parseOverride(override);
        if (forced != null) {
            return forced;
        }
        if (television) {
            return Mode.SHIELD;
        }
        String normalized = model == null ? "" : model.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("pixel 7 pro")) {
            return Mode.WALL;
        }
        if (smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP) {
            return Mode.WALL;
        }
        return Mode.LAUNCHER;
    }

    static void setOverride(Context context, Mode mode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        if (mode == null) {
            editor.remove(KEY_OVERRIDE);
        } else {
            editor.putString(KEY_OVERRIDE, mode.name());
        }
        editor.apply();
    }

    private static Mode parseOverride(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Mode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isTelevision(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                || pm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
            return true;
        }
        UiModeManager uiMode = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiMode != null
                && uiMode.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }
}
