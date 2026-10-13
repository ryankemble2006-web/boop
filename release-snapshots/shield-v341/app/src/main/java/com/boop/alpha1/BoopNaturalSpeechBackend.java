package com.boop.alpha1;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.PlaybackParams;
import android.os.SystemClock;

import com.k2fsa.sherpa.onnx.GeneratedAudio;
import com.k2fsa.sherpa.onnx.GenerationConfig;
import com.k2fsa.sherpa.onnx.OfflineTts;
import com.k2fsa.sherpa.onnx.OfflineTtsConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class BoopNaturalSpeechBackend implements BoopSpeechBackend {
    private static final String LATENCY_TAG = "BOOP-VoiceLatency";
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
        final long startMs;
        final AtomicBoolean terminal = new AtomicBoolean(false);
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        RequestState(Callback callback) {
            this.callback = callback;
            this.startMs = SystemClock.elapsedRealtime();
        }

        void done() {
            if (terminal.compareAndSet(false, true)) callback.onDone();
        }

        void error(Throwable error) {
            if (terminal.compareAndSet(false, true)) callback.onError(error);
        }

        void cancel() {
            if (terminal.compareAndSet(false, true)) callback.onCancelled();
        }
    }

    private final BoopNaturalVoicePack pack;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "boop-natural-tts");
        thread.setDaemon(true);
        return thread;
    });
    private final Object lock = new Object();
    // Audio must not queue behind a running, non-interruptible native inference.
    private final ExecutorService playbackExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "boop-natural-playback");
        thread.setDaemon(true);
        return thread;
    });

    private OfflineTts offlineTts;
    private AudioTrack activeTrack;
    private RequestState activeRequest;
    private volatile boolean released;

    BoopNaturalSpeechBackend(BoopNaturalVoicePack pack) {
        this.pack = pack;
    }

    @Override
    public boolean speak(String text, int speakerId, float pitch, float rate, Callback callback) {
        if (text == null || text.isEmpty() || callback == null || speakerId < 0 || !pack.isInstalled()) {
            return false;
        }
        final RequestState request = new RequestState(callback);
        android.util.Log.i(
                LATENCY_TAG,
                "request_queued sid=" + speakerId + " chars=" + text.length());
        RequestState interrupted = null;
        synchronized (lock) {
            if (released) return false;
            if (activeRequest != null) {
                interrupted = activeRequest;
                stopLocked(interrupted, true);
            }
            activeRequest = request;
        }
        if (interrupted != null) interrupted.cancel();
        try {
            String preview = BoopNaturalVoicePreview.assetName(text, speakerId);
            if (preview != null) {
                playbackExecutor.execute(() -> playPreview(
                        request, preview, text, speakerId, pitchForPlayback(pitch), speedForRate(rate)));
            } else {
                queueSynthesis(request, text, speakerId, speedForRate(rate), pitchForPlayback(pitch));
            }
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
        request.cancel();
    }

    @Override
    public void release() {
        synchronized (lock) {
            if (released) return;
            released = true;
            if (activeRequest != null) {
                stopLocked(activeRequest, false);
                activeRequest = null;
            }
        }
        playbackExecutor.shutdownNow();
        // JNI generation cannot safely be interrupted. Dispose on its own worker,
        // after the current call returns, without blocking the Activity/main thread.
        executor.execute(() -> {
            if (offlineTts != null) {
                try { offlineTts.release(); } catch (Throwable ignored) { }
                offlineTts = null;
            }
        });
        executor.shutdown();
    }

    private void queueSynthesis(RequestState request, String text, int speakerId, float speed, float pitch) {
        executor.execute(() -> synthesizeAndPlay(request, text, speakerId, speed, pitch));
    }

    private void playPreview(RequestState request, String asset, String text, int speakerId, float pitch, float speed) {
        if (request.cancelled.get() || released) return;
        short[] pcm;
        try (InputStream input = pack.openPreview(asset)) {
            pcm = BoopNaturalVoicePreview.read(input);
        } catch (Exception unavailable) {
            if (request.cancelled.get() || released) return;
            android.util.Log.w("BOOP-NaturalVoice", "Voice demo unavailable; generating locally", unavailable);
            try { queueSynthesis(request, text, speakerId, speed, pitch); }
            catch (RuntimeException rejected) { fail(request, "synthesis", rejected); }
            return;
        }
        android.util.Log.i(LATENCY_TAG, "preview_ready total_ms=" + (SystemClock.elapsedRealtime() - request.startMs));
        playSafely(request, pcm, BoopNaturalVoicePreview.SAMPLE_RATE, pitch, speed);
    }

    private void playSafely(RequestState request, short[] pcm, int sampleRate, float pitch, float speed) {
        try { play(request, pcm, sampleRate, pitch, speed); }
        catch (Throwable error) { fail(request, "playback", error); }
    }

    private void fail(RequestState request, String stage, Throwable error) {
        if (!request.cancelled.get() && !released) {
            clearIfActive(request);
            request.error(error instanceof NaturalSpeechException ? error : new NaturalSpeechException(stage, error));
        }
    }

    static float speedForRate(float rate) {
        return BoopVoiceTuning.clampRate(rate);
    }

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
            float speed,
            float pitch) {
        GeneratedAudio audio;
        try {
            if (request.cancelled.get() || released) return;
            OfflineTts tts = ensureTts();
            long modelReadyMs = SystemClock.elapsedRealtime();
            android.util.Log.i(
                    LATENCY_TAG,
                    "model_ready total_ms=" + (modelReadyMs - request.startMs));
            if (request.cancelled.get()) return;

            GenerationConfig generation = new GenerationConfig();
            generation.setSid(speakerId);
            // Match the fixed demos: keep neural PCM neutral, then apply both
            // saved controls together through Android's independent pitch/speed.
            generation.setSpeed(1.0f);
            generation.setSilenceScale(SILENCE_SCALE);
            long synthesisStartMs = SystemClock.elapsedRealtime();
            // Sherpa-ONNX 1.13.7's Android callback JNI bridge can abort the
            // process. Generate without that callback and honor cancellation
            // immediately after synthesis instead.
            audio = tts.generateWithConfig(text, generation);
            long synthesisDoneMs = SystemClock.elapsedRealtime();
            android.util.Log.i(
                    LATENCY_TAG,
                    "synthesis_done stage_ms=" + (synthesisDoneMs - synthesisStartMs)
                            + " total_ms=" + (synthesisDoneMs - request.startMs));
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
            short[] pcm = toPcm16(audio.getSamples());
            playbackExecutor.execute(() -> playSafely(request, pcm, audio.getSampleRate(), pitch, speed));
        } catch (Throwable error) {
            if (!request.cancelled.get()) {
                clearIfActive(request);
                request.error(new NaturalSpeechException("playback", error));
            }
        }
    }

    private OfflineTts ensureTts() {
        // Only the synthesis executor accesses this model. Never hold the UI's
        // cancellation lock while opening the native model or running inference.
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
            OfflineTtsKokoroModelConfig kokoro = new OfflineTtsKokoroModelConfig();
            kokoro.setModel(new File(root, "model.onnx").getAbsolutePath());
            kokoro.setVoices(new File(root, "voices.bin").getAbsolutePath());
            kokoro.setTokens(new File(root, "tokens.txt").getAbsolutePath());
            kokoro.setDataDir(new File(root, "espeak-ng-data").getAbsolutePath());
            kokoro.setLexicon(lexicon.getAbsolutePath());

            OfflineTtsModelConfig model = new OfflineTtsModelConfig();
            model.setKokoro(kokoro);
            model.setNumThreads(4);
            model.setDebug(false);
            model.setProvider("cpu");

            OfflineTtsConfig config = new OfflineTtsConfig();
            config.setModel(model);
            config.setSilenceScale(SILENCE_SCALE);

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

    private void play(RequestState request, short[] pcm, int sampleRate, float pitch, float speed)
            throws InterruptedException {
        if (request.cancelled.get() || released) return;
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
        if (track.getState() == AudioTrack.STATE_UNINITIALIZED) {
            track.release();
            throw new IllegalStateException("Natural speech audio output unavailable");
        }

        try {
            synchronized (lock) {
                if (released || activeRequest != request || request.cancelled.get()) {
                    return;
                }
                activeTrack = track;
                int written = track.write(pcm, 0, pcm.length, AudioTrack.WRITE_BLOCKING);
                if (written != pcm.length) {
                    throw new IllegalStateException("Natural speech audio output was incomplete");
                }
                try {
                    PlaybackParams params = new PlaybackParams()
                            .allowDefaults()
                            .setSpeed(speedForRate(speed))
                            .setPitch(pitchForPlayback(pitch));
                    track.setPlaybackParams(params);
                } catch (RuntimeException unsupported) {
                    throw new IllegalStateException("Natural pitch/cadence unavailable", unsupported);
                }
                track.play();
                android.util.Log.i(
                        LATENCY_TAG,
                        "playback_start total_ms=" + (SystemClock.elapsedRealtime() - request.startMs)
                                + " pitch=" + pitch + " rate=" + speed);
            }

            while (!request.cancelled.get()) {
                synchronized (lock) {
                    if (request.cancelled.get() || activeTrack != track) break;
                    if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING
                            || Integer.toUnsignedLong(track.getPlaybackHeadPosition()) >= pcm.length) break;
                }
                Thread.sleep(20L);
            }
        } finally {
            clearTrack(track, request);
        }
        if (!request.cancelled.get() && !released) {
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
            if (activeRequest != request) return;
            activeRequest = null;
            AudioTrack track = activeTrack;
            activeTrack = null;
            if (track != null) {
                try { track.stop(); } catch (Throwable ignored) { }
                try { track.release(); } catch (Throwable ignored) { }
            }
        }
    }
}
