package com.boop.alpha1;

public final class BoopEyeHueMathHarness {
    public static void main(String[] args) {
        require(BoopEyeHueMath.PROGRESS_MAX == 359, "full hue range");
        require(BoopEyeHueMath.DEFAULT_HUE_DEGREES == 190, "default hue anchor");
        require(BoopEyeHueMath.clampHue(-5) == 0, "low clamp");
        require(BoopEyeHueMath.clampHue(999) == 359, "high clamp");
        require(BoopEyeHueMath.matrixForHue(190) == null, "default hue must be unfiltered");

        checkHue(30, -160f, "orange");
        checkHue(120, -70f, "green");
        checkHue(330, 140f, "pink");
        checkHue(270, 80f, "purple");
        checkHue(180, -10f, "cyan");

        System.out.println("BoopEyeHueMathHarness PASS");
    }

    private static void checkHue(int hue, float expectedRotation, String name) {
        float actual = BoopEyeHueMath.rotationDegreesForHue(hue);
        require(Math.abs(actual - expectedRotation) < 0.001f, name + " rotation");
        float[] matrix = BoopEyeHueMath.matrixForHue(hue);
        require(matrix != null && matrix.length == 20, name + " matrix");
        for (float value : matrix) {
            require(Float.isFinite(value), name + " finite matrix");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
