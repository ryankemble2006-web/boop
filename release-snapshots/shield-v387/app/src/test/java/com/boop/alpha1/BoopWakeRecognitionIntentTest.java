package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import android.media.AudioFormat;

import org.junit.Test;

public final class BoopWakeRecognitionIntentTest {
    @Test
    public void wakePcmMetadataIsFixed() {
        assertEquals(16_000, BoopWakeRecognitionIntent.SAMPLE_RATE_HZ);
        assertEquals(1, BoopWakeRecognitionIntent.CHANNEL_COUNT);
        assertEquals(AudioFormat.ENCODING_PCM_16BIT, BoopWakeRecognitionIntent.ENCODING);
    }
}
