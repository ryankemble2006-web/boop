package com.boop.shieldhome;

/** Holds a first-favourite focus request until the favourite list is ready. */
final class HomeStartupFocusGate {
    private boolean pending = true;

    void request() {
        pending = true;
    }

    boolean consumeWhenReady(boolean favouritesReady) {
        if (!pending || !favouritesReady) return false;
        pending = false;
        return true;
    }

    boolean isPending() {
        return pending;
    }
}
