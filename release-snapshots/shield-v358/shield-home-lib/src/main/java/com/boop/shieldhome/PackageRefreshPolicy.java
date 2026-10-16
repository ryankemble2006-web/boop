package com.boop.shieldhome;

/** Limits installed-app rescans to broadcasts that can actually change the catalogue. */
public final class PackageRefreshPolicy {
    private static final String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    private static final String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    private static final String PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED";

    private PackageRefreshPolicy() { }

    public static boolean shouldReload(String action) {
        return PACKAGE_ADDED.equals(action)
                || PACKAGE_REMOVED.equals(action)
                || PACKAGE_CHANGED.equals(action);
    }
}
