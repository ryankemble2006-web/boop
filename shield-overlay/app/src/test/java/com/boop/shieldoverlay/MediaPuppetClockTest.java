package com.boop.shieldoverlay;

import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.EYES;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_REST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MediaPuppetClockTest {
    @Test
    public void pausedTimeAndDuplicatePlayingDoNotRestartPhase() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, 1000L);
        assertEquals(1200L, clock.sampleTimeMs(2000L));
        clock.update(HEADPHONES_REST, true, true, 2000L);
        assertEquals(1200L, clock.sampleTimeMs(9000L));
        clock.update(HEADPHONES_PLAYING, true, true, 9000L);
        clock.update(HEADPHONES_PLAYING, true, true, 9500L);
        assertEquals(2400L, clock.sampleTimeMs(10000L));
    }

    @Test
    public void threeSecondsOfRealTimeWrapsOnePreviewCycle() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, 0L);

        assertEquals(3598L, clock.sampleTimeMs(2999L));
        assertEquals(0L, clock.sampleTimeMs(3000L));
    }

    @Test
    public void manyOneMillisecondSamplesPreserveFractionalProgress() {
        MediaPuppetClock frequent = new MediaPuppetClock();
        MediaPuppetClock single = new MediaPuppetClock();
        frequent.update(HEADPHONES_PLAYING, true, true, 0L);
        single.update(HEADPHONES_PLAYING, true, true, 0L);

        for (long nowMs = 1L; nowMs <= 1001L; nowMs++) {
            frequent.sampleTimeMs(nowMs);
        }

        assertEquals(1201L, frequent.sampleTimeMs(1001L));
        assertEquals(1201L, single.sampleTimeMs(1001L));
    }

    @Test
    public void fiftyNineMillisecondSkipHoldsAndResumesFractionalPhase() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, 0L);

        assertEquals(70L, clock.sampleTimeMs(59L));
        clock.update(HEADPHONES_REST, true, true, 59L);
        assertEquals(70L, clock.sampleTimeMs(5059L));
        clock.update(HEADPHONES_PLAYING, true, true, 5059L);
        assertEquals(72L, clock.sampleTimeMs(5060L));
    }

    @Test
    public void hiddenTimeDoesNotAdvanceThePhase() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, 0L);
        assertEquals(1200L, clock.sampleTimeMs(1000L));

        clock.update(HEADPHONES_PLAYING, false, true, 1000L);
        assertFalse(clock.isRunning());
        assertEquals(1200L, clock.sampleTimeMs(9000L));

        clock.update(HEADPHONES_PLAYING, true, true, 9000L);
        assertTrue(clock.isRunning());
        assertEquals(2400L, clock.sampleTimeMs(10000L));
    }

    @Test
    public void disabledSystemAnimationsFreezeUntilReenabled() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, false, 0L);
        assertFalse(clock.isRunning());
        assertEquals(0L, clock.sampleTimeMs(4000L));

        clock.update(HEADPHONES_PLAYING, true, true, 4000L);
        assertTrue(clock.isRunning());
        assertEquals(1200L, clock.sampleTimeMs(5000L));
    }

    @Test
    public void eyesModeResetsPhaseBeforePlaybackCanResume() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, 0L);
        assertEquals(1200L, clock.sampleTimeMs(1000L));

        clock.update(EYES, true, true, 1000L);
        assertFalse(clock.isRunning());
        assertEquals(0L, clock.sampleTimeMs(9000L));

        clock.update(HEADPHONES_PLAYING, true, true, 9000L);
        assertEquals(1200L, clock.sampleTimeMs(10000L));
    }

    @Test
    public void backwardAndNegativeTimesDoNotDoubleCountElapsedTime() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, 1000L);

        assertEquals(0L, clock.sampleTimeMs(-1000L));
        assertEquals(1200L, clock.sampleTimeMs(2000L));
    }

    @Test
    public void extremeForwardIntervalIsReducedBeforeScaling() {
        MediaPuppetClock clock = new MediaPuppetClock();
        clock.update(HEADPHONES_PLAYING, true, true, Long.MIN_VALUE);

        assertEquals(738L, clock.sampleTimeMs(Long.MAX_VALUE));
        assertTrue(clock.sampleTimeMs(Long.MAX_VALUE) >= 0L);
        assertTrue(clock.sampleTimeMs(Long.MAX_VALUE) <= 3599L);
    }
}
