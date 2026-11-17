package com.boop.shieldhome;

/** Permission decisions only. This class never starts playback analysis or voice. */
public final class MusicAudioPermissionFlow {
    public enum Action { READY, EXPLAIN, REQUEST, WAIT, DENIED }

    private boolean pending;

    public MusicAudioPermissionFlow(boolean pending) {
        this.pending = pending;
    }

    public Action begin(boolean granted) {
        if (granted) {
            pending = false;
            return Action.READY;
        }
        return pending ? Action.WAIT : Action.EXPLAIN;
    }

    public Action continueRequest(boolean granted) {
        Action current = begin(granted);
        if (current != Action.EXPLAIN) return current;
        pending = true;
        return Action.REQUEST;
    }

    public Action result(boolean granted) {
        pending = false;
        return granted ? Action.READY : Action.DENIED;
    }

    public boolean isPending() {
        return pending;
    }
}
