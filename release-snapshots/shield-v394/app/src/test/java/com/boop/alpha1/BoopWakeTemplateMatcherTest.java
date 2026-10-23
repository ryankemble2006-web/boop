package com.boop.alpha1;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public final class BoopWakeTemplateMatcherTest {
    private static final int SAMPLE_RATE = 16_000;
    private static final int CHUNK = 1_600;

    @Test public void trainedNameMatchesBeforeImmediateCommandFinishes() {
        short[] wake = syntheticWake(8_000);
        BoopWakePronunciationFeatures.Result learned =
                BoopWakePronunciationFeatures.extract(wake, SAMPLE_RATE);
        assertNotNull(learned);
        BoopWakeAcousticProfile profile = BoopWakeAcousticProfile.train(
                "Steve",
                Arrays.asList(
                        learned.vector(), learned.vector(), learned.vector(),
                        learned.vector(), learned.vector()),
                learned.durationSeconds());
        BoopWakeTemplateMatcher matcher = new BoopWakeTemplateMatcher(profile, SAMPLE_RATE);

        short[] command = tone(9_600, 7_000, 1_420.0);
        short[] phrase = concat(wake, command);
        int detectedAt = -1;
        for (int offset = 0; offset < phrase.length; offset += CHUNK) {
            int count = Math.min(CHUNK, phrase.length - offset);
            short[] chunk = Arrays.copyOfRange(phrase, offset, offset + count);
            if (matcher.accept(chunk, chunk.length)) {
                detectedAt = offset + count;
                break;
            }
        }

        assertTrue("custom wake must not require a silence after the name", detectedAt > 0);
        assertTrue("wake should fire before more than one command chunk is lost",
                detectedAt <= wake.length + CHUNK);
    }

    @Test public void unrelatedContinuousSpeechDoesNotMatchProfile() {
        short[] wake = syntheticWake(8_000);
        BoopWakePronunciationFeatures.Result learned =
                BoopWakePronunciationFeatures.extract(wake, SAMPLE_RATE);
        assertNotNull(learned);
        BoopWakeAcousticProfile profile = BoopWakeAcousticProfile.train(
                "Steve",
                Arrays.asList(
                        learned.vector(), learned.vector(), learned.vector(),
                        learned.vector(), learned.vector()),
                learned.durationSeconds());
        BoopWakeTemplateMatcher matcher = new BoopWakeTemplateMatcher(profile, SAMPLE_RATE);

        short[] other = concat(
                tone(6_400, 7_000, 1_700.0),
                tone(6_400, 7_000, 2_350.0));
        for (int offset = 0; offset < other.length; offset += CHUNK) {
            int count = Math.min(CHUNK, other.length - offset);
            short[] chunk = Arrays.copyOfRange(other, offset, offset + count);
            assertFalse(matcher.accept(chunk, chunk.length));
        }
    }

    private static short[] syntheticWake(int amplitude) {
        return concat(
                tone(3_200, amplitude, 510.0),
                tone(3_200, amplitude, 910.0));
    }

    private static short[] concat(short[]... parts) {
        int total = 0;
        for (short[] part : parts) total += part.length;
        short[] result = new short[total];
        int at = 0;
        for (short[] part : parts) {
            System.arraycopy(part, 0, result, at, part.length);
            at += part.length;
        }
        return result;
    }

    private static short[] tone(int samples, int amplitude, double hz) {
        short[] result = new short[samples];
        for (int i = 0; i < samples; i++) {
            double envelope = Math.sin(Math.PI * i / Math.max(1, samples - 1));
            result[i] = (short) Math.round(
                    amplitude * envelope * Math.sin(2.0 * Math.PI * hz * i / SAMPLE_RATE));
        }
        return result;
    }
}
