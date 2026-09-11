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

    private AudioModeController(Context context) {
        audioManager = context.getSystemService(AudioManager.class);
    }

    synchronized void apply(AudioModePolicy.Mode mode) {
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
