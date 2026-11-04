package com.boop.alpha1;

final class BoopWakeTriggerGate {
    static final long REFRACTORY_MS = 750L;

    private long lastAcceptedMs = Long.MIN_VALUE;

    boolean accept(long nowMs) {
        if (lastAcceptedMs != Long.MIN_VALUE
                && nowMs - lastAcceptedMs < REFRACTORY_MS) {
            return false;
        }
        lastAcceptedMs = nowMs;
        return true;
    }
}
