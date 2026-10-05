package com.boop.shieldhome;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.json.JSONObject;

/** Activity-owned data lifecycle. No laptop, HA roundtrip, or new Kodi settings required. */
final class SerenNextUpSession implements AutoCloseable {
    interface Listener { void updated(List<SerenEpisode> episodes, String status); }
    final SerenPosterLoader posters;
    private final SerenNextUpCache cache;
    private final KodiJsonRpc reader = new KodiJsonRpc(), player = new KodiJsonRpc();
    private final ExecutorService reads = Executors.newSingleThreadExecutor(), plays = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private final SerenPlayback playback;
    private final Listener listener;
    private volatile List<SerenEpisode> entries = List.of();
    private volatile boolean closed;
    private long lastRefresh;

    SerenNextUpSession(Context context, Listener listener) {
        this.listener = listener;
        posters = new SerenPosterLoader(context);
        cache = new SerenNextUpCache(new File(context.getFilesDir(), "seren-next-up.json"));
        playback = new SerenPlayback(plays, new SerenPlayback.Port() {
            public void awaitReady() throws Exception {
                long deadline = android.os.SystemClock.elapsedRealtime() + 15000;
                while (!closed && android.os.SystemClock.elapsedRealtime() < deadline) {
                    try { player.call("JSONRPC.Ping", new JSONObject(), 1200); return; }
                    catch (IOException unavailable) { Thread.sleep(250); }
                }
                throw new IOException("Kodi did not start");
            }
            public void open(SerenEpisode episode) throws Exception {
                player.call("Player.Open", SerenEpisode.playbackParams(episode), 30000);
            }
        });
        reads.execute(() -> { entries=cache.load(); publish(""); refresh(true); });
    }
    List<SerenEpisode> current() { return entries; }
    void refresh(boolean force) {
        if (closed || (!force && android.os.SystemClock.elapsedRealtime()-lastRefresh < 60000)
                || !refreshing.compareAndSet(false,true)) return;
        lastRefresh=android.os.SystemClock.elapsedRealtime();
        reads.execute(() -> {
            String status="";
            try {
                JSONObject result=reader.call("Files.GetDirectory",SerenEpisode.directoryParams(),30000).getJSONObject("result");
                entries=cache.accept(result);
                if(entries.isEmpty()) status="You're caught up";
            } catch(Exception unavailable) { status=entries.isEmpty()?"Open Kodi to load Next Up":"Saved Next Up"; }
            finally { refreshing.set(false); }
            publish(status);
        });
    }
    private void publish(String status) { main.post(() -> { if(!closed)listener.updated(entries,status); }); }
    void play(SerenEpisode episode, BooleanSupplier launch, Consumer<String> error) {
        playback.start(episode,launch,message->main.post(()->{if(!closed)error.accept(message);}));
    }
    @Override public void close() {
        closed=true;playback.close();reader.close();player.close();reads.shutdownNow();plays.shutdownNow();
        posters.close();main.removeCallbacksAndMessages(null);
    }
}
