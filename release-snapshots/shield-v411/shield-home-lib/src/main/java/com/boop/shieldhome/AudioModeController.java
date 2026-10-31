package com.boop.shieldhome;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

final class AudioModeController {
    private static final String TAG = "BOOP-AudioMode";
    private static final String PARAM = "nv_param_audio_native_sample_rate_select=";
    private static volatile AudioModeController instance;

    static AudioModeController get(Context context) {
        AudioModeController local = instance;
        if (local == null) {
            synchronized (AudioModeController.class) {
                local = instance;
                if (local == null) {
                    local = new AudioModeController(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    private final AudioManager audioManager;
    private AudioModePolicy.Mode foregroundLaunch = AudioModePolicy.Mode.IGNORE;

    private AudioModeController(Context context) {
        audioManager = context.getSystemService(AudioManager.class);
    }

    synchronized void applyLaunch(AudioModePolicy.Mode mode) {
        foregroundLaunch = mode == null ? AudioModePolicy.Mode.IGNORE : mode;
        applyResolved(foregroundLaunch);
    }

    synchronized void applyPlayback(AudioModePolicy.Mode mode) {
        applyResolved(AudioModePolicy.arbitrate(foregroundLaunch, mode));
    }

    synchronized void clearForegroundLaunch() {
        foregroundLaunch = AudioModePolicy.Mode.IGNORE;
        // Home immediately re-evaluates the live session; do not replay a stale mode.
    }

    synchronized void resume(String packageName, Runnable play) {
        // Restore the route before Deezer creates/resumes its AudioTrack.
        applyPlayback(AudioModePolicy.forResume(packageName));
        play.run();
    }

    private void applyResolved(AudioModePolicy.Mode mode) {
        if (mode == null || mode == AudioModePolicy.Mode.IGNORE || audioManager == null) return;
        String value = mode == AudioModePolicy.Mode.NATIVE_MUSIC ? "1" : "0";
        try {
            String current = audioManager.getParameters("nv_param_audio_native_sample_rate_select");
            if ((PARAM + value).equals(current)) return;
            audioManager.setParameters(PARAM + value);
            Log.i(TAG, "native sample-rate mode=" + value);
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "native sample-rate mode change rejected");
        }
    }

    private AudioModeController() { audioManager = null; }
}
