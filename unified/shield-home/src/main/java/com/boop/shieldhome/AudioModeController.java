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
    private AudioModePolicy.Mode lastMode = AudioModePolicy.Mode.IGNORE;

    private AudioModeController(Context context) {
        audioManager = context.getSystemService(AudioManager.class);
    }

    synchronized void apply(AudioModePolicy.Mode mode) {
        if (mode == null || mode == AudioModePolicy.Mode.IGNORE || mode == lastMode || audioManager == null) return;
        String value = mode == AudioModePolicy.Mode.NATIVE_MUSIC ? "1" : "0";
        try {
            audioManager.setParameters(PARAM + value);
            lastMode = mode;
            Log.i(TAG, "native sample-rate mode=" + value);
        } catch (RuntimeException unavailable) {
            Log.w(TAG, "native sample-rate mode change rejected");
        }
    }

    private AudioModeController() { audioManager = null; }
}
