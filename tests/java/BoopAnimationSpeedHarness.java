package com.boop.eyes;

public final class BoopAnimationSpeedHarness {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static void same(EyeMotion.Pose expected, EyeMotion.Pose actual, String context) {
        check(Float.floatToIntBits(expected.left) == Float.floatToIntBits(actual.left)
                && Float.floatToIntBits(expected.right) == Float.floatToIntBits(actual.right)
                && Float.floatToIntBits(expected.x) == Float.floatToIntBits(actual.x)
                && Float.floatToIntBits(expected.y) == Float.floatToIntBits(actual.y), context);
    }
    public static void main(String[] args) {
        AnimationClock clock = new AnimationClock(1000);
        check(clock.now(1003) == 1003, "1x keeps original uptime exactly");
        clock.setSpeed(.5, 1003);
        check(clock.now(1004) == 1003, "Retain fractional half-millisecond");
        check(clock.now(1005) == 1004, "Half-speed remainder must not be lost");
        clock.setSpeed(2, 1005);
        check(clock.now(1005) == 1004, "Changing speed must not jump phase");
        check(clock.now(1006) == 1006, "New delta uses the new speed");
        check(clock.now(1000) == 1006, "Monotonic clock ignores backward input");
        check(clock.now(1007) == 1008, "Backward input must not double-count elapsed time");
        for (double bad : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -1, 0, 20, .1}) {
            clock.setSpeed(bad, 1007);
            check(clock.speed() == 1, "Invalid saved rate returns to the approved default");
        }
        AnimationClock sleep = new AnimationClock(1000);
        check(sleep.realDelayUntil(1600, 1000) == 600, "1x sleep deadline is unchanged");
        sleep.setSpeed(.5, 1000);
        check(sleep.realDelayUntil(1600, 1000) == 1200, "Slower sleep must finish before hiding");
        check(sleep.realDelayUntil(1600, 1100) == 1100, "Scaled remaining time");
        sleep.setSpeed(2, 1100);
        check(sleep.realDelayUntil(1600, 1100) == 275, "Mid-sleep speed change reschedules remaining time");
        check(sleep.realDelayUntil(1600, 1375) == 0, "Finished sleep hides without another cycle");
        AnimationClock longUptime = new AnimationClock(9_000_000_000L);
        longUptime.setSpeed(.5, 9_000_000_000L);
        for (int i = 1; i <= 10001; i++)
            check(longUptime.now(9_000_000_000L + i) == 9_000_000_000L + i / 2, "Fractional rate remains stable at long uptime");

        long base = 34567L;
        for (double rate : new double[]{.5, 1, 1.5, 2}) {
            for (EyeMotion.Clip clip : EyeCatalogue.ALL) {
                ProductionAnimationController actual = new ProductionAnimationController(clip.id, base, 98171L);
                V156ProductionAnimationController expected = new V156ProductionAnimationController(clip.id, base, 98171L);
                actual.setSpeed(rate, base);
                for (int elapsed = 0; elapsed <= 18000; elapsed += 13) {
                    same(expected.sample(base + (long)(elapsed * rate)), actual.sample(base + elapsed),
                            clip.id + " at " + rate + "x / " + elapsed);
                }
            }
            ProductionAnimationController actual = new ProductionAnimationController("idle", base, 117L);
            V156ProductionAnimationController expected = new V156ProductionAnimationController("idle", base, 117L);
            actual.setSpeed(rate, base);
            for (int elapsed = 0; elapsed <= 10000; elapsed += 13) {
                long real = base + elapsed, logical = base + (long)(elapsed * rate);
                if (elapsed == 520) { actual.trigger("wake", real, 0); expected.trigger("wake", logical, 0); }
                if (elapsed == 1300) { actual.setState("music", real, 160); expected.setState("music", logical, 160); }
                if (elapsed == 2600) { actual.pause(real); expected.pause(logical); }
                if (elapsed == 3900) { actual.resume(real); expected.resume(logical); }
                if (elapsed == 5200) { actual.trigger("track_change", real, 120); expected.trigger("track_change", logical, 120); }
                same(expected.sample(logical), actual.sample(real), "State/trigger/blend/pause at " + rate);
                check(expected.activeClipId().equals(actual.activeClipId()), "One-shot return timing must match scaled baseline");
            }
        }
        ProductionAnimationController live = new ProductionAnimationController("thinking", base, 51L);
        V156ProductionAnimationController reference = new V156ProductionAnimationController("thinking", base, 51L);
        for (int elapsed = 0; elapsed <= 5000; elapsed += 10) {
            if (elapsed == 1000) live.setSpeed(.5, base + elapsed);
            if (elapsed == 3000) live.setSpeed(2, base + elapsed);
            long logical = elapsed <= 1000 ? elapsed : elapsed <= 3000
                    ? 1000 + (elapsed - 1000) / 2 : 2000 + (elapsed - 3000) * 2;
            same(reference.sample(base + logical), live.sample(base + elapsed), "Live rate changes keep continuous phase");
        }
        System.out.println("BoopAnimationSpeedHarness: " + checks + " exact timing checks; "
                + EyeCatalogue.ALL.length + " authored clips unchanged, rates .5/1/1.5/2");
    }
}
