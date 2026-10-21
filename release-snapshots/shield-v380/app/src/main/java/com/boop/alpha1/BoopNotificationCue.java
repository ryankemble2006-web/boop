package com.boop.alpha1;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

final class BoopNotificationCue {
    private static final int SAMPLE_RATE = 44_100;
    private static final long RELEASE_DELAY_MS = 420L;
    private static final long[] VIBRATION_WAVEFORM_MS = {0L, 35L, 55L, 28L};

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());

    BoopNotificationCue(Context context) {
        if (context == null) throw new IllegalArgumentException("context required");
        this.context = context.getApplicationContext();
    }

    void play() {
        playSound();
        playVibration();
    }

    private void playSound() {
        short[] pcm = BoopNotificationCueRenderer.render(SAMPLE_RATE);
        AudioAttributes attributes = notificationAttributes();
        AudioTrack track = null;
        try {
            track = new AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(pcm.length * Short.BYTES)
                    .build();
            int written = track.write(pcm, 0, pcm.length);
            if (written <= 0) {
                track.release();
                return;
            }
            track.play();
            AudioTrack playingTrack = track;
            handler.postDelayed(() -> release(playingTrack), RELEASE_DELAY_MS);
        } catch (RuntimeException unavailable) {
            release(track);
        }
    }

    private void playVibration() {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) return;
            vibrator.vibrate(
                    VibrationEffect.createWaveform(VIBRATION_WAVEFORM_MS, -1),
                    notificationAttributes());
        } catch (RuntimeException ignored) {
            // Visual notification presentation remains available without haptics.
        }
    }

    private static AudioAttributes notificationAttributes() {
        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
    }

    private static void release(AudioTrack track) {
        if (track == null) return;
        try {
            track.release();
        } catch (RuntimeException ignored) {
            // The platform may already have reclaimed audio resources.
        }
    }
}
