package com.boop.shared;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/** One process-local puppet identity. Mutations are delivered synchronously, release before acquire. */
public final class BoopState {
    public enum Owner { NONE, MEDIA_CORNER, HOME_NOW_PLAYING }
    public interface Listener { void changed(Snapshot snapshot); }
    public static final BoopState INSTANCE = new BoopState();
    public static final class Snapshot {
        public final Owner owner;
        public final boolean listening, speaking, playing, homeVisible;
        public final String roomId, roomName, mediaId;
        public final long mediaStartedMs, revision;
        private Snapshot(BoopState state, Owner owner) {
            this.owner = owner; listening = state.listening; speaking = state.speaking;
            playing = state.playing; homeVisible = state.home;
            roomId = state.roomId; roomName = state.roomName; mediaId = state.mediaId;
            mediaStartedMs = state.mediaStartedMs; revision = ++state.revision;
        }
    }
    private final Set<Listener> listeners = new LinkedHashSet<>();
    private boolean listening, speaking, playing, home, cornerAllowed;
    private String roomId = "", roomName = "", mediaId = "";
    private long mediaStartedMs, revision;
    private Snapshot current = new Snapshot(this, Owner.NONE);
    public synchronized Snapshot snapshot() { return current; }
    public synchronized Runnable subscribe(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("listener required");
        listeners.add(listener); listener.changed(current);
        return () -> { synchronized (BoopState.this) { listeners.remove(listener); } };
    }
    public synchronized void speech(boolean listening, boolean speaking) {
        if (this.listening == listening && this.speaking == speaking) return;
        this.listening = listening; this.speaking = speaking; publish();
    }
    public synchronized void room(String id, String name) {
        id = clean(id); name = clean(name);
        if (id.equals(roomId) && name.equals(roomName)) return;
        roomId = id; roomName = name; publish();
    }
    public synchronized void media(String id, boolean playing, long nowMs) {
        id = clean(id);
        if (this.playing == playing && mediaId.equals(id)) return;
        if (!mediaId.equals(id) || (playing && !this.playing)) mediaStartedMs = Math.max(0,nowMs);
        mediaId = id; this.playing = playing && !id.isEmpty(); publish();
    }
    public synchronized void homeVisible(boolean visible) {
        if (home == visible) return;
        home = visible; publish();
    }
    public synchronized void cornerAllowed(boolean allowed) {
        if(cornerAllowed == allowed) return;
        cornerAllowed = allowed; publish();
    }
    private void publish() {
        Owner next = !playing ? Owner.NONE : home ? Owner.HOME_NOW_PLAYING : cornerAllowed ? Owner.MEDIA_CORNER : Owner.NONE;
        if (current.owner != Owner.NONE && current.owner != next) {
            Snapshot released = new Snapshot(this, Owner.NONE);
            deliver(released);
            if (current != released) return; // a listener changed the state during release
        }
        deliver(new Snapshot(this, next));
    }
    private void deliver(Snapshot value) {
        current = value;
        for (Listener listener : new ArrayList<>(listeners)) {
            if (current != value) return;
            listener.changed(value);
        }
    }
    private static String clean(String value) { return value == null ? "" : value.trim(); }
}
