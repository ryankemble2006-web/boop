package com.boop.alpha1;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public final class BoopWakeEnrollmentProfileTest {
    @Test public void trainingRequiresFiveUtterancesAndStoresNoRawPcm() {
        assertEquals(5, BoopWakeAcousticProfile.REQUIRED_UTTERANCES);
        List<float[]> four = Arrays.asList(
                vector(1.00f, .10f, .20f, .30f),
                vector(.98f, .11f, .19f, .31f),
                vector(1.02f, .09f, .21f, .29f),
                vector(1.01f, .10f, .20f, .30f));
        try {
            BoopWakeAcousticProfile.train("Steve", four, 0.62f);
            fail("four samples must not produce a trained wake profile");
        } catch (IllegalArgumentException expected) {
            // Five deliberate utterances are the enrolment contract.
        }
    }

    @Test public void trainedProfileMatchesSamePronunciationAndRejectsDifferentShape() {
        List<float[]> examples = Arrays.asList(
                vector(1.00f, .10f, .20f, .30f),
                vector(.98f, .11f, .19f, .31f),
                vector(1.02f, .09f, .21f, .29f),
                vector(1.01f, .10f, .20f, .30f),
                vector(.99f, .12f, .18f, .30f));
        BoopWakeAcousticProfile profile = BoopWakeAcousticProfile.train("Steve", examples, 0.62f);

        assertEquals("Steve", profile.name());
        assertTrue(profile.matches(vector(1.01f, .105f, .195f, .30f), 0.60f));
        assertFalse(profile.matches(vector(.05f, .90f, .80f, .10f), 0.60f));
        assertFalse("wildly different duration should not match",
                profile.matches(vector(1.01f, .105f, .195f, .30f), 1.55f));
    }

    @Test public void compactCodecRoundTripsProfileWithoutAudio() {
        BoopWakeAcousticProfile original = BoopWakeAcousticProfile.train("Steve", Arrays.asList(
                vector(1.00f, .10f, .20f, .30f),
                vector(.98f, .11f, .19f, .31f),
                vector(1.02f, .09f, .21f, .29f),
                vector(1.01f, .10f, .20f, .30f),
                vector(.99f, .12f, .18f, .30f)), 0.62f);

        String encoded = original.encode();
        assertFalse(encoded.contains("PCM"));
        BoopWakeAcousticProfile decoded = BoopWakeAcousticProfile.decode(encoded);
        assertNotNull(decoded);
        assertEquals(original.name(), decoded.name());
        assertTrue(decoded.matches(vector(1.0f, .10f, .20f, .30f), 0.62f));
    }

    @Test public void pronunciationFeaturesIgnoreSimpleVolumeChangeAndLeadingSilence() {
        short[] loud = syntheticWord(0, 11000);
        short[] quietWithLead = syntheticWord(3200, 4800);
        BoopWakePronunciationFeatures.Result a = BoopWakePronunciationFeatures.extract(loud, 16000);
        BoopWakePronunciationFeatures.Result b = BoopWakePronunciationFeatures.extract(quietWithLead, 16000);
        assertNotNull(a);
        assertNotNull(b);
        assertTrue("same synthetic word should remain similar after volume/silence change",
                BoopWakeAcousticProfile.cosine(a.vector(), b.vector()) > 0.90f);
    }

    @Test public void segmenterEmitsOneUtteranceAfterTrailingSilence() {
        BoopWakeUtteranceSegmenter segmenter = new BoopWakeUtteranceSegmenter(16000);
        short[] silence = new short[1600];
        short[] speech = tone(1600, 7000, 510.0);
        assertNull(segmenter.accept(silence, silence.length));
        assertNull(segmenter.accept(speech, speech.length));
        assertNull(segmenter.accept(speech, speech.length));
        assertNull(segmenter.accept(speech, speech.length));
        assertNull(segmenter.accept(silence, silence.length));
        short[] utterance = segmenter.accept(silence, silence.length);
        assertNotNull(utterance);
        assertTrue(utterance.length >= 3200);
    }

    private static float[] vector(float... values) { return values; }

    private static short[] syntheticWord(int leadingSilence, int amplitude) {
        short[] first = tone(3200, amplitude, 510.0);
        short[] second = tone(3200, amplitude, 910.0);
        short[] result = new short[leadingSilence + first.length + second.length + 1600];
        System.arraycopy(first, 0, result, leadingSilence, first.length);
        System.arraycopy(second, 0, result, leadingSilence + first.length, second.length);
        return result;
    }

    private static short[] tone(int samples, int amplitude, double hz) {
        short[] result = new short[samples];
        for (int i = 0; i < samples; i++) {
            double envelope = Math.sin(Math.PI * i / Math.max(1, samples - 1));
            result[i] = (short) Math.round(amplitude * envelope * Math.sin(2.0 * Math.PI * hz * i / 16000.0));
        }
        return result;
    }
}
