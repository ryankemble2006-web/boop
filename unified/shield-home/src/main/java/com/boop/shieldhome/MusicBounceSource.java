package com.boop.shieldhome;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

/** Output-mix visualization only. No room microphone, audio focus changes or saved audio. */
final class MusicBounceSource {
    private static final String TAG = "BOOP-MusicBounce";
    private static final long POLL_MS = 33L;
    private static final long STALE_MS = 250L;
    private final Context context;
    private Session session;

    MusicBounceSource(Context context) {
        this.context = context.getApplicationContext();
    }

    void setActive(boolean active) {
        if (!active) { stop(); return; }
        if (session != null) return;
        session = new Session(context);
        session.start();
    }

    float level(long nowMs) {
        Session current = session;
        if (current == null) return 0f;
        Sample sample = current.sample;
        return sample.timeMs < 0 || nowMs < sample.timeMs || nowMs - sample.timeMs > STALE_MS
                ? 0f : sample.level;
    }

    boolean unavailable() {
        return session != null && session.unavailable;
    }

    void stop() {
        Session previous = session;
        session = null; // An old worker can never deliver a level to a later playback session.
        if (previous != null) previous.stop();
    }

    private static final class Sample {
        static final Sample SILENT = new Sample(0f, -1L);
        final float level;
        final long timeMs;
        Sample(float level, long timeMs) { this.level = level; this.timeMs = timeMs; }
    }

    private static final class Session implements Runnable {
        final Context context;
        final HandlerThread thread = new HandlerThread("BOOP-MusicLevels", Process.THREAD_PRIORITY_BACKGROUND);
        Handler handler;
        Visualizer visualizer;
        byte[] waveform;
        volatile Sample sample = Sample.SILENT;
        volatile boolean closed;
        volatile boolean unavailable;
        long retryAtMs;
        long silentSinceMs = -1L;
        final DirectMusicSource direct;
        boolean diagnosticMode;
        boolean reportedSignal;

        Session(Context context) { this.context = context; direct = new DirectMusicSource(context); }

        void start() {
            thread.start();
            handler = new Handler(thread.getLooper());
            handler.post(this);
        }

        @Override public void run() {
            if (closed) return;
            long now = SystemClock.uptimeMillis();
            try {
                if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    sample = Sample.SILENT;
                    diagnosticMode = false;
                    silentSinceMs = -1L;
                    release();
                } else if (!diagnosticMode || now >= retryAtMs) {
                    if (visualizer == null) open();
                    int result = visualizer.getWaveForm(waveform);
                    if (result != Visualizer.SUCCESS) throw new IllegalStateException("waveform status " + result);
                    float level = MusicBounceEnvelope.levelOf(waveform);
                    sample = new Sample(level, now);
                    unavailable = false;
                    diagnosticMode = false;
                    if (level > 0f) silentSinceMs = -1L;
                    else {
                        if (silentSinceMs < 0L) silentSinceMs = now;
                        if (now - silentSinceMs >= 1000L) {
                            // Some vendor routes leave an enabled Visualizer returning only silence.
                            release();
                            diagnosticMode = true;
                            retryAtMs = now + 10000L;
                        }
                    }
                    if (level > 0f && !reportedSignal) {
                        reportedSignal = true;
                        Log.i(TAG, "Actual output-mix music levels received");
                    }
                }
            } catch (RuntimeException | LinkageError failure) {
                sample = Sample.SILENT;
                release();
                retryAtMs = now + 10000L;
                diagnosticMode = true;
                if (!unavailable) Log.w(TAG, "Android output visualization unavailable: " + failure.getClass().getSimpleName());
                unavailable = true;
            }
            if (!closed && diagnosticMode) {
                float level = direct.read();
                sample = level >= 0f ? new Sample(level, SystemClock.uptimeMillis()) : Sample.SILENT;
                unavailable = level < 0f;
            }
            if (!closed) handler.postDelayed(this, diagnosticMode ? 150L : POLL_MS);
        }

        private void open() {
            visualizer = new Visualizer(0);
            int[] range = Visualizer.getCaptureSizeRange();
            int size = Math.max(range[0], Math.min(range[1], 512));
            if (visualizer.getEnabled()) visualizer.setEnabled(false);
            if (visualizer.setCaptureSize(size) != Visualizer.SUCCESS)
                throw new IllegalStateException("capture size unavailable");
            visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED);
            waveform = new byte[size];
            if (visualizer.setEnabled(true) != Visualizer.SUCCESS)
                throw new IllegalStateException("visualizer activation unavailable");
        }

        void stop() {
            closed = true;
            direct.close();
            sample = Sample.SILENT;
            handler.removeCallbacks(this);
            // All native access and release stay on the same worker, not the remote/UI thread.
            handler.post(() -> {
                release();
                thread.quitSafely();
            });
        }

        private void release() {
            Visualizer old = visualizer;
            visualizer = null;
            waveform = null;
            if (old != null) {
                try { old.release(); }
                catch (RuntimeException | LinkageError ignored) { }
            }
        }
    }
}
