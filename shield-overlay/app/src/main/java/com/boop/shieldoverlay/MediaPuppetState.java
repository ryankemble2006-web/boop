package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MediaPuppetState {
    public static final class Snapshot {
        public final boolean enabled;
        public final boolean granted;
        public final boolean connected;
        public final long sessionId;
        public final Integer playbackState;
        public final DeezerPuppetPolicy.Mode mode;

        private Snapshot(
                boolean enabled,
                boolean granted,
                boolean connected,
                long sessionId,
                Integer playbackState,
                DeezerPuppetPolicy.Mode mode) {
            this.enabled = enabled;
            this.granted = granted;
            this.connected = connected;
            this.sessionId = sessionId;
            this.playbackState = playbackState;
            this.mode = mode;
        }

        public String status() {
            if (!enabled) {
                return "Off";
            }
            if (!granted) {
                return "Access needed";
            }
            if (!connected) {
                return "Connecting";
            }
            return "On";
        }

        private boolean sameAs(Snapshot other) {
            return other != null
                    && enabled == other.enabled
                    && granted == other.granted
                    && connected == other.connected
                    && sessionId == other.sessionId
                    && java.util.Objects.equals(playbackState, other.playbackState)
                    && mode == other.mode;
        }
    }

    public interface Listener {
        void onChanged(Snapshot snapshot);
    }

    private final Set<Listener> listeners = new LinkedHashSet<>();
    private boolean enabled;
    private boolean granted;
    private boolean connected;
    private long sessionId;
    private Integer playbackState;
    private Snapshot current = buildSnapshot();

    public void updateAccess(boolean enabled, boolean granted, boolean connected) {
        this.enabled = enabled;
        this.granted = granted;
        this.connected = connected;
        if (!enabled || !granted || !connected) {
            sessionId = 0L;
            playbackState = null;
        }
        publishIfChanged();
    }

    public void updateSession(long sessionId, Integer playbackState) {
        if (!enabled || !granted || !connected || sessionId == 0L) {
            this.sessionId = 0L;
            this.playbackState = null;
        } else {
            this.sessionId = sessionId;
            this.playbackState = playbackState;
        }
        publishIfChanged();
    }

    public Snapshot snapshot() {
        return current;
    }

    public Runnable subscribe(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }
        listeners.add(listener);
        listener.onChanged(current);
        return new Runnable() {
            private boolean unsubscribed;

            @Override
            public void run() {
                if (unsubscribed) {
                    return;
                }
                unsubscribed = true;
                listeners.remove(listener);
            }
        };
    }

    private void publishIfChanged() {
        Snapshot next = buildSnapshot();
        if (next.sameAs(current)) {
            return;
        }
        current = next;
        for (Listener listener : new ArrayList<>(listeners)) {
            // A listener can synchronously publish a newer snapshot to everyone.
            // Never follow that nested delivery with this obsolete outer value.
            if (current != next) {
                return;
            }
            listener.onChanged(next);
        }
    }

    private Snapshot buildSnapshot() {
        DeezerPuppetPolicy.Mode mode = DeezerPuppetPolicy.mode(
                enabled, granted, connected, playbackState);
        return new Snapshot(enabled, granted, connected, sessionId, playbackState, mode);
    }
}
