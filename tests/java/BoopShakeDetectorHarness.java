package com.boop.alpha1;

public final class BoopShakeDetectorHarness {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        BoopShakeDetector detector = new BoopShakeDetector();

        detector.onAccelerometer(0f, 0f, 9.80665f, 0L);
        check(!detector.onAccelerometer(2.0f, 0f, 9.80665f, 120L), "gentle pickup must not wake");
        check(!detector.onAccelerometer(-2.0f, 0f, 9.80665f, 240L), "gentle reversal must not wake");
        check(!detector.onAccelerometer(2.5f, 0f, 9.80665f, 360L), "gentle movement must stay ignored");

        detector.reset();
        detector.onAccelerometer(0f, 0f, 9.80665f, 0L);
        check(!detector.onAccelerometer(30f, 0f, 9.80665f, 100L), "one jolt is not a shake");
        check(!detector.onAccelerometer(-30f, 0f, 9.80665f, 220L), "two jolts are not enough");
        check(detector.onAccelerometer(30f, 0f, 9.80665f, 340L), "deliberate back-and-forth shake must wake");
        check(detector.lastTriggerStrength() > 0.35f, "strong shake should carry useful animation strength");

        check(!detector.onAccelerometer(-30f, 0f, 9.80665f, 500L), "cooldown must block retrigger one");
        check(!detector.onAccelerometer(30f, 0f, 9.80665f, 620L), "cooldown must block retrigger two");
        check(!detector.onAccelerometer(-30f, 0f, 9.80665f, 740L), "cooldown must block retrigger three");

        detector.onAccelerometer(0f, 0f, 9.80665f, 2200L);
        check(!detector.onAccelerometer(30f, 0f, 9.80665f, 2320L), "new shake peak one");
        check(!detector.onAccelerometer(-30f, 0f, 9.80665f, 2440L), "new shake peak two");
        check(detector.onAccelerometer(30f, 0f, 9.80665f, 2560L), "shake should work again after cooldown");

        System.out.println("BOOP shake detector harness passed");
    }
}
