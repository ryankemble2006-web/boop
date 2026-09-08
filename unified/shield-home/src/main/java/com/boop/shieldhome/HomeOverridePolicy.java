package com.boop.shieldhome;

/** Pure policy for the no-ADB Shield HOME accessibility override experiment. */
final class HomeOverridePolicy {
    private static final String ANDROID_TV_HOME = "com.google.android.tvlauncher";
    private static final String LEGACY_LEANBACK_HOME = "com.google.android.leanbacklauncher";

    private HomeOverridePolicy() {}

    static boolean shouldReplaceForeground(String foregroundPackage, String ownPackage) {
        if (foregroundPackage == null || foregroundPackage.isEmpty()) {
            return false;
        }
        if (ownPackage != null && foregroundPackage.equals(ownPackage)) {
            return false;
        }
        return ANDROID_TV_HOME.equals(foregroundPackage)
                || LEGACY_LEANBACK_HOME.equals(foregroundPackage);
    }
}
