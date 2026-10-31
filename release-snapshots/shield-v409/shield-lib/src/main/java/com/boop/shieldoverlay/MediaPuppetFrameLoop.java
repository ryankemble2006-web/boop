package com.boop.shieldoverlay;

import java.util.function.LongSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.BooleanSupplier;

public final class MediaPuppetFrameLoop {
    public interface Scheduler { void post(Runnable frame); void cancel(Runnable frame); }
    public interface Drawing { void draw(long sampleTimeMs); }
    private final Scheduler scheduler;
    private final LongSupplier now;
    private final Drawing drawing;
    private final MediaPuppetClock clock = new MediaPuppetClock();
    private DeezerPuppetPolicy.Mode mode = DeezerPuppetPolicy.Mode.EYES;
    private boolean visible;
    private boolean animationsEnabled;
    private long generation;
    private Runnable pending;

    public MediaPuppetFrameLoop(Scheduler scheduler, LongSupplier now, Drawing drawing) {
        this.scheduler = scheduler;
        this.now = now;
        this.drawing = drawing;
    }

    public void update(DeezerPuppetPolicy.Mode mode, boolean visible, boolean animationsEnabled) {
        boolean changed = this.mode != mode || this.visible != visible
                || this.animationsEnabled != animationsEnabled;
        long time = now.getAsLong();
        // Accrue the old state before deciding whether the new state can run.
        clock.update(mode, visible, animationsEnabled, time);
        this.mode = mode;
        this.visible = visible;
        this.animationsEnabled = animationsEnabled;
        if (!clock.isRunning()) {
            cancel();
        }
        if (changed && visible) {
            drawing.draw(clock.sampleTimeMs(time));
        }
        schedule();
    }

    /** Acquire public settings at lifecycle/observer boundaries, never on repeating frames. */
    public void update(DeezerPuppetPolicy.Mode mode, boolean visible,
            boolean animationObservationAvailable,
            DoubleSupplier durationScale, BooleanSupplier powerSaveMode) {
        boolean enabled;
        try {
            enabled = animationObservationAvailable
                    && durationScale.getAsDouble() > 0.0 && !powerSaveMode.getAsBoolean();
        } catch (SecurityException denied) {
            // Unreadable scale/power state must not leave a previously running loop active.
            enabled = false;
        }
        update(mode, visible, enabled);
    }

    public void reset() {
        cancel();
        long time = now.getAsLong();
        clock.reset(time);
        clock.update(mode, visible, animationsEnabled, time);
        if (visible) {
            drawing.draw(0L);
        }
        schedule();
    }

    public void detach() {
        update(mode, false, animationsEnabled);
    }

    private void cancel() {
        generation++;
        if (pending != null) {
            scheduler.cancel(pending);
            pending = null;
        }
    }

    private void schedule() {
        if (!clock.isRunning() || pending != null) {
            return;
        }
        long postedGeneration = ++generation;
        pending = () -> {
            if (postedGeneration != generation || !clock.isRunning()) {
                return;
            }
            pending = null;
            drawing.draw(clock.sampleTimeMs(now.getAsLong()));
            schedule();
        };
        scheduler.post(pending);
    }
}
