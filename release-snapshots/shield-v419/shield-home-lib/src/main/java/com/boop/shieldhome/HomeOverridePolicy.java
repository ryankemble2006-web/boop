package com.boop.shieldhome;

/** Pure policy for the no-ADB Shield HOME accessibility override experiment. */
final class HomeOverridePolicy {
    private static final String ANDROID_TV_HOME = "com.google.android.tvlauncher";
    private static final String LEGACY_LEANBACK_HOME = "com.google.android.leanbacklauncher";

    private HomeOverridePolicy() {}

    static boolean shouldRearmOnConnect(boolean shieldProfile, int currentBoot, int lastRearmedBoot) {
        return shieldProfile && shouldRearmOnConnect(currentBoot, lastRearmedBoot);
    }

    static boolean shouldReplaceForeground(boolean shieldProfile, String foregroundPackage, String ownPackage) {
        return shieldProfile && shouldReplaceForeground(foregroundPackage, ownPackage);
    }

    static boolean shouldRearmOnConnect(int currentBoot, int lastRearmedBoot) {
        return currentBoot >= 0 && currentBoot != lastRearmedBoot;
    }

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
