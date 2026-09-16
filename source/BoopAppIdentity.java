package com.boop.alpha1;

/** Installed application identity owns routing; saved profiles cannot change it. */
final class BoopAppIdentity {
    static final String WALL = "com.boop.alpha1";
    static final String SHIELD = "com.boop.shieldoverlay";

    private BoopAppIdentity() { }

    static boolean isShield(String packageName) {
        if (SHIELD.equals(packageName)) return true;
        if (WALL.equals(packageName)) return false;
        throw new IllegalArgumentException("Unknown BOOP application identity");
    }

    static String displayName(String packageName) {
        return isShield(packageName) ? "BOOP Shield" : "BOOP Wall";
    }
}
