package com.boop.shieldhome;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Private user action. Uses only this Shield's previously approved local connection. */
public final class LauncherClosePlayerActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ShieldNowPlayingManager manager;
    private StartupLocalBridge bridge;
    private Runnable unsubscribe;
    private Thread worker;
    private volatile boolean cancelled;
    private volatile NowPlayingSnapshot latest;
    private boolean all;
    private long session;
    private String player;
    private MediaSession.Token originalToken;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        if (saved != null) { finish(); return; }
        manager = ShieldNowPlayingManager.get(this);
        all = getIntent().getBooleanExtra("all_media_apps", false);
        session = getIntent().getLongExtra("session", 0);
        player = getIntent().getStringExtra("player");
        latest = manager.state().current();
        if (!current()) { complete("That player has changed. Please try again."); return; }
        if (!all) {
            originalToken = manager.sessionToken(session);
            if (originalToken == null) { complete("Media access is not ready."); return; }
        }
        unsubscribe = manager.state().subscribe(value -> latest = value);
        TextView status = new TextView(this);
        status.setText((all ? "Closing media apps..." : "Closing player...") + "\nPress Back to cancel");
        status.setTextSize(24); status.setGravity(Gravity.CENTER);
        setContentView(status);
        handler.postDelayed(() -> complete("Could not finish closing the player. Check the local tools connection."), 12000);
        if (!all && "com.google.android.apps.mediashell".equals(player)) {
            if (manager.stopSelectedCast(session)) awaitRemoved(25);
            else complete("This Cast session does not offer Stop.");
            return;
        }
        bridge = new StartupLocalBridge(this);
        if (!bridge.hasIdentity()) {
            complete("Connect the advanced tools in Startup Manager first."); return;
        }
        worker = new Thread(this::closeNative, "BOOP-Launcher-close");
        worker.start();
    }

    private boolean current() {
        if (cancelled) return false;
        NowPlayingSnapshot value = latest;
        return all || value != null && value.sessionId() == session
                && player != null && player.equals(value.packageName());
    }

    private void closeNative() {
        try (StartupLocalBridge.PackageSession local = bridge.openPackageSession(false, null)) {
            if (!current()) { handler.post(() -> complete("That player has changed. Please try again.")); return; }
            if (all) {
                FutureTask<Boolean> stopCast = new FutureTask<>(() -> !cancelled && manager.stopCastForCleanup());
                handler.post(stopCast);
                if (!stopCast.get(3, TimeUnit.SECONDS)) throw new IllegalStateException("Cast could not be stopped safely");
            }
            LocalMediaClosePolicy.execute(all, session, player, this::current, new LocalMediaClosePolicy.Bridge() {
                @Override public boolean installed(String name) throws Exception {
                    try { getPackageManager().getApplicationInfo(name, PackageManager.MATCH_DISABLED_COMPONENTS); }
                    catch (PackageManager.NameNotFoundException missing) { return false; }
                    local.probe(name);
                    return true;
                }
                @Override public void stop(String name) throws Exception { local.forceStop(name); }
            });
            handler.post(() -> awaitRemoved(25));
        } catch (Exception unavailable) {
            handler.post(() -> { if (!cancelled) complete("Could not finish closing. Check the connection in Startup Manager."); });
        }
    }

    private void awaitRemoved(int attempts) {
        if (cancelled) return;
        if (all ? manager.confirmsMediaCleanup() : manager.confirmsSessionGone(originalToken)) {
            complete(null); return;
        }
        if (attempts <= 0) { complete("Stop was sent, but the player has not confirmed closing."); return; }
        handler.postDelayed(() -> awaitRemoved(attempts - 1), 120);
    }

    private void complete(String message) {
        if (cancelled) return;
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        cancelWork(); finish();
    }
    private void cancelWork() {
        cancelled = true;
        handler.removeCallbacksAndMessages(null);
        if (unsubscribe != null) { unsubscribe.run(); unsubscribe = null; }
        if (bridge != null) bridge.cancel();
        if (worker != null) worker.interrupt();
    }
    @Override public void onBackPressed() { cancelWork(); finish(); }
    @Override protected void onPause() { cancelWork(); super.onPause(); if (!isFinishing()) finish(); }
    @Override protected void onDestroy() { cancelWork(); super.onDestroy(); }
}
