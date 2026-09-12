package com.boop.alpha1;

/** Stable production mappings into the finished canonical animation catalogue. */
public final class BoopCanonicalAnimationRouter {
    private BoopCanonicalAnimationRouter() { }

    public static String idle() { return "idle"; }
    public static String blink() { return "blink"; }
    public static String doubleBlink() { return "double_blink"; }
    public static String wake() { return "wake"; }
    public static String sleep() { return "sleep"; }
    public static String listening() { return "listening"; }
    public static String reading() { return "reading"; }
    public static String thinking() { return "thinking"; }
    public static String music() { return "music"; }
    public static String mediaPause() { return "media_pause"; }
    public static String trackChange() { return "track_change"; }
    public static String cinema() { return "cinema"; }
    public static String shake() { return "shake_reaction"; }
    public static String curious() { return "curious"; }
    public static String skeptical() { return "skeptical"; }
    public static String success() { return "success"; }
    public static String confused() { return "confused"; }
    public static String winkLeft() { return "wink_left"; }
    public static String winkRight() { return "wink_right"; }
    public static String attentionLeft() { return "attention_left"; }
    public static String attentionRight() { return "attention_right"; }
    public static String notification() { return "notification"; }
    public static String reset() { return "reset"; }

    public static String berry(int variant) {
        switch (Math.floorMod(variant, 3)) {
            case 0: return "berry_remember";
            case 1: return "berry_curious";
            default: return "berry_cheeky";
        }
    }
}
