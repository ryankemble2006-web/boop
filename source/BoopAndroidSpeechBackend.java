package com.boop.alpha1;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

final class BoopAndroidSpeechBackend implements BoopSpeechBackend {
    private final TextToSpeech tts;
    private final AtomicLong utteranceCounter = new AtomicLong();
    private final ConcurrentHashMap<String, Callback> callbacks = new ConcurrentHashMap<>();
    private volatile boolean released;

    BoopAndroidSpeechBackend(TextToSpeech tts) {
        this.tts = tts;
        this.tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                finish(utteranceId, null);
            }

            @Override
            public void onError(String utteranceId) {
                finish(utteranceId, new IllegalStateException("Android TTS failed"));
            }

            @Override
            public void onError(String utteranceId, int errorCode) {
                finish(utteranceId, new IllegalStateException("Android TTS failed: " + errorCode));
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                finish(utteranceId, null);
            }
        });
    }

    @Override
    public boolean speak(String text, int speakerId, float pitch, float rate, Callback callback) {
        if (released || tts == null || text == null || text.isEmpty() || callback == null) {
            return false;
        }
        tts.setPitch(BoopVoiceTuning.clampPitch(pitch));
        tts.setSpeechRate(BoopVoiceTuning.clampRate(rate));
        String utteranceId = "boop-android-" + utteranceCounter.incrementAndGet();
        callbacks.put(utteranceId, callback);
        int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        if (result == TextToSpeech.ERROR) {
            callbacks.remove(utteranceId);
            return false;
        }
        return true;
    }

    @Override
    public void stop() {
        if (!released && tts != null) {
            tts.stop();
        }
    }

    @Override
    public void release() {
        released = true;
        callbacks.clear();
    }

    private void finish(String utteranceId, Throwable error) {
        Callback callback = callbacks.remove(utteranceId);
        if (callback == null) return;
        if (error == null) {
            callback.onDone();
        } else {
            callback.onError(error);
        }
    }
}
