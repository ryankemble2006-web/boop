package com.boop.alpha1;

final class BoopDockPower {
    static final int PLUGGED_WIRELESS = 4;

    private BoopDockPower() { }

    static boolean isWireless(int pluggedFlags) {
        return (pluggedFlags & PLUGGED_WIRELESS) != 0;
    }
}
