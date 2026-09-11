package com.boop.alpha1;

/** Maps existing BOOP runtime states to the canonical eye catalogue. */
public final class BoopCanonicalAnimationRouter {
    private BoopCanonicalAnimationRouter() { }

    public static String wake() { return "wake"; }
    public static String sleep() { return "sleep"; }
    public static String listening() { return "listening"; }
    public static String thinking() { return "thinking"; }
    public static String shake() { return "shake_reaction"; }
    public static String notification() { return "notification"; }

    public static String berry(int variant) {
        switch (Math.floorMod(variant, 3)) {
            case 0: return "berry_remember";
            case 1: return "berry_curious";
            default: return "berry_cheeky";
        }
    }
}
