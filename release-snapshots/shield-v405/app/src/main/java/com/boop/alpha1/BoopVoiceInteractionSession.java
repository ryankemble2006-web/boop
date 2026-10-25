package com.boop.alpha1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;

/** Delegates the Android assistant invocation into BOOP's existing one-shot recognizer. */
final class BoopVoiceInteractionSession extends VoiceInteractionSession {
    private static final String TAG = "BOOP-Assist";
    static final String EXTRA_ONE_SHOT_ASSIST = "com.boop.alpha1.extra.ONE_SHOT_ASSIST";

    BoopVoiceInteractionSession(Context context) {
        super(context);
    }

    @Override public void onCreate() {
        super.onCreate();
        setUiEnabled(false);
    }

    @Override public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Intent intent = new Intent(Intent.ACTION_ASSIST)
                .setClassName(getContext().getPackageName(), "com.boop.alpha1.MainActivity")
                .putExtra(EXTRA_ONE_SHOT_ASSIST, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (args != null && args.containsKey(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID)) {
            intent.putExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID,
                    args.getInt(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, -1));
        }
        try {
            startVoiceActivity(intent);
        } catch (RuntimeException unavailable) {
            Log.e(TAG, "Could not start BOOP assistant voice activity", unavailable);
            finish();
        }
    }
}
