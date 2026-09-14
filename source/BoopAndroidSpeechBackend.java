package com.boop.alpha1;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** One current reply, including the period before Android finishes initializing speech. */
final class BoopAndroidSpeechBackend implements BoopSpeechBackend {
    private static final String TAG = "BOOP-Speech";
    private static final long STARTUP_TIMEOUT_MS = 10_000L;
    private final TextToSpeech tts;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicLong utteranceCounter = new AtomicLong();
    private final ConcurrentHashMap<String, Callback> callbacks = new ConcurrentHashMap<>();
    private volatile boolean released;
    // Requests, readiness and cancellation are owned by the main thread.
    private int initialization; // 0 waiting, 1 ready, -1 failed
    private Pending pending;
    private final Runnable startupTimeout = () -> {
        Pending expired = pending;
        cancelPending();
        if (expired != null && !released) {
            Log.w(TAG, "Android reply initialization timed out");
            expired.callback.onError(new IllegalStateException("Speech initialization timed out"));
        }
    };

    private static final class Pending {
        final String text;
        final float pitch, rate;
        final Callback callback;
        Pending(String text, float pitch, float rate, Callback callback) {
            this.text = text;
            this.pitch = pitch;
            this.rate = rate;
            this.callback = callback;
        }
    }

    BoopAndroidSpeechBackend(TextToSpeech tts) {
        this.tts = tts;
        this.tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {
                if (!released && callbacks.containsKey(id)) Log.i(TAG, "Android reply playback started");
            }
            @Override public void onDone(String id) { finish(id, null); }
            @Override public void onError(String id) {
                finish(id, new IllegalStateException("Android TTS failed"));
            }
            @Override public void onError(String id, int code) {
                finish(id, new IllegalStateException("Android TTS failed: " + code));
            }
            @Override public void onStop(String id, boolean interrupted) {
                finish(id, new IllegalStateException("Android TTS interrupted"));
            }
        });
    }

    void onInitializationFinished(boolean ready) {
        handler.post(() -> {
            if (released) return;
            initialization = ready ? 1 : -1;
            Log.i(TAG, ready ? "Android speech ready" : "Android speech initialization failed");
            Pending waiting = pending;
            cancelPending();
            if (waiting == null) return;
            if (!ready || !start(waiting)) {
                waiting.callback.onError(new IllegalStateException("Android speech unavailable"));
            }
        });
    }

    @Override public boolean speak(String text, int speakerId, float pitch, float rate, Callback callback) {
        if (released || tts == null || text == null || text.isEmpty() || callback == null
                || initialization < 0) return false;
        stop(); // Invalidate older callbacks before QUEUE_FLUSH can report their onStop.
        Pending request = new Pending(text, pitch, rate, callback);
        if (initialization == 0) {
            pending = request;
            handler.postDelayed(startupTimeout, STARTUP_TIMEOUT_MS);
            Log.i(TAG, "Android reply queued until speech initialization");
            return true;
        }
        return start(request);
    }

    private boolean start(Pending request) {
        if (released) return false;
        String id = "boop-android-" + utteranceCounter.incrementAndGet();
        try {
            tts.setPitch(BoopVoiceTuning.clampPitch(request.pitch));
            tts.setSpeechRate(BoopVoiceTuning.clampRate(request.rate));
            callbacks.put(id, request.callback);
            if (tts.speak(request.text, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.ERROR) {
                Log.i(TAG, "Android reply submitted");
                return true;
            }
        } catch (RuntimeException failure) {
            Log.w(TAG, "Android reply submission failed", failure);
        }
        callbacks.remove(id);
        return false;
    }

    /** Cancel only startup work when the owning screen leaves the foreground. */
    void cancelPending() {
        pending = null;
        handler.removeCallbacks(startupTimeout);
    }

    @Override public void stop() {
        cancelPending();
        callbacks.clear();
        if (!released && tts != null && initialization == 1) tts.stop();
    }

    @Override public void release() {
        released = true;
        cancelPending();
        callbacks.clear();
        handler.removeCallbacksAndMessages(null);
    }

    private void finish(String id, Throwable error) {
        handler.post(() -> {
            Callback callback = callbacks.remove(id);
            if (released || callback == null) return;
            if (error == null) {
                Log.i(TAG, "Android reply playback completed");
                callback.onDone();
            } else {
                Log.w(TAG, "Android reply playback failed", error);
                callback.onError(error);
            }
        });
    }
}
