package com.boop.alpha1;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class BoopWakeCommandAudioPolicyTest {
    @Test public void keepsOnlyFinalHundredMillisecondsForImmediateCommandBridge() {
        short[] wakeHistory = new short[2_000];
        for (int i = 0; i < wakeHistory.length; i++) wakeHistory[i] = (short) i;

        short[] prelude = BoopWakeCommandAudioPolicy.recognizerPrelude(wakeHistory);

        assertEquals(1_600, prelude.length);
        assertEquals(wakeHistory[400], prelude[0]);
        assertEquals(wakeHistory[1_999], prelude[1_599]);
    }

    @Test public void shortHistoryIsPreservedWithoutPadding() {
        short[] wakeHistory = new short[]{10, 20, 30, 40};

        assertArrayEquals(
                wakeHistory,
                BoopWakeCommandAudioPolicy.recognizerPrelude(wakeHistory));
    }

    @Test public void emptyOrMissingHistoryProducesNoPrelude() {
        assertArrayEquals(new short[0], BoopWakeCommandAudioPolicy.recognizerPrelude(new short[0]));
        assertArrayEquals(new short[0], BoopWakeCommandAudioPolicy.recognizerPrelude(null));
    }
}
