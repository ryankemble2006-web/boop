package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Tiny synchronous state bus shared by the media manager and launcher surfaces. */
public final class NowPlayingState {
    public interface Listener {
        void onNowPlayingChanged(NowPlayingSnapshot snapshot);
    }

    private final List<Listener> listeners = new ArrayList<>();
    private NowPlayingSnapshot current;

    public Runnable subscribe(Listener listener) {
        if (listener == null) {
            return () -> { };
        }

        NowPlayingSnapshot initial;
        synchronized (this) {
            listeners.add(listener);
            initial = current;
        }
        listener.onNowPlayingChanged(initial);

        AtomicBoolean removed = new AtomicBoolean(false);
        return () -> {
            if (!removed.compareAndSet(false, true)) return;
            synchronized (NowPlayingState.this) {
                listeners.remove(listener);
            }
        };
    }

    public synchronized NowPlayingSnapshot current() {
        return current;
    }

    public void update(NowPlayingSnapshot snapshot) {
        List<Listener> copy;
        synchronized (this) {
            if (Objects.equals(current, snapshot)) {
                return;
            }
            current = snapshot;
            copy = new ArrayList<>(listeners);
        }
        for (Listener listener : copy) {
            listener.onNowPlayingChanged(snapshot);
        }
    }
}
