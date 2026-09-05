package com.boop.alpha1;

public final class BoopShakeEyeMotionHarness {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertInside(BoopShakeEyeMotion.Pose pose, float width, float height, float eyeWidth, float eyeHeight) {
        float halfW = eyeWidth * pose.scaleX() / 2f;
        float halfH = eyeHeight * pose.scaleY() / 2f;
        check(pose.centerX() - halfW >= -0.01f, "eye escaped left edge");
        check(pose.centerX() + halfW <= width + 0.01f, "eye escaped right edge");
        check(pose.centerY() - halfH >= -0.01f, "eye escaped top edge");
        check(pose.centerY() + halfH <= height + 0.01f, "eye escaped bottom edge");
    }

    public static void main(String[] args) {
        float width = 2400f;
        float height = 1080f;
        float eyeWidth = 420f;
        float eyeHeight = 500f;
        float leftX = 870f;
        float rightX = 1530f;
        float baseY = 540f;

        boolean reachedEdge = false;
        for (int i = 0; i <= 100; i++) {
            float fraction = i / 100f;
            BoopShakeEyeMotion.Pose left = BoopShakeEyeMotion.pose(true, fraction, width, height, leftX, baseY, eyeWidth, eyeHeight, 1f);
            BoopShakeEyeMotion.Pose right = BoopShakeEyeMotion.pose(false, fraction, width, height, rightX, baseY, eyeWidth, eyeHeight, 1f);
            assertInside(left, width, height, eyeWidth, eyeHeight);
            assertInside(right, width, height, eyeWidth, eyeHeight);

            float leftHalfW = eyeWidth * left.scaleX() / 2f;
            float leftHalfH = eyeHeight * left.scaleY() / 2f;
            if (left.centerX() - leftHalfW < 3f || left.centerX() + leftHalfW > width - 3f
                    || left.centerY() - leftHalfH < 3f || left.centerY() + leftHalfH > height - 3f) {
                reachedEdge = true;
            }
        }
        check(reachedEdge, "strong shake should visibly reach a real screen edge");

        BoopShakeEyeMotion.Pose midLeft = BoopShakeEyeMotion.pose(true, 0.34f, width, height, leftX, baseY, eyeWidth, eyeHeight, 0.8f);
        BoopShakeEyeMotion.Pose midRight = BoopShakeEyeMotion.pose(false, 0.34f, width, height, rightX, baseY, eyeWidth, eyeHeight, 0.8f);
        check(Math.abs(midLeft.centerX() - midRight.centerX()) > 20f || Math.abs(midLeft.centerY() - midRight.centerY()) > 20f,
                "eyes should move independently, not as one rigid face");

        BoopShakeEyeMotion.Pose settledLeft = BoopShakeEyeMotion.pose(true, 1f, width, height, leftX, baseY, eyeWidth, eyeHeight, 1f);
        BoopShakeEyeMotion.Pose settledRight = BoopShakeEyeMotion.pose(false, 1f, width, height, rightX, baseY, eyeWidth, eyeHeight, 1f);
        check(Math.abs(settledLeft.centerX() - leftX) < 0.01f && Math.abs(settledLeft.centerY() - baseY) < 0.01f,
                "left eye must settle exactly home");
        check(Math.abs(settledRight.centerX() - rightX) < 0.01f && Math.abs(settledRight.centerY() - baseY) < 0.01f,
                "right eye must settle exactly home");
        check(Math.abs(settledLeft.scaleX() - 1f) < 0.001f && Math.abs(settledLeft.scaleY() - 1f) < 0.001f,
                "settled eye scale must be normal");

        System.out.println("BOOP shake eye motion harness passed");
    }
}
