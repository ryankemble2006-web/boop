package com.boop.alpha1;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.k2fsa.sherpa.onnx.GeneratedAudio;
import com.k2fsa.sherpa.onnx.GenerationConfig;
import com.k2fsa.sherpa.onnx.OfflineTts;
import com.k2fsa.sherpa.onnx.OfflineTtsConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class BoopNaturalSpeechBackend implements BoopSpeechBackend {
    private static final float SILENCE_SCALE = 0.2f;
    private static final int HIGHEST_REQUIRED_SPEAKER_ID = 26;
    private static final String[] SHERPA_RUNTIME_FILES = {
            "model.onnx",
            "voices.bin",
            "tokens.txt",
            "lexicon-gb-en.txt"
    };

    static final class NaturalSpeechException extends RuntimeException {
        private final String stage;

        NaturalSpeechException(String stage, Throwable cause) {
            super("Natural voice " + stage + " failed", cause);
            this.stage = stage;
        }

        String stage() {
            return stage;
        }
    }

    private static final class RequestState {
        final Callback callback;
        final AtomicBoolean terminal = new AtomicBoolean(false);
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        RequestState(Callback callback) {
            this.callback = callback;
        }

        void done() {
            if (terminal.compareAndSet(false, true)) callback.onDone();
        }

        void error(Throwable error) {
            if (terminal.compareAndSet(false, true)) callback.onError(error);
        }
    }

    private final BoopNaturalVoicePack pack;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "boop-natural-tts");
        thread.setDaemon(true);
        return thread;
    });
    private final Object lock = new Object();

    private OfflineTts offlineTts;
    private AudioTrack activeTrack;
    private RequestState activeRequest;
    private boolean released;

    BoopNaturalSpeechBackend(BoopNaturalVoicePack pack) {
        this.pack = pack;
    }

    @Override
    public boolean speak(String text, int speakerId, float pitch, float rate, Callback callback) {
        if (text == null || text.isEmpty() || callback == null || speakerId < 0 || !pack.isInstalled()) {
            return false;
        }
        final RequestState request = new RequestState(callback);
        RequestState interrupted = null;
        synchronized (lock) {
            if (released) return false;
            if (activeRequest != null) {
                interrupted = activeRequest;
                stopLocked(interrupted, true);
            }
            activeRequest = request;
        }
        if (interrupted != null) interrupted.done();
        try {
            executor.execute(() -> synthesizeAndPlay(
                    request,
                    text,
                    speakerId,
                    speedForRate(rate)));
            return true;
        } catch (RuntimeException rejected) {
            synchronized (lock) {
                if (activeRequest == request) activeRequest = null;
            }
            return false;
        }
    }

    @Override
    public void stop() {
        RequestState request;
        synchronized (lock) {
            request = activeRequest;
            if (request == null) return;
            stopLocked(request, true);
        }
        request.done();
    }

    @Override
    public void release() {
        OfflineTts ttsToRelease;
        synchronized (lock) {
            if (released) return;
            released = true;
            if (activeRequest != null) {
                stopLocked(activeRequest, false);
                activeRequest = null;
            }
            ttsToRelease = offlineTts;
            offlineTts = null;
        }
        executor.shutdownNow();
        if (ttsToRelease != null) {
            try { ttsToRelease.release(); } catch (Throwable ignored) { }
        }
    }

    static float speedForRate(float rate) {
        return BoopVoiceTuning.clampRate(rate);
    }

    // Retained for source/API compatibility with existing focused tests. Natural
    // pitch is not applied until the Android playback path is physically proven.
    static float pitchForPlayback(float pitch) {
        return BoopVoiceTuning.clampPitch(pitch);
    }

    static short[] toPcm16(float[] samples) {
        short[] pcm = new short[samples.length];
        for (int i = 0; i < samples.length; i++) {
            float clipped = Math.max(-1.0f, Math.min(1.0f, samples[i]));
            pcm[i] = (short) Math.round(clipped * 32767.0f);
        }
        return pcm;
    }

    static boolean runtimeFilesReadyForSherpa(File root) {
        if (root == null || !root.isDirectory() || !root.canRead()) return false;
        for (String relative : SHERPA_RUNTIME_FILES) {
            File file = new File(root, relative);
            if (!file.isFile() || !file.canRead() || file.length() <= 0L) return false;
        }
        File espeak = new File(root, "espeak-ng-data");
        String[] entries = espeak.list();
        return espeak.isDirectory()
                && espeak.canRead()
                && entries != null
                && entries.length > 0;
    }

    private void synthesizeAndPlay(
            RequestState request,
            String text,
            int speakerId,
            float speed) {
        GeneratedAudio audio;
        try {
            OfflineTts tts = ensureTts();
            if (request.cancelled.get()) return;

            GenerationConfig generation = new GenerationConfig();
            generation.setSid(speakerId);
            generation.setSpeed(speed);
            generation.setSilenceScale(SILENCE_SCALE);
            // Sherpa-ONNX 1.13.7's Android callback JNI bridge can abort the
            // process. Generate without that callback and honor cancellation
            // immediately after synthesis instead.
            audio = tts.generateWithConfig(text, generation);
            if (request.cancelled.get()) return;
            if (audio == null || audio.getSamples() == null || audio.getSamples().length == 0) {
                throw new IllegalStateException("Natural speech produced no audio");
            }
        } catch (Throwable error) {
            if (!request.cancelled.get()) {
                clearIfActive(request);
                NaturalSpeechException failure = error instanceof NaturalSpeechException
                        ? (NaturalSpeechException) error
                        : new NaturalSpeechException("synthesis", error);
                request.error(failure);
            }
            return;
        }

        try {
            play(request, audio.getSamples(), audio.getSampleRate());
        } catch (Throwable error) {
            if (!request.cancelled.get()) {
                clearIfActive(request);
                request.error(new NaturalSpeechException("playback", error));
            }
        }
    }

    private OfflineTts ensureTts() {
        synchronized (lock) {
            if (released) throw new IllegalStateException("Natural speech backend is closed");
            if (offlineTts != null) return offlineTts;
            File root = pack.activeDirectory();
            if (!runtimeFilesReadyForSherpa(root)) {
                throw new NaturalSpeechException(
                        "files",
                        new IllegalStateException("Natural voice runtime files are incomplete"));
            }
            File lexicon = new File(root, "lexicon-gb-en.txt");

            try {
                // The Android AAR exposes Sherpa's Kotlin data classes to Java as
                // no-arg objects with bean setters, not the desktop Java builders.
                OfflineTtsKokoroModelConfig kokoro = new OfflineTtsKokoroModelConfig();
                kokoro.setModel(new File(root, "model.onnx").getAbsolutePath());
                kokoro.setVoices(new File(root, "voices.bin").getAbsolutePath());
                kokoro.setTokens(new File(root, "tokens.txt").getAbsolutePath());
                kokoro.setDataDir(new File(root, "espeak-ng-data").getAbsolutePath());
                kokoro.setLexicon(lexicon.getAbsolutePath());

                OfflineTtsModelConfig model = new OfflineTtsModelConfig();
                model.setKokoro(kokoro);
                model.setNumThreads(2);
                model.setDebug(false);
                model.setProvider("cpu");

                OfflineTtsConfig config = new OfflineTtsConfig();
                config.setModel(model);
                config.setSilenceScale(SILENCE_SCALE);

                // A null AssetManager tells Sherpa to load the app-private absolute
                // file paths above instead of looking in APK assets.
                OfflineTts candidate = new OfflineTts(null, config);
                int sampleRate = candidate.sampleRate();
                int speakerCount = candidate.numSpeakers();
                if (sampleRate <= 0 || speakerCount <= HIGHEST_REQUIRED_SPEAKER_ID) {
                    try { candidate.release(); } catch (Throwable ignored) { }
                    throw new IllegalStateException(
                            "Natural voice model opened with invalid runtime metadata");
                }
                offlineTts = candidate;
                return offlineTts;
            } catch (NaturalSpeechException failure) {
                throw failure;
            } catch (Throwable error) {
                throw new NaturalSpeechException("initialization", error);
            }
        }
    }

    private void play(RequestState request, float[] samples, int sampleRate)
            throws InterruptedException {
        short[] pcm = toPcm16(samples);
        int minimumBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int bufferBytes = Math.max(pcm.length * 2, Math.max(4096, minimumBytes));
        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
                .build();
        if (track.getState() != AudioTrack.STATE_INITIALIZED) {
            track.release();
            throw new IllegalStateException("Natural speech audio output unavailable");
        }

        synchronized (lock) {
            if (released || activeRequest != request || request.cancelled.get()) {
                track.release();
                return;
            }
            activeTrack = track;
        }

        int written = track.write(pcm, 0, pcm.length, AudioTrack.WRITE_BLOCKING);
        if (written != pcm.length) {
            throw new IllegalStateException("Natural speech audio output was incomplete");
        }
        track.play();

        while (!request.cancelled.get()
                && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                && Integer.toUnsignedLong(track.getPlaybackHeadPosition()) < pcm.length) {
            Thread.sleep(20L);
        }
        if (!request.cancelled.get()) {
            clearTrack(track, request);
            request.done();
        }
    }

    private void stopLocked(RequestState request, boolean keepTerminalCallback) {
        request.cancelled.set(true);
        AudioTrack track = activeTrack;
        activeTrack = null;
        if (activeRequest == request) activeRequest = null;
        if (track != null) {
            try { track.pause(); } catch (Throwable ignored) { }
            try { track.flush(); } catch (Throwable ignored) { }
            try { track.stop(); } catch (Throwable ignored) { }
            try { track.release(); } catch (Throwable ignored) { }
        }
        if (!keepTerminalCallback) request.terminal.set(true);
    }

    private void clearTrack(AudioTrack track, RequestState request) {
        synchronized (lock) {
            if (activeTrack == track) activeTrack = null;
            if (activeRequest == request) activeRequest = null;
        }
        try { track.stop(); } catch (Throwable ignored) { }
        try { track.release(); } catch (Throwable ignored) { }
    }

    private void clearIfActive(RequestState request) {
        synchronized (lock) {
            if (activeRequest == request) activeRequest = null;
            AudioTrack track = activeTrack;
            activeTrack = null;
            if (track != null) {
                try { track.stop(); } catch (Throwable ignored) { }
                try { track.release(); } catch (Throwable ignored) { }
            }
        }
    }
}
