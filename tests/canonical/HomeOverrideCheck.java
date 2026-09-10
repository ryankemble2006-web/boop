package com.boop.shieldhome;

public final class HomeOverrideCheck {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        check(!HomeOverridePolicy.shouldRearmOnConnect(false, 12, -1));
        check(HomeOverridePolicy.shouldRearmOnConnect(true, 12, -1));
        check(!HomeOverridePolicy.shouldRearmOnConnect(true, 12, 12));
        check(HomeOverridePolicy.shouldRearmOnConnect(true, 13, 12));
        check(!HomeOverridePolicy.shouldRearmOnConnect(true, -1, -1));
        for (String home : new String[]{"com.google.android.tvlauncher", "com.google.android.leanbacklauncher"}) {
            check(HomeOverridePolicy.shouldReplaceForeground(true, home, "com.boop.alpha1"));
            check(!HomeOverridePolicy.shouldReplaceForeground(false, home, "com.boop.alpha1"));
        }
        for (String other : new String[]{null, "", "com.boop.alpha1", "com.boop.shieldhome", "deezer.android.app", "com.google.android.apps.mediashell", "com.android.systemui", "com.android.tv.settings"}) {
            check(!HomeOverridePolicy.shouldReplaceForeground(true, other, "com.boop.alpha1"));
        }
        System.out.println("Home override: profile, reconnect, and foreground checks passed");
    }
}
