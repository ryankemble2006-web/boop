package com.boop.alpha1;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public final class BoopWakeCommandAudioPolicyTest {
    @Test public void excludesWakeDetectionHistoryFromCommandRecognizer() {
        short[] wakeHistory = new short[]{10, 20, 30, 40};

        assertArrayEquals(
                new short[0],
                BoopWakeCommandAudioPolicy.recognizerPrelude(wakeHistory));
    }
}
