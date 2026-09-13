package com.boop.alpha1;

public final class BoopMotionPolicyHarness {
    private static void expect(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        expect(BoopMotionPolicy.shouldAnimate(false), "normal power state must animate BOOP regardless of Android UI scale");
        expect(!BoopMotionPolicy.shouldAnimate(true), "power saver may reduce BOOP motion");
        System.out.println("2 motion policy scenarios passed");
    }
}