package com.boop.shieldoverlay;

public final class FirstRunCoordinator {
    public enum State {
        PAIRING,
        PAIRING_SUCCESS,
        ROOM_PICKER,
        HOME
    }

    private State state = State.PAIRING;

    public State start(boolean hasCredential, boolean hasRoom) {
        if (!hasCredential) {
            state = State.PAIRING;
        } else if (!hasRoom) {
            state = State.ROOM_PICKER;
        } else {
            state = State.HOME;
        }
        return state;
    }

    public State onPairingConnected() {
        state = State.PAIRING_SUCCESS;
        return state;
    }

    public State afterPairingSuccess(boolean hasRoom) {
        state = hasRoom ? State.HOME : State.ROOM_PICKER;
        return state;
    }

    public State state() {
        return state;
    }
}
