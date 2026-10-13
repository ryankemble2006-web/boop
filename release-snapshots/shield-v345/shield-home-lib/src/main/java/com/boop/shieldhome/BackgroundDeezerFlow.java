package com.boop.shieldhome;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
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
            if (found != null) return false;
            found = controller;
        }
        if (found == null) return false;
        // Idle sessions still accept Flow even though they have no Now Playing metadata.
        found.getTransportControls().playFromUri(Uri.parse("https://www.deezer.com/flow"), null);
        if (requested != null) requested.run();
        android.util.Log.i("BOOPFlow", "Background Deezer Flow requested");
        return true;
    }

    private void wakePlayer(int displayId, Runnable requested) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        File marker = new File(context.getFilesDir(), "boop-flow-" + nonce);
        try (FileOutputStream out = new FileOutputStream(marker)) {
            out.write(nonce.getBytes(StandardCharsets.US_ASCII));
            File key = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
            if (!key.isFile()) throw new java.io.IOException("No approved local ADB identity");
            try (AdbWire adb = new AdbWire()) {
                adb.connect(5555, AdbWire.identity(key), 1500, () -> {}, false);
                AdbWire.Result binding = adb.execute("run-as " + context.getPackageName()
                        + " cat files/boop-flow-" + nonce + " 2>/dev/null", 1500);
                if (binding.exitCode != 0 || !nonce.equals(binding.output.trim()))
                    throw new java.io.IOException("Local hardware binding failed");
                // Reuse the existing approved shell authority for offscreen Deezer launches.
                // Android forbids ordinary apps placing a different app on a virtual display.
                AdbWire.Result launch = adb.execute("am start --display " + displayId
                        + " -f 0x18000000 -n deezer.android.app/.navigation.ui.MainNavigationActivity", 4000);
                if (launch.exitCode != 0 || launch.output.contains("Error:")
                        || launch.output.contains("Exception")) throw new java.io.IOException("Hidden player launch failed");
            }
            handler.postDelayed(() -> poll(requested, SystemClock.elapsedRealtime() + 15000), 1000);
        } catch (Exception unavailable) {
            android.util.Log.w("BOOPFlow", "Hidden Deezer player unavailable", unavailable);
            handler.post(() -> { pending = false; release();
                android.widget.Toast.makeText(context, "Deezer Flow is unavailable.", android.widget.Toast.LENGTH_SHORT).show(); });
        } finally { marker.delete(); }
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
                    pending = false; release();
                    if (requested != null) requested.run();
                    return;
                }
                // Cold Deezer can advertise URI controls before its account has initialized.
                // Retry only while still empty, never reset a playing or buffering Flow.
                if (state != null && state.getState() == PlaybackState.STATE_NONE && retries < 2
                        && SystemClock.elapsedRealtime() - sentAt >= 1500) {
                    play(requested); sentAt = SystemClock.elapsedRealtime(); retries++;
                }
            }
            if (SystemClock.elapsedRealtime() < deadline) {
                final long lastSent = sentAt; final int attempts = retries;
                handler.postDelayed(() -> awaitPlayback(requested, deadline, lastSent, attempts), 200);
                return;
            }
        } catch (RuntimeException unavailable) {
            android.util.Log.w("BOOPFlow", "Deezer did not confirm Flow", unavailable);
        }
        pending = false; release();
        android.widget.Toast.makeText(context, "Deezer Flow is unavailable.", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void poll(Runnable requested, long deadline) {
        try {
            if (play(requested)) { awaitPlayback(requested, deadline, SystemClock.elapsedRealtime(), 0); return; }
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
