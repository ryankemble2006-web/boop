package com.boop.shieldhome;

import java.io.File;
import com.boop.shieldturbo.power.AdbWire;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;


/** Starts Deezer on an offscreen display for Flow requests or quiet launcher warm-up. */
final class BackgroundDeezerFlow {
    private static final String PACKAGE = "deezer.android.app";
    private static final long FLOW_DEBOUNCE_MS = 1200L;
    private static final long WARM_UP_DEBOUNCE_MS = 30000L;
    private static final long SESSION_TIMEOUT_MS = 20000L;

    private enum Mode { IDLE, FLOW, WARM_UP }

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastRequest = -1200L;
    private long lastWarmUpRequest = -30000L;
    private long sessionDeadline;
    private boolean pending;
    private Mode mode = Mode.IDLE;
    private Runnable flowRequested;
    private VirtualDisplay display;
    private ImageReader images;
    private final Runnable delayedRelease = this::releaseNow;

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BOOP-background-flow");
        t.setDaemon(true);
        return t;
    });

    BackgroundDeezerFlow(Context context) { this.context = context.getApplicationContext(); }

    boolean request(Runnable requested) {
        long now = SystemClock.elapsedRealtime();
        if (pending) {
            if (mode == Mode.WARM_UP) {
                mode = Mode.FLOW;
                flowRequested = requested;
                lastRequest = now;
                sessionDeadline = now + SESSION_TIMEOUT_MS;
                handler.removeCallbacks(delayedRelease);
            }
            return true;
        }
        if (now - lastRequest < FLOW_DEBOUNCE_MS) return true;
        try {
            if (play(requested)) { lastRequest = now; return true; }
            if (display != null) release();
            return startHidden(Mode.FLOW, requested);
        } catch (RuntimeException unavailable) {
            android.util.Log.w("BOOPFlow", "Unable to request Deezer Flow", unavailable);
            return false;
        }
    }

    /** Quietly opens Deezer after Shield Home starts, without sending any playback command. */
    void warmUp() {
        long now = SystemClock.elapsedRealtime();
        if (pending || display != null || now - lastWarmUpRequest < WARM_UP_DEBOUNCE_MS) return;
        if (hasDeezerSession() || isAnyMediaPlaying()) return;
        lastWarmUpRequest = now;
        startHidden(Mode.WARM_UP, null);
    }

    private boolean startHidden(Mode launchMode, Runnable requested) {
        try {
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
            mode = launchMode;
            flowRequested = requested;
            long now = SystemClock.elapsedRealtime();
            sessionDeadline = now + SESSION_TIMEOUT_MS;
            if (launchMode == Mode.FLOW) lastRequest = now;
            int displayId = display.getDisplay().getDisplayId();
            executor.execute(() -> wakePlayer(displayId));
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

    private boolean hasDeezerSession() {
        MediaSessionManager sessions = context.getSystemService(MediaSessionManager.class);
        if (sessions == null) return false;
        try {
            for (MediaController controller : sessions.getActiveSessions(
                    new ComponentName(context, ShieldNowPlayingListenerService.class))) {
                if (PACKAGE.equals(controller.getPackageName())) return true;
            }
        } catch (RuntimeException unavailable) {
            android.util.Log.d("BOOPFlow", "Unable to inspect Deezer sessions during warm-up", unavailable);
        }
        return false;
    }

    private boolean isAnyMediaPlaying() {
        MediaSessionManager sessions = context.getSystemService(MediaSessionManager.class);
        if (sessions == null) return false;
        try {
            for (MediaController controller : sessions.getActiveSessions(
                    new ComponentName(context, ShieldNowPlayingListenerService.class))) {
                PlaybackState state = controller.getPlaybackState();
                if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                    return true;
                }
            }
        } catch (RuntimeException unavailable) {
            // Ignored
        }
        return false;
    }

    private void wakePlayer(int displayId) {
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
            handler.postDelayed(this::poll, 1000);
        } catch (Exception unavailable) {
            android.util.Log.w("BOOPFlow", "Hidden Deezer player unavailable", unavailable);
            handler.post(() -> failPending());
        }
    }

    private void poll() {
        if (!pending) return;
        if (mode == Mode.WARM_UP) {
            if (hasDeezerSession()) {
                finishWarmUp(true);
            } else if (SystemClock.elapsedRealtime() < sessionDeadline) {
                handler.postDelayed(this::poll, 200);
            } else {
                finishWarmUp(false);
            }
            return;
        }
        if (mode != Mode.FLOW) return;

        Runnable requested = flowRequested;
        try {
            if (play(requested)) {
                awaitPlayback(requested, sessionDeadline, SystemClock.elapsedRealtime(), 0);
                return;
            }
            if (SystemClock.elapsedRealtime() < sessionDeadline) {
                handler.postDelayed(this::poll, 100);
                return;
            }
        } catch (RuntimeException unavailable) {
            android.util.Log.w("BOOPFlow", "Deezer session unavailable", unavailable);
        }
        failPending();
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
                    mode = Mode.IDLE;
                    flowRequested = null;
                    // Defer virtual display teardown by 1200ms so Deezer's detached background
                    // playback service firmly locks in foreground priority before the activity dies.
                    scheduleRelease();
                    if (requested != null) requested.run();
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
        failPending();
    }

    private void finishWarmUp(boolean sessionFound) {
        pending = false;
        mode = Mode.IDLE;
        flowRequested = null;
        if (sessionFound) scheduleRelease();
        else release();
    }

    private void failPending() {
        boolean flowFailed = mode == Mode.FLOW;
        pending = false;
        mode = Mode.IDLE;
        flowRequested = null;
        release();
        if (flowFailed) {
            android.widget.Toast.makeText(context, "Deezer Flow is unavailable.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleRelease() {
        handler.removeCallbacks(delayedRelease);
        handler.postDelayed(delayedRelease, 1200);
    }

    private void release() {
        handler.removeCallbacks(delayedRelease);
        releaseNow();
    }

    private void releaseNow() {
        if (display != null) { display.release(); display = null; }
        if (images != null) { images.close(); images = null; }
    }
}
