package com.boop.shieldhome;

import java.io.File;
import com.boop.shieldturbo.power.AdbWire;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;


/** Wakes Deezer on an offscreen display, then controls its native session. */
final class BackgroundDeezerFlow {
    private static final String PACKAGE = "deezer.android.app";
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastRequest = -1200;
    private boolean pending;
    private VirtualDisplay display;
    private ImageReader images;

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BOOP-background-flow");
        t.setDaemon(true);
        return t;
    });

    BackgroundDeezerFlow(Context context) { this.context = context.getApplicationContext(); }

    boolean request(Runnable requested) {
        if (pending || display != null || SystemClock.elapsedRealtime() - lastRequest < 1200) return true;
        try {
            if (play(requested)) { lastRequest = SystemClock.elapsedRealtime(); return true; }
            ComponentName player = new ComponentName(PACKAGE, "deezer.android.app.navigation.ui.MainNavigationActivity");
            if (!context.getPackageManager().getActivityInfo(player, 0).exported) return false;
            // Match the existing hidden Deezer player: OWN_CONTENT_ONLY prevents mirroring;
            // DESTROY_CONTENT_ON_REMOVAL prevents its activity moving onto the TV at cleanup.
            images = ImageReader.newInstance(1280, 720, PixelFormat.RGBA_8888, 2);
            images.setOnImageAvailableListener(reader -> {
                try (android.media.Image frame = reader.acquireLatestImage()) { }
                catch (IllegalStateException closing) { }
            }, handler);
            DisplayManager displays = context.getSystemService(DisplayManager.class);
            display = displays.createVirtualDisplay("BOOP-background-flow", 1280, 720, 160,
                    images.getSurface(), DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                            | DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                            | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION | 256);
            if (display == null || display.getDisplay().getDisplayId() <= 0) {
                release(); return false;
            }
            pending = true;
            lastRequest = SystemClock.elapsedRealtime();
            int displayId = display.getDisplay().getDisplayId();
            executor.execute(() -> wakePlayer(displayId, requested));
            return true;
        } catch (Exception unavailable) {
            release();
            android.util.Log.w("BOOPFlow", "Unable to wake Deezer player", unavailable);
            return false;
        }
    }

    private boolean play(Runnable requested) {
        MediaSessionManager sessions = context.getSystemService(MediaSessionManager.class);
        if (sessions == null) return false;
        MediaController found = null;
        for (MediaController controller : sessions.getActiveSessions(
                new ComponentName(context, ShieldNowPlayingListenerService.class))) {
            if (!PACKAGE.equals(controller.getPackageName())) continue;
            PlaybackState state = controller.getPlaybackState();
            if (state == null || (state.getActions() & PlaybackState.ACTION_PLAY_FROM_URI) == 0) continue;
            found = controller;
            break;
        }
        if (found == null) return false;
        // Idle sessions still accept Flow even though they have no Now Playing metadata.
        found.getTransportControls().playFromUri(Uri.parse("https://www.deezer.com/flow"), null);
        if (requested != null) requested.run();
        android.util.Log.i("BOOPFlow", "Background Deezer Flow requested");
        return true;
    }

    private void wakePlayer(int displayId, Runnable requested) {
        try {
            File key = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
            if (!key.isFile()) throw new java.io.IOException("No approved local ADB identity");

            Exception lastError = null;
            boolean launched = false;
            for (int attempt = 0; attempt < 3 && !launched; attempt++) {
                if (attempt > 0) {
                    SystemClock.sleep(500);
                }
                try (AdbWire adb = new AdbWire()) {
                    adb.connect(5555, AdbWire.identity(key), 3500, () -> {}, false);
                    // Reuse the existing approved shell authority for offscreen Deezer launches.
                    // Android forbids ordinary apps placing a different app on a virtual display.
                    AdbWire.Result launch = adb.execute("am start --display " + displayId
                            + " -f 0x18000000 -n deezer.android.app/.navigation.ui.MainNavigationActivity", 4000);
                    if (launch.exitCode != 0 || launch.output.contains("Error:")
                            || launch.output.contains("Exception")) throw new java.io.IOException("Hidden player launch failed");
                    launched = true;
                } catch (Exception e) {
                    lastError = e;
                }
            }
            if (!launched) {
                throw (lastError != null ? lastError : new java.io.IOException("Hidden player launch failed"));
            }
            handler.postDelayed(() -> poll(requested, SystemClock.elapsedRealtime() + 15000), 1000);
        } catch (Exception unavailable) {
            android.util.Log.w("BOOPFlow", "Hidden Deezer player unavailable", unavailable);
            handler.post(() -> {
                pending = false;
                release();
                android.widget.Toast.makeText(context, "Deezer Flow is unavailable.", android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void release() {
        if (display != null) { display.release(); display = null; }
        if (images != null) { images.close(); images = null; }
    }

    private void awaitPlayback(Runnable requested, long deadline, long sentAt, int retries) {
        try {
            MediaSessionManager sessions = context.getSystemService(MediaSessionManager.class);
            for (MediaController controller : sessions.getActiveSessions(
                    new ComponentName(context, ShieldNowPlayingListenerService.class))) {
                if (!PACKAGE.equals(controller.getPackageName())) continue;
                PlaybackState state = controller.getPlaybackState();
                if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                    pending = false;
                    if (requested != null) requested.run();
                    // Defer virtual display teardown by 1200ms so Deezer's detached background
                    // playback service firmly locks in foreground priority before the activity dies.
                    handler.postDelayed(this::release, 1200);
                    return;
                }
                // Cold Deezer can advertise URI controls before its account or playback engine is ready.
                // Retry only while still empty/stopped/paused, never reset an active buffering Flow.
                int playbackState = (state != null ? state.getState() : PlaybackState.STATE_NONE);
                boolean notPlayingYet = (playbackState == PlaybackState.STATE_NONE
                        || playbackState == PlaybackState.STATE_STOPPED
                        || playbackState == PlaybackState.STATE_PAUSED);
                if (notPlayingYet && retries < 4 && SystemClock.elapsedRealtime() - sentAt >= 1500) {
                    play(requested);
                    sentAt = SystemClock.elapsedRealtime();
                    retries++;
                }
            }
            if (SystemClock.elapsedRealtime() < deadline) {
                final long lastSent = sentAt;
                final int attempts = retries;
                handler.postDelayed(() -> awaitPlayback(requested, deadline, lastSent, attempts), 200);
                return;
            }
        } catch (RuntimeException unavailable) {
            android.util.Log.w("BOOPFlow", "Deezer did not confirm Flow", unavailable);
        }
        pending = false;
        release();
        android.widget.Toast.makeText(context, "Deezer Flow is unavailable.", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void poll(Runnable requested, long deadline) {
        try {
            if (play(requested)) {
                awaitPlayback(requested, deadline, SystemClock.elapsedRealtime(), 0);
                return;
            }
            if (SystemClock.elapsedRealtime() < deadline) {
                handler.postDelayed(() -> poll(requested, deadline), 100);
                return;
            }
        } catch (RuntimeException unavailable) {
            android.util.Log.w("BOOPFlow", "Deezer session unavailable", unavailable);
        }
        pending = false;
        release();
        android.widget.Toast.makeText(context, "Deezer Flow is unavailable.", android.widget.Toast.LENGTH_SHORT).show();
    }
}
