package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

final class BoopDeviceProfile {
    enum Mode { WALL, LAUNCHER, SHIELD }

    private static final String PREFS = "boop_unified";
    private static final String KEY_OVERRIDE = "device_profile_override";
    private static final int TABLET_MIN_SMALLEST_WIDTH_DP = 600;

    private BoopDeviceProfile() { }

    static Mode resolve(Context context) {
        // The shell owns the body even if historical profile preferences exist.
        return BoopAppIdentity.isShield(context.getPackageName()) ? Mode.SHIELD : Mode.WALL;
    }

    // Historical pure routing policy retained for old source tests and receipts.
    // The runtime Context entry above deliberately does not consult this policy.
    static Mode resolve(boolean television, String model, String override) {
        return resolve(television, model, override, 0);
    }

    static Mode resolve(boolean television, String model, String override, int smallestScreenWidthDp) {
        Mode forced = parseOverride(override);
        if (forced != null) return forced;
        if (television) return Mode.SHIELD;
        String normalized = model == null ? "" : model.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("pixel 7 pro")) return Mode.WALL;
        if (smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP) return Mode.WALL;
        return Mode.LAUNCHER;
    }

    static void setOverride(Context context, Mode mode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        if (mode == null) editor.remove(KEY_OVERRIDE);
        else editor.putString(KEY_OVERRIDE, mode.name());
        editor.apply();
    }

    private static Mode parseOverride(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Mode.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
