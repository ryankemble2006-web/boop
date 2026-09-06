package com.boop.alpha1;

public final class BoopLauncherSwipeGestureHarness {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(BoopLauncherSwipeGesture.shouldOpenLauncher(220f, 400f, 124f, 400f, false, false, 96f),
                "a 96dp horizontal left swipe should open Launcher");
        check(!BoopLauncherSwipeGesture.shouldOpenLauncher(220f, 400f, 125f, 400f, false, false, 96f),
                "a 95dp left move must not open Launcher");
        check(!BoopLauncherSwipeGesture.shouldOpenLauncher(220f, 400f, 124f, 465f, false, false, 96f),
                "a diagonal move without 1.5x horizontal confidence must not open Launcher");
        check(!BoopLauncherSwipeGesture.shouldOpenLauncher(124f, 400f, 220f, 400f, false, false, 96f),
                "a right swipe must not open Launcher");
        check(!BoopLauncherSwipeGesture.shouldOpenLauncher(220f, 400f, 100f, 400f, true, false, 96f),
                "a cancelled gesture must not open Launcher");
        check(!BoopLauncherSwipeGesture.shouldOpenLauncher(220f, 400f, 100f, 400f, false, true, 96f),
                "a multi-touch gesture must not open Launcher");
        check(BoopLauncherSwipeGesture.shouldOpenLauncher(720f, 400f, 384f, 400f, false, false, 336f),
                "a 96dp swipe must scale to the display density");

        System.out.println("BOOP Launcher swipe gesture harness passed");
    }
}
