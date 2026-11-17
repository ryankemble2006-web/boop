package com.boop.shieldhome;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Single-flight launch; readiness may be polled, playback is sent at most once. */
final class SerenPlayback implements AutoCloseable {
    interface Port {
        void awaitReady() throws Exception;
        void open(SerenEpisode episode) throws Exception;
    }
    private final Executor executor;
    private final Port port;
    private final AtomicBoolean busy = new AtomicBoolean();
    private volatile boolean closed;
    SerenPlayback(Executor executor, Port port) { this.executor = executor; this.port = port; }

    boolean start(SerenEpisode episode, BooleanSupplier launch, Consumer<String> failure) {
        if (closed || !busy.compareAndSet(false, true)) return false;
        try {
            if (!launch.getAsBoolean()) {
                busy.set(false); failure.accept("Kodi could not be opened."); return true;
            }
            executor.execute(() -> {
                try {
                    port.awaitReady();
                    if (!closed) port.open(episode);
                } catch (Exception unavailable) {
                    if (!closed) failure.accept("Kodi could not start that episode. Open Seren to check its sources.");
                } finally { busy.set(false); }
            });
        } catch (RuntimeException stopped) {
            busy.set(false);
            if (!closed) failure.accept("Kodi could not be opened.");
        }
        return true;
    }
    @Override public void close() { closed = true; }
}
