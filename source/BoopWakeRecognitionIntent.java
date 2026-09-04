package com.boop.alpha1;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.ParcelFileDescriptor;
import android.speech.RecognizerIntent;

import java.util.Locale;

final class BoopWakeRecognitionIntent {
    static final int SAMPLE_RATE_HZ = 16_000;
    static final int CHANNEL_COUNT = 1;
    static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private BoopWakeRecognitionIntent() { }

    static Intent build(Locale locale, ParcelFileDescriptor source) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, source);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, CHANNEL_COUNT);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, ENCODING);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, SAMPLE_RATE_HZ);
        return intent;
    }
}
