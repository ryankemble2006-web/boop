package com.boop.alpha1;

final class BoopDockPower {
    static final int PLUGGED_AC = 1;
    static final int PLUGGED_USB = 2;
    static final int PLUGGED_WIRELESS = 4;
    static final int PLUGGED_DOCK = 8;
    private static final int EXTERNAL_POWER_MASK =
            PLUGGED_AC | PLUGGED_USB | PLUGGED_WIRELESS | PLUGGED_DOCK;

    private BoopDockPower() { }

    static boolean isExternallyPowered(int pluggedFlags) {
        return (pluggedFlags & EXTERNAL_POWER_MASK) != 0;
    }

    static boolean isWireless(int pluggedFlags) {
        return (pluggedFlags & PLUGGED_WIRELESS) != 0;
    }
}
