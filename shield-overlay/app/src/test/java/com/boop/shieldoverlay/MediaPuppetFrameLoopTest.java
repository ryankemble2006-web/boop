package com.boop.shieldoverlay;

import static org.junit.Assert.*;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class MediaPuppetFrameLoopTest {
    private final Queue scheduler = new Queue();
    private final List<Long> drawings = new ArrayList<>();
    private long now;
    private final MediaPuppetFrameLoop loop = new MediaPuppetFrameLoop(
            scheduler, () -> now, drawings::add);

    @Test public void duplicatePlayingKeepsOneFrameAndPauseRejectsCancelledCallback() {
        loop.update(HEADPHONES_PLAYING, true, true);
        assertEquals(1, scheduler.pending.size());
        assertEquals(1, drawings.size());
        loop.update(HEADPHONES_PLAYING, true, true);
        assertEquals(1, scheduler.pending.size());
        assertEquals(1, drawings.size());
        now = 100;
        scheduler.frame();
        assertEquals(Long.valueOf(120), drawings.get(1));
        assertEquals(1, scheduler.pending.size());
        now = 200;
        loop.update(HEADPHONES_REST, true, true);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(240), drawings.get(2));
        int atPause = drawings.size();
        scheduler.cancelled.get(0).run();
        assertEquals(atPause, drawings.size());
        assertEquals(0, scheduler.pending.size());
        loop.update(HEADPHONES_REST, true, true);
        assertEquals(atPause, drawings.size());
    }

    @Test public void hiddenHomeAndOffDisplayHoldPhaseWithoutFrames() {
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 250;
        loop.update(HEADPHONES_PLAYING, false, true);
        assertTrue(scheduler.pending.isEmpty());
        int hidden = drawings.size();
        now = 20250;
        scheduler.cancelled.get(0).run();
        assertEquals(hidden, drawings.size());
        loop.update(HEADPHONES_PLAYING, true, true);
        assertEquals(Long.valueOf(300), last());
        now = 20350;
        scheduler.frame();
        assertEquals(Long.valueOf(420), last());
    }

    @Test public void pauseAndAnimationDisableExcludeElapsedTimeAndDoNotRepeat() {
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 100;
        loop.update(HEADPHONES_REST, true, true);
        now = 5100;
        loop.update(HEADPHONES_PLAYING, true, false);
        assertEquals(Long.valueOf(120), last());
        assertTrue(scheduler.pending.isEmpty());
        int count = drawings.size();
        loop.update(HEADPHONES_PLAYING, true, false);
        assertEquals(count, drawings.size());
        now = 10100;
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 10200;
        scheduler.frame();
        assertEquals(Long.valueOf(240), last());
    }

    @Test public void detachAndReattachCannotReviveStaleCallback() {
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 100;
        loop.detach();
        assertTrue(scheduler.pending.isEmpty());
        int count = drawings.size();
        now = 10000;
        loop.update(HEADPHONES_PLAYING, true, true);
        assertEquals(Long.valueOf(120), last());
        scheduler.cancelled.get(0).run();
        assertEquals(count + 1, drawings.size());
        assertEquals(1, scheduler.pending.size());
    }

    @Test public void eyesAndFreshSessionResetToNeutralWhileTrackDuplicatesContinue() {
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 200;
        scheduler.frame();
        loop.reset();
        assertEquals(Long.valueOf(0), last());
        assertEquals(1, scheduler.pending.size());
        now = 300;
        scheduler.frame();
        assertEquals(Long.valueOf(120), last());
        loop.update(EYES, true, true);
        assertTrue(scheduler.pending.isEmpty());
        now = 9000;
        loop.update(HEADPHONES_REST, true, true);
        assertEquals(Long.valueOf(0), last());
        assertTrue(scheduler.pending.isEmpty());
    }

    @Test public void authoritativeZeroCancelsFramesDespitePreviouslyEnabledCache() {
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 100;
        // The process still holds enabled=true when the setting notification arrives.
        loop.update(HEADPHONES_PLAYING, true, true, () -> 0.0, () -> false);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(120), last());
        int count = drawings.size();
        now = 5100;
        scheduler.cancelled.get(0).run();
        loop.update(HEADPHONES_PLAYING, true, true, () -> 0.0, () -> false);
        assertEquals(count, drawings.size());
        assertEquals(0, scheduler.pending.size());
    }

    @Test public void authoritativePositiveResumesFramesDespitePreviouslyDisabledCache() {
        loop.update(HEADPHONES_PLAYING, true, true);
        now = 100;
        loop.update(HEADPHONES_PLAYING, true, false);
        now = 5100;
        // The process still holds enabled=false after the setting has been restored.
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> false);
        assertEquals(1, scheduler.pending.size());
        assertEquals(Long.valueOf(120), last());
        int count = drawings.size();
        scheduler.cancelled.get(0).run();
        assertEquals(count, drawings.size());
        now = 5200;
        scheduler.frame();
        assertEquals(Long.valueOf(240), last());
        assertEquals(1, scheduler.pending.size());
    }

    @Test public void deniedAuthoritativeReadStopsFramesWithoutTrustingOldEnabledValue() {
        loop.update(HEADPHONES_PLAYING, true, true);
        loop.update(HEADPHONES_PLAYING, true, true,
                () -> { throw new SecurityException(); }, () -> false);
        assertEquals(0, scheduler.pending.size());
    }

    @Test public void powerSaverOnInitialAcquisitionKeepsPositiveScaleStatic() {
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> true);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(0), last());
        int count = drawings.size();
        now = 5000;
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> true);
        assertEquals(count, drawings.size());
        assertEquals(0, scheduler.pending.size());
    }

    @Test public void powerSaverTransitionCancelsAndExitResumesHeldPhase() {
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> false);
        now = 100;
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> true);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(120), last());
        int count = drawings.size();
        now = 5100;
        scheduler.cancelled.get(0).run();
        assertEquals(count, drawings.size());
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> false);
        assertEquals(1, scheduler.pending.size());
        assertEquals(Long.valueOf(120), last());
        now = 5200;
        scheduler.frame();
        assertEquals(Long.valueOf(240), last());
    }

    @Test public void zeroDurationStillHoldsWhenPowerSaverEnds() {
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> true);
        now = 100;
        loop.update(HEADPHONES_PLAYING, true, true, () -> 0.0, () -> false);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(0), last());
        now = 5000;
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> false);
        assertEquals(1, scheduler.pending.size());
        assertEquals(Long.valueOf(0), last());
    }

    @Test public void deniedPowerReadStopsAlreadyRunningFrames() {
        loop.update(HEADPHONES_PLAYING, true, true);
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0,
                () -> { throw new SecurityException(); });
        assertEquals(0, scheduler.pending.size());
    }

    @Test public void unavailableAnimatorObservationKeepsReadablePositiveScaleStatic() {
        loop.update(HEADPHONES_PLAYING, true, false, () -> 1.0, () -> false);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(0), last());
        int count = drawings.size();
        now = 5000;
        loop.update(HEADPHONES_PLAYING, true, false, () -> 1.0, () -> false);
        assertEquals(count, drawings.size());
        assertEquals(0, scheduler.pending.size());
    }

    @Test public void losingAnimatorObservationCancelsAndRecoveryResumesHeldPhase() {
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> false);
        now = 100;
        loop.update(HEADPHONES_PLAYING, true, false, () -> 1.0, () -> false);
        assertEquals(0, scheduler.pending.size());
        assertEquals(Long.valueOf(120), last());
        int count = drawings.size();
        now = 5100;
        scheduler.cancelled.get(0).run();
        assertEquals(count, drawings.size());
        assertEquals(0, scheduler.pending.size());
        loop.update(HEADPHONES_PLAYING, true, true, () -> 1.0, () -> false);
        assertEquals(1, scheduler.pending.size());
        assertEquals(Long.valueOf(120), last());
        now = 5200;
        scheduler.frame();
        assertEquals(Long.valueOf(240), last());
    }

    private Long last() { return drawings.get(drawings.size() - 1); }
    private static final class Queue implements MediaPuppetFrameLoop.Scheduler {
        final List<Runnable> pending = new ArrayList<>();
        final List<Runnable> cancelled = new ArrayList<>();
        public void post(Runnable frame) { pending.add(frame); }
        public void cancel(Runnable frame) { pending.remove(frame); cancelled.add(frame); }
        void frame() { pending.remove(0).run(); }
    }
}
