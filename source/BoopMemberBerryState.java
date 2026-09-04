package com.boop.alpha1;

final class BoopMemberBerryState {
    static final int NONE = -1;

    private static final int FIRST_BERRY_WAKE = 3;
    private static final int BERRY_INTERVAL_WAKES = 8;
    private static final int BERRY_VARIANTS = 3;

    private int wakeCount;
    private int berryCount;

    int onWake() {
        wakeCount++;
        if (wakeCount < FIRST_BERRY_WAKE
                || (wakeCount - FIRST_BERRY_WAKE) % BERRY_INTERVAL_WAKES != 0) {
            return NONE;
        }
        int variant = berryCount % BERRY_VARIANTS;
        berryCount++;
        return variant;
    }

    int wakeCount() {
        return wakeCount;
    }
}
