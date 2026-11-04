package com.boop.shieldhome;

public final class NowPlayingPuppetBlinkExactTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void near(float actual, float expected, String message) {
        if (Math.abs(actual - expected) > 0.0005f) {
            throw new AssertionError(message + ": " + actual);
        }
    }

    public static void main(String[] args) {
        check(NowPlayingPuppetBlink.DURATION_MS == 183L, "183ms total");
        check(NowPlayingPuppetBlink.DOUBLE_GAP_MS == 110L, "110ms gap");
        near(NowPlayingPuppetBlink.openness(73.2f / 183f), 0.05f, "closed at 73.2ms");
        near(NowPlayingPuppetBlink.openness(77.2f / 183f), 0.05f, "8ms closed hold");
        near(NowPlayingPuppetBlink.openness(81.2f / 183f), 0.05f, "hold ends at 81.2ms");
        near(NowPlayingPuppetBlink.openness(1f), 1f, "fully reopened");
        System.out.println("NowPlayingPuppetBlinkExactTest PASS");
    }
}
