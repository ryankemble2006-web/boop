package com.boop.alpha1;

final class BoopPresenceState {
    enum State {
        IDLE_BLACK,
        AWAKE
    }

    private State state = State.IDLE_BLACK;

    State state() {
        return state;
    }

    boolean isIdleBlack() {
        return state == State.IDLE_BLACK;
    }

    boolean wake() {
        if (state == State.AWAKE) {
            return false;
        }
        state = State.AWAKE;
        return true;
    }

    boolean idle() {
        if (state == State.IDLE_BLACK) {
            return false;
        }
        state = State.IDLE_BLACK;
        return true;
    }
}
