package com.boop.shieldhome;

import android.os.Handler;
import android.os.Looper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/** One UI owner, one cancellable request. Successful documents live briefly in memory only. */
final class NativeLyricsLoader {
    private static final long WAIT_MS = 5500L;
    private static final long DEEZER_WAIT_MS = 2500L;
    private static final long CACHE_MS = 300000L;
    private static final Map<String, Cached> CACHE = new LinkedHashMap<>();
    private static final class Cached {
        final DeezerLyricsDocument document;
        final long expires;
        Cached(DeezerLyricsDocument document) {
            this.document = document;
            expires = DeezerLyricsClient.nowMs() + CACHE_MS;
        }
    }
    private final Handler main = new Handler(Looper.getMainLooper());
    private final LyricsRequestGate gate = new LyricsRequestGate();
    private final DeezerTimedLyricsClient client = new DeezerTimedLyricsClient();
    private final LrclibLyricsClient fallback = new LrclibLyricsClient();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "boop-native-lyrics"); thread.setDaemon(true); return thread;
    });
    private Future<?> task;
    private DeezerLyricsClient.Call call;
    private Runnable timeout;
    private String pending = "";
    private static synchronized DeezerLyricsDocument cached(String id) {
        Cached value = CACHE.get(id);
        if (value == null) return null;
        if (DeezerLyricsClient.nowMs() >= value.expires) { CACHE.remove(id); return null; }
        return value.document;
    }
    private static synchronized void remember(DeezerLyricsDocument document) {
        if (document.status() != DeezerLyricsDocument.Status.AVAILABLE) return;
        CACHE.remove(document.trackId());
        CACHE.put(document.trackId(), new Cached(document));
        while (CACHE.size() > 4) CACHE.remove(CACHE.keySet().iterator().next());
    }
    void load(String id, String identity, Consumer<DeezerLyricsDocument> completion) {
        load(null, id, identity, completion);
    }
    void load(NowPlayingSnapshot track, String id, String identity,
            Consumer<DeezerLyricsDocument> completion) {
        if (worker.isShutdown() || identity.equals(pending)) return;
        cancel();
        long ticket = gate.begin(identity);
        pending = identity;
        DeezerLyricsDocument hit = cached(id);
        if (hit != null) {
            pending = "";
            completion.accept(hit);
            return;
        }
        DeezerLyricsClient.Call request = new DeezerLyricsClient.Call();
        call = request;
        long deadline = DeezerLyricsClient.nowMs() + WAIT_MS;
        timeout = () -> {
            if (!gate.accepts(ticket, identity)) return;
            cancel();
            completion.accept(DeezerLyricsDocument.unknown(id));
        };
        main.postDelayed(timeout, WAIT_MS);
        task = worker.submit(() -> {
            long primaryDeadline = Math.min(deadline, DeezerLyricsClient.nowMs() + DEEZER_WAIT_MS);
            DeezerLyricsDocument document = client.load(id, request, primaryDeadline);
            if (document.status() != DeezerLyricsDocument.Status.AVAILABLE
                    && track != null && !request.cancelled()
                    && DeezerLyricsClient.nowMs() < deadline) {
                DeezerLyricsDocument second = fallback.load(track, id, request, deadline);
                if (second.status() == DeezerLyricsDocument.Status.AVAILABLE
                        || document.status() == DeezerLyricsDocument.Status.UNAVAILABLE)
                    document = second;
            }
            final DeezerLyricsDocument result = document;
            main.post(() -> {
                if (!gate.accepts(ticket, identity)) return;
                if (DeezerLyricsClient.nowMs() >= deadline) {
                    Runnable expired = timeout;
                    if (expired != null) expired.run();
                    return;
                }
                if (timeout != null) main.removeCallbacks(timeout);
                timeout = null;
                call = null;
                task = null;
                pending = "";
                remember(result);
                completion.accept(result);
            });
        });
    }
    void cancel() {
        gate.cancel();
        pending = "";
        if (timeout != null) main.removeCallbacks(timeout);
        timeout = null;
        if (call != null) call.cancel();
        call = null;
        if (task != null) task.cancel(true);
        task = null;
    }
    void destroy() { cancel(); worker.shutdownNow(); }
}
