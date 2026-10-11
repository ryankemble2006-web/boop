package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;

final class BoopWakeSensitivity {
    static final int PROGRESS_MAX = 100;
    static final int DEFAULT_PROGRESS = 50;
    static final float MIN_KEYWORD_SCORE = 1.0f;
    static final float DEFAULT_KEYWORD_SCORE = 1.5f;
    static final float MAX_KEYWORD_SCORE = 2.0f;

    private static final String PREFS_NAME = "boop_wake";
    private static final String KEY_PROGRESS = "sensitivity_progress";

    private BoopWakeSensitivity() { }

    static int loadProgress(Context context) {
        if (context == null) {
            return DEFAULT_PROGRESS;
        }
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return clampProgress(preferences.getInt(KEY_PROGRESS, DEFAULT_PROGRESS));
    }

    static void saveProgress(Context context, int progress) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_PROGRESS, clampProgress(progress))
                .apply();
    }

    static float keywordScore(Context context) {
        return scoreFromProgress(loadProgress(context));
    }

    static float scoreFromProgress(int progress) {
        int bounded = clampProgress(progress);
        if (bounded == DEFAULT_PROGRESS) {
            return DEFAULT_KEYWORD_SCORE;
        }
        float fraction = bounded / (float) PROGRESS_MAX;
        return MIN_KEYWORD_SCORE + fraction * (MAX_KEYWORD_SCORE - MIN_KEYWORD_SCORE);
    }

    private static int clampProgress(int progress) {
        return Math.max(0, Math.min(PROGRESS_MAX, progress));
    }
}
