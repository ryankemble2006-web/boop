package com.boop.shieldoverlay;

public final class MediaPuppetClock {
    private static final long SCALE_NUMERATOR = 6L;
    private static final long SCALE_DENOMINATOR = 5L;
    private static final long PHASE_UNITS_PERIOD =
            MediaPuppetMotion.MUSIC_PERIOD_MS * SCALE_DENOMINATOR;
    private static final long REAL_PERIOD_MS = PHASE_UNITS_PERIOD / SCALE_NUMERATOR;

    private long phaseUnits;
    private long lastNowMs;
    private boolean initialized;
    private boolean running;

    public void update(
            DeezerPuppetPolicy.Mode mode,
            boolean visible,
            boolean animationsEnabled,
            long nowMs) {
        accrue(nowMs);
        if (mode == DeezerPuppetPolicy.Mode.EYES) {
            reset(nowMs);
        }
        running = mode == DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING
                && visible
                && animationsEnabled;
    }

    public long sampleTimeMs(long nowMs) {
        accrue(nowMs);
        return phaseUnits / SCALE_DENOMINATOR;
    }

    public boolean isRunning() {
        return running;
    }

    public void reset(long nowMs) {
        phaseUnits = 0L;
        lastNowMs = nowMs;
        initialized = true;
        running = false;
    }

    private void accrue(long nowMs) {
        if (!initialized) {
            lastNowMs = nowMs;
            initialized = true;
            return;
        }
        if (nowMs <= lastNowMs) {
            return;
        }

        if (running) {
            long elapsedPhaseMs = Math.floorMod(
                    Math.floorMod(nowMs, REAL_PERIOD_MS)
                            - Math.floorMod(lastNowMs, REAL_PERIOD_MS),
                    REAL_PERIOD_MS);
            phaseUnits = Math.floorMod(
                    phaseUnits + elapsedPhaseMs * SCALE_NUMERATOR,
                    PHASE_UNITS_PERIOD);
        }
        lastNowMs = nowMs;
    }
}
