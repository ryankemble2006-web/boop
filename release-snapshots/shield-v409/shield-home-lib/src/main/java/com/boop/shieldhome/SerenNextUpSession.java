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
    private final BackgroundKodiSync backgroundKodi;
    private final SerenNextUpCache cache;
    private final KodiJsonRpc reader = new KodiJsonRpc(), player = new KodiJsonRpc();
    private final KodiJsonRpc syncRpc = new KodiJsonRpc();
    private final ExecutorService syncs = Executors.newSingleThreadExecutor();
    private final AtomicBoolean syncing = new AtomicBoolean();
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
        backgroundKodi = new BackgroundKodiSync(context);
        posters = new SerenPosterLoader(context);
        cache = new SerenNextUpCache(new File(context.getFilesDir(), "seren-next-up.json"));
        playback = new SerenPlayback(plays, new SerenPlayback.Port() {
            public void awaitReady() throws Exception {
                long deadline = android.os.SystemClock.elapsedRealtime() + 15000;
                long delay = 100L;
                while (!closed && android.os.SystemClock.elapsedRealtime() < deadline) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    try { player.call("JSONRPC.Ping", new JSONObject(), 1200); return; }
                    catch (IOException unavailable) {
                        Thread.sleep(delay);
                        delay = Math.min(delay * 2, 800L);
                    }
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
    boolean isSyncing() { return syncing.get(); }
    void syncTrakt(Consumer<Boolean> busy, Consumer<String> result) {
        if (closed || !syncing.compareAndSet(false, true)) return;
        busy.accept(true);
        syncs.execute(() -> {
            String message;
            try {
                String requestId = java.util.UUID.randomUUID().toString();
                String status = SerenTraktSync.run(new SerenTraktSync.Port() {
                    public boolean ready() {
                        try { syncRpc.call("JSONRPC.Ping", new JSONObject(), 1200); return true; }
                        catch (Exception unavailable) { return false; }
                    }
                    public boolean launchKodi() throws Exception { return backgroundKodi.open(); }
                    public void returnToLauncher() { /* Kodi was never visible. */ }
                    public void execute(String id) { throw new AssertionError("Startup sync must not be duplicated"); }
                    public String status(String id) throws Exception {
                        String label = "Window(Home).Property(BOOP.TraktStartupSync)";
                        JSONObject response = syncRpc.call("XBMC.GetInfoLabels", new JSONObject()
                                .put("labels", new org.json.JSONArray().put(label)), 5000).getJSONObject("result");
                        return response.optString(label);
                    }
                    public long nowMs() { return android.os.SystemClock.elapsedRealtime(); }
                    public void sleep(long ms) throws Exception { Thread.sleep(ms); }
                    public boolean cancelled() { return closed || Thread.currentThread().isInterrupted(); }
                }, requestId);
                if ("done".equals(status) || "unchanged".equals(status)) {
                    // Read before removing the hidden display: Kodi owns the Seren service.
                    JSONObject feed = syncRpc.call("Files.GetDirectory", SerenEpisode.directoryParams(), 30000).getJSONObject("result");
                    entries = cache.accept(feed); publish(entries.isEmpty() ? "You're caught up" : "");
                    message = "unchanged".equals(status) ? "Already up to date — no Trakt changes" : "Trakt synced — Next Up refreshed";
                    android.util.Log.i("BOOPKodiSync", "Seren startup done; Next Up refreshed: " + entries.size());
                }
                else if ("no_auth".equals(status)) message = "Connect Trakt in Seren first";
                else message = "Trakt sync unavailable — saved episodes kept";
            } catch (Exception unavailable) { message = "Could not sync Trakt — saved episodes kept"; }
            backgroundKodi.close();
            syncing.set(false);
            String completed = message;
            main.post(() -> { if (!closed) { busy.accept(false); result.accept(completed); } });
        });
    }
    @Override public void close() {
        closed=true;backgroundKodi.close();playback.close();reader.close();player.close();reads.shutdownNow();plays.shutdownNow();
        syncRpc.close();syncs.shutdownNow();posters.close();main.removeCallbacksAndMessages(null);
    }
}
