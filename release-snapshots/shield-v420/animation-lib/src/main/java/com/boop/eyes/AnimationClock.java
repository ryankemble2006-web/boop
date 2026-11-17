package com.boop.eyes;

/** Monotonic BOOP-owned milliseconds. Rate changes preserve phase and fractional time. */
public final class AnimationClock {
    private long lastRealMs, logicalMs;
    private double fraction, speed = 1.0;
    public AnimationClock(long realNowMs) { lastRealMs = logicalMs = realNowMs; }
    public static double normaliseSpeed(double value) {
        return value == .5 || value == 1 || value == 1.5 || value == 2 ? value : 1;
    }
    public long now(long realNowMs) {
        if (realNowMs <= lastRealMs) return logicalMs;
        long delta = realNowMs - lastRealMs;
        lastRealMs = realNowMs;
        double elapsed = delta * speed + fraction;
        long whole = (long) elapsed;
        logicalMs += whole;
        fraction = elapsed - whole;
        return logicalMs;
    }
    public void setSpeed(double value, long realNowMs) {
        now(realNowMs);
        speed = normaliseSpeed(value);
    }
    public double speed() { return speed; }
    public long realDelayUntil(long logicalDeadlineMs, long realNowMs) {
        long current = now(realNowMs);
        double remaining = logicalDeadlineMs - current - fraction;
        return remaining <= 0 ? 0 : (long) Math.ceil(remaining / speed);
    }
}
