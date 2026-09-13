package com.boop.eyes;

/** Host-side numerical coverage only; this is not an Android lifecycle or visual test. */
public final class BoopAnimationSpeedEdgeHarness {
    private static final double[] RATES = {.5, 1, 1.5, 2};
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static void same(float expected, float actual, String context) {
        check(Float.floatToIntBits(expected) == Float.floatToIntBits(actual), context);
    }
    private static void same(EyeMotion.Pose expected, EyeMotion.Pose actual, String context) {
        same(expected.left, actual.left, context + " left lid");
        same(expected.right, actual.right, context + " right lid");
        same(expected.x, actual.x, context + " gaze x");
        same(expected.y, actual.y, context + " gaze y");
    }
    private static void same(V156SignMotion.Pose expected, SignMotion.Pose actual, String context) {
        same(expected.lift, actual.lift, context + " lift");
        same(expected.angle, actual.angle, context + " angle");
        same(expected.sway, actual.sway, context + " sway");
        same(expected.bob, actual.bob, context + " bob");
        same(expected.wrist, actual.wrist, context + " wrist");
        same(expected.eyes, actual.eyes, context + " eyes");
    }
    private static void irregularClockAndSigns() {
        long base = 9_000_000_001L, real = base, halfTicks = 0L;
        AnimationClock clock = new AnimationClock(base);
        // Independent integer half-millisecond oracle, not another AnimationClock.
        for (int i = 0; i < 10000; i++) {
            int units = 1 + (i * 7) % 4;
            clock.setSpeed(units / 2.0, real);
            check(clock.now(real) == base + halfTicks / 2, "Rate change moved phase");
            int delta = 1 + (i * 17) % 47;
            real += delta;
            halfTicks += (long) delta * units;
            long logical = clock.now(real);
            check(logical == base + halfTicks / 2, "Irregular-frame fractional drift");
            check(clock.now(real - 1) == logical, "Backward sample changed time");
            long deadline = logical + 1 + i % 311;
            long remainingHalf = 2 * (deadline - base) - halfTicks;
            long expectedDelay = (remainingHalf + units - 1) / units;
            check(clock.realDelayUntil(deadline, real) == expectedDelay, "Fractional sleep deadline rounded incorrectly");
            for (int style = 0; style < 4; style++) {
                same(V156SignMotion.sample(halfTicks / 2, style), SignMotion.sample(logical - base, style),
                        "Changing rate notification style " + style);
            }
        }
    }
    private static void fixedRateSigns() {
        long base = 10001;
        for (double rate : RATES) {
            AnimationClock clock = new AnimationClock(base);
            clock.setSpeed(rate, base);
            for (int elapsed = 0; elapsed <= 18000; elapsed += 11) {
                long actualElapsed = clock.now(base + elapsed) - base;
                long expectedElapsed = (long) (elapsed * rate);
                for (int style = 0; style < 4; style++) {
                    same(V156SignMotion.sample(expectedElapsed, style), SignMotion.sample(actualElapsed, style),
                            "Fixed rate notification " + rate + " / " + style);
                }
            }
        }
    }
    private static void oneShotBoundaries() {
        long base = 70001;
        for (double rate : RATES) {
            for (EyeMotion.Clip clip : EyeCatalogue.ALL) {
                if (clip.loop) continue;
                long end = (long) Math.ceil(clip.duration / rate);
                for (long elapsed : new long[]{Math.max(0, end - 1), end, end + 1}) {
                    ProductionAnimationController actual = new ProductionAnimationController("idle", base, 71L);
                    V156ProductionAnimationController expected = new V156ProductionAnimationController("idle", base, 71L);
                    actual.setSpeed(rate, base);
                    actual.trigger(clip.id, base, 120f);
                    expected.trigger(clip.id, base, 120f);
                    same(expected.sample(base + (long) (elapsed * rate)), actual.sample(base + elapsed),
                            "One-shot boundary " + clip.id + " / " + rate + " / " + elapsed);
                    check(expected.activeClipId().equals(actual.activeClipId()), "One-shot returned at wrong boundary");
                    check((elapsed < end ? clip.id : "idle").equals(actual.activeClipId()), "One-shot did not complete at scaled duration");
                }
            }
        }
    }
    private static void pauseResumeWithChangingRate() {
        long base = 43001;
        for (EyeMotion.Clip clip : EyeCatalogue.ALL) {
            // Compatibility with accepted v156, not a new definition of its lifecycle policy.
            ProductionAnimationController actual = new ProductionAnimationController(clip.id, base, 19L);
            V156ProductionAnimationController expected = new V156ProductionAnimationController(clip.id, base, 19L);
            long logicalHalf = 0L;
            int units = 2;
            for (int elapsed = 0; elapsed <= 5000; elapsed++) {
                if (elapsed > 0) logicalHalf += units;
                long logical = base + logicalHalf / 2;
                if (elapsed == 400) { actual.pause(base + elapsed); expected.pause(logical); }
                if (elapsed == 701 || elapsed == 1702 || elapsed == 3003) {
                    units = elapsed == 701 ? 1 : elapsed == 1702 ? 4 : 3;
                    actual.setSpeed(units / 2.0, base + elapsed);
                }
                if (elapsed == 2200) { actual.resume(base + elapsed); expected.resume(logical); }
                same(expected.sample(logical), actual.sample(base + elapsed), "Pause/rate/resume " + clip.id);
            }
        }
    }
    public static void main(String[] args) {
        irregularClockAndSigns();
        fixedRateSigns();
        oneShotBoundaries();
        pauseResumeWithChangingRate();
        System.out.println("BoopAnimationSpeedEdgeHarness: " + checks
                + " exact checks; integer clock oracle, four sign styles, one-shot boundaries, pause/rate/resume");
    }
}
