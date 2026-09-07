package com.boop.alpha1;

public final class BoopSleepCharmHarness {
    private static void assertNear(float expected, float actual, float tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        float blinkEnd = BoopSleepCharm.BLINK_DURATION_MS / (float) BoopSleepCharm.TOTAL_DURATION_MS;
        float blinkMid = (BoopSleepCharm.BLINK_DURATION_MS * 0.4f) / BoopSleepCharm.TOTAL_DURATION_MS;

        assertNear(
                BoopIdleBlink.openness(0.4f),
                BoopSleepCharm.openness(blinkMid),
                0.02f,
                "sleep lead-in must preserve accepted blink geometry");
        assertNear(1f, BoopSleepCharm.openness(blinkEnd), 0.02f,
                "lead-in blink must reopen fully before the drowsy close");

        float droopPoint = (BoopSleepCharm.BLINK_DURATION_MS + BoopSleepCharm.OPEN_SETTLE_MS
                + BoopSleepCharm.DROWSY_DROOP_MS) / (float) BoopSleepCharm.TOTAL_DURATION_MS;
        assertNear(0.52f, BoopSleepCharm.openness(droopPoint), 0.03f,
                "sleep should pause half-lidded");

        assertTrue(BoopSleepCharm.openness(0.95f) < 0.30f,
                "final close should be visibly near shut");
        assertNear(0.04f, BoopSleepCharm.openness(1f), 0.01f,
                "sleep should finish on a soft eyelid line");
        assertNear(1f, BoopSleepCharm.alpha(0.90f), 0.001f,
                "eyes should remain visible through almost all of the close");
        assertNear(0f, BoopSleepCharm.alpha(1f), 0.001f,
                "sleep should end fully black");
        assertTrue(BoopSleepCharm.TOTAL_DURATION_MS > 1000L,
                "sleep close should feel deliberately slow");
    }
}
