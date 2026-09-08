package com.boop.alpha1;

final class BoopListeningCueState {
    private boolean active;

    boolean start() {
        if (active) return false;
        active = true;
        return true;
    }

    boolean stop() {
        if (!active) return false;
        active = false;
        return true;
    }

    boolean isActive() {
        return active;
    }
}
