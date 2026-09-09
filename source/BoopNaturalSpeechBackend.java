package com.boop.alpha1;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.PlaybackParams;

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
                    pitchForPlayback(pitch),
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

    static float pitchForPlayback(float pitch) {
        return BoopVoiceTuning.clampPitch(pitch);
    }

    private void synthesizeAndPlay(
            RequestState request,
            String text,
            int speakerId,
            float pitch,
            float speed) {
        try {
            OfflineTts tts = ensureTts();
            if (request.cancelled.get()) return;

            GenerationConfig generation = new GenerationConfig();
            generation.setSid(speakerId);
            generation.setSpeed(speed);
            generation.setSilenceScale(SILENCE_SCALE);
            GeneratedAudio audio = tts.generateWithConfigAndCallback(text, generation, samples ->
                    request.cancelled.get() ? 0 : 1);
            if (request.cancelled.get()) return;
            if (audio == null || audio.getSamples() == null || audio.getSamples().length == 0) {
                throw new IllegalStateException("Natural speech produced no audio");
            }
            play(request, audio.getSamples(), audio.getSampleRate(), pitch);
        } catch (Throwable error) {
            if (!request.cancelled.get()) {
                clearIfActive(request);
                request.error(error);
            }
        }
    }

    private OfflineTts ensureTts() {
        synchronized (lock) {
            if (released) throw new IllegalStateException("Natural speech backend is closed");
            if (offlineTts != null) return offlineTts;
            File root = pack.activeDirectory();
            File lexicon = new File(root, "lexicon-gb-en.txt");
            OfflineTtsKokoroModelConfig kokoro = OfflineTtsKokoroModelConfig.builder()
                    .setModel(new File(root, "model.onnx").getAbsolutePath())
                    .setVoices(new File(root, "voices.bin").getAbsolutePath())
                    .setTokens(new File(root, "tokens.txt").getAbsolutePath())
                    .setDataDir(new File(root, "espeak-ng-data").getAbsolutePath())
                    .setLexicon(lexicon.getAbsolutePath())
                    .setLang("eng")
                    .build();
            OfflineTtsModelConfig model = OfflineTtsModelConfig.builder()
                    .setKokoro(kokoro)
                    .setNumThreads(2)
                    .setDebug(false)
                    .setProvider("cpu")
                    .build();
            OfflineTtsConfig config = OfflineTtsConfig.builder()
                    .setModel(model)
                    .setSilenceScale(SILENCE_SCALE)
                    .build();
            offlineTts = new OfflineTts(config);
            return offlineTts;
        }
    }

    private void play(RequestState request, float[] samples, int sampleRate, float pitch)
            throws InterruptedException {
        int minimumBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT);
        int bufferBytes = Math.max(samples.length * 4, Math.max(4096, minimumBytes));
        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
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

        int written = track.write(samples, 0, samples.length, AudioTrack.WRITE_BLOCKING);
        if (written != samples.length) {
            throw new IllegalStateException("Natural speech audio output was incomplete");
        }
        PlaybackParams playback = new PlaybackParams()
                .allowDefaults()
                .setPitch(pitch)
                .setSpeed(1.0f);
        track.setPlaybackParams(playback);
        track.play();

        while (!request.cancelled.get()
                && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                && Integer.toUnsignedLong(track.getPlaybackHeadPosition()) < samples.length) {
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
