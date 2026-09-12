package com.boop.shieldhome;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Checks the exact track before handing the already-approved positive path to Deezer. */
final class DeezerLyricsBrowser {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "boop-deezer-lyrics"); t.setDaemon(true); return t;
    });
    private final DeezerLyricsClient client = new DeezerLyricsClient();
    private final DeezerLyricsGate gate = new DeezerLyricsGate();
    private volatile long generation;
    private Future<?> task;
    private DeezerLyricsClient.Call networkCall;
    private Runnable timeout;
    private NowPlayingSnapshot pinnedSnapshot;
    private String pinnedIdentity = "";
    private StartupLocalBridge positiveBridge;
    private boolean opening;

    void cancel() {
        generation++;
        gate.cancel();
        stopTask();
        if (positiveBridge != null) positiveBridge.cancel();
        positiveBridge = null;
        pinnedSnapshot = null;
        pinnedIdentity = "";
        opening = false;
    }
    void destroy() { cancel(); worker.shutdownNow(); }
    void onHostPaused() { if (!opening) cancel(); }
    void onTrackChanged(ShieldNowPlayingManager manager) {
        if (pinnedSnapshot != null && !pinnedIdentity.equals(identity(manager, pinnedSnapshot))) cancel();
    }
    private void stopTask() {
        if (timeout != null) main.removeCallbacks(timeout);
        timeout = null;
        if (networkCall != null) networkCall.cancel();
        networkCall = null;
        if (task != null) task.cancel(true);
        task = null;
    }
    void open(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        if (activity == null || manager == null || requested == null || worker.isShutdown()
                || !DeezerLyricsPolicy.available(requested.packageName())) return;
        String id = manager.deezerLyricsTrackId(requested);
        if (id.isEmpty()) { message(activity, "Couldn't identify this track for lyrics."); return; }
        String identity = requested.sessionId() + ":" + id;
        DeezerLyricsGate.Ticket ticket = gate.begin(identity, DeezerLyricsClient.nowMs());
        if (ticket == null) return;
        stopTask();
        if (positiveBridge != null) positiveBridge.cancel();
        positiveBridge = null;
        opening = false;
        long operation = ++generation;
        pinnedSnapshot = requested;
        pinnedIdentity = identity;
        DeezerLyricsClient.Call call = new DeezerLyricsClient.Call();
        networkCall = call;
        Runnable timedOut = () -> {
            if (!gate.expire(ticket, DeezerLyricsClient.nowMs())) return;
            stopTask();
            pinnedSnapshot = null;
            if (validHost(activity) && ticket.identity.equals(identity(manager, requested)))
                message(activity, "Couldn't check lyrics just now.");
        };
        timeout = timedOut;
        main.postDelayed(timedOut, Math.max(0L, ticket.deadline - DeezerLyricsClient.nowMs()));
        task = worker.submit(() -> {
            DeezerLyricsClient.Result result = client.check(id, call, ticket.deadline);
            main.post(() -> {
                if (operation != generation) return;
                if (DeezerLyricsClient.nowMs() >= ticket.deadline) { timedOut.run(); return; }
                DeezerLyricsGate.Action action = gate.resolve(ticket, identity(manager, requested),
                        DeezerLyricsClient.nowMs(), result);
                main.removeCallbacks(timedOut);
                timeout = null;
                networkCall = null;
                task = null;
                if (!validHost(activity) || action == DeezerLyricsGate.Action.IGNORE) {
                    pinnedSnapshot = null;
                    return;
                }
                if (action == DeezerLyricsGate.Action.NO_LYRICS) {
                    pinnedSnapshot = null;
                    message(activity, "No lyrics for this track.");
                } else if (action == DeezerLyricsGate.Action.CANNOT_CHECK) {
                    pinnedSnapshot = null;
                    message(activity, "Couldn't check lyrics just now.");
                } else {
                    openVerifiedPositive(activity, manager, operation);
                }
            });
        });
    }
    private void openVerifiedPositive(Activity activity, ShieldNowPlayingManager manager, long operation) {
        opening = true; // This intentional handoff must survive Home's onPause callback.
        boolean launched;
        try { launched = manager.openNotificationSource(); }
        catch (RuntimeException unavailable) { launched = false; }
        if (!launched) {
            opening = false;
            pinnedSnapshot = null;
            message(activity, "Deezer player isn't available yet.");
            return;
        }
        StartupLocalBridge bridge = new StartupLocalBridge(activity);
        positiveBridge = bridge;
        task = worker.submit(() -> {
            boolean opened = false;
            for (int attempt = 0; attempt < 4 && !opened; attempt++) {
                if (operation != generation || Thread.currentThread().isInterrupted()) return;
                try {
                    Thread.sleep(attempt == 0 ? 350L : 250L);
                    if (operation != generation || Thread.currentThread().isInterrupted()) return;
                    bridge.openDeezerLyrics();
                    opened = true;
                } catch (Exception unavailable) {
                    if (Thread.currentThread().isInterrupted() || operation != generation) return;
                }
            }
            final boolean success = opened;
            main.post(() -> {
                if (operation != generation) return;
                opening = false;
                pinnedSnapshot = null;
                positiveBridge = null;
                task = null;
                if (!success && !activity.isFinishing() && !activity.isDestroyed())
                    message(activity, "Deezer couldn't open lyrics for this track.");
            });
        });
    }
    private static String identity(ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        String id = manager.deezerLyricsTrackId(requested);
        return id.isEmpty() ? "" : requested.sessionId() + ":" + id;
    }
    private static boolean validHost(Activity activity) {
        return !activity.isFinishing() && !activity.isDestroyed() && activity.hasWindowFocus();
    }
    private static void message(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
