package com.boop.shieldhome;

/** Tracks one Back-key press so long-press and short-release never both fire. */
final class BackPressGesture {
    private boolean pressed;
    private boolean holdTriggered;

    void onDown() {
        pressed = true;
        holdTriggered = false;
    }

    boolean onHoldTriggered() {
        if (!pressed || holdTriggered) {
            return false;
        }
        holdTriggered = true;
        return true;
    }

    boolean onUpShouldRunShortBack() {
        if (!pressed) {
            return false;
        }
        boolean shortBack = !holdTriggered;
        pressed = false;
        holdTriggered = false;
        return shortBack;
    }

    void cancel() {
        pressed = false;
        holdTriggered = false;
    }
}
