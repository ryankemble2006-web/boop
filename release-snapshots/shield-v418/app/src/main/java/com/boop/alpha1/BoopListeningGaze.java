package com.boop.alpha1;

/** Pure geometry/compositing policy for the active-listening reading gaze. */
final class BoopListeningGaze {
    static final long HALF_SWEEP_MS = 720L;
    private static final float HORIZONTAL_SOURCE_PX = 48f;
    private static final float VERTICAL_SOURCE_PX = 22f;
    private static final float PATCH_RADIUS_SOURCE_PX = 260f;
    private static final float LISTENING_ZOOM = 1.16f;

    private BoopListeningGaze() { }

    static float horizontalSourceOffset(float fraction) {
        float clamped = Math.max(0f, Math.min(1f, fraction));
        return -HORIZONTAL_SOURCE_PX + (HORIZONTAL_SOURCE_PX * 2f * clamped);
    }

    static float verticalSourceOffset() {
        return VERTICAL_SOURCE_PX;
    }

    static float patchRadiusSource() {
        return PATCH_RADIUS_SOURCE_PX;
    }

    static float zoom() {
        return LISTENING_ZOOM;
    }

    static boolean clearBaseApertureBeforeShiftedPatch() {
        return true;
    }
}
