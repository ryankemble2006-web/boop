package com.boop.alpha1;

import java.util.Random;

public final class BoopIdleBlinkExactTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void close(float actual, float expected, String message) {
        if (Math.abs(actual - expected) > 0.01f) {
            throw new AssertionError(message + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        check(BoopIdleBlink.DURATION_MS == 183L, "183ms blink");
        check(BoopIdleBlink.DOUBLE_GAP_MS == 110L, "110ms double gap");
        close(BoopIdleBlink.openness(73.2f / 183f), 0f, "fully closed after close phase");
        close(BoopIdleBlink.openness(81.2f / 183f), 0f, "8ms closed hold");
        close(BoopIdleBlink.openness(1f), 1f, "fully reopened");
        check(BoopIdleBlink.shouldDoubleBlink(new FixedRandom(17)), "17 triggers double");
        check(!BoopIdleBlink.shouldDoubleBlink(new FixedRandom(18)), "18 does not trigger double");
        System.out.println("BoopIdleBlinkExactTest PASS");
    }

    private static final class FixedRandom extends Random {
        private final int value;
        FixedRandom(int value) { this.value = value; }
        @Override public int nextInt(int bound) { return Math.floorMod(value, bound); }
    }
}
