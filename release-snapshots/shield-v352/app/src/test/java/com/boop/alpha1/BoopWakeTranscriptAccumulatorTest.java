package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class BoopWakeTranscriptAccumulatorTest {
    @Test
    public void fallsBackToLatestNonEmptyPartialWhenFinalIsEmpty() {
        BoopWakeTranscriptAccumulator accumulator = new BoopWakeTranscriptAccumulator();
        accumulator.rememberPartial(null);
        accumulator.rememberPartial("   ");
        accumulator.rememberPartial("boop");
        accumulator.rememberPartial("boop skip");

        assertEquals("boop skip", accumulator.chooseFinal(null));
        assertEquals("boop skip", accumulator.chooseFinal("   "));
    }

    @Test
    public void realFinalTranscriptWinsOverPartial() {
        BoopWakeTranscriptAccumulator accumulator = new BoopWakeTranscriptAccumulator();
        accumulator.rememberPartial("boop skip");

        assertEquals("boop stop", accumulator.chooseFinal(" boop stop "));
    }

    @Test
    public void resetPreventsPreviousWakeFromLeakingIntoNextWake() {
        BoopWakeTranscriptAccumulator accumulator = new BoopWakeTranscriptAccumulator();
        accumulator.rememberPartial("boop skip");
        accumulator.reset();

        assertNull(accumulator.chooseFinal(null));
    }
}
