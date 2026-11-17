package com.boop.alpha1;

/** BOOP motion is independent of Android's global animator scale. */
final class BoopMotionPolicy {
    private BoopMotionPolicy() { }

    static boolean shouldAnimate(boolean powerSaveMode) {
        return !powerSaveMode;
    }
}