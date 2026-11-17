package com.boop.alpha1;

import android.graphics.Rect;

/**
 * Geometry for Ryan's approved permanent BOOP eye master.
 *
 * This class contains framing coordinates only. It does not alter pixels, alpha,
 * colour, or appearance. Visual acceptance remains manual on Ryan's devices.
 */
final class BoopApprovedEyeGeometry {
    static final int CANVAS_WIDTH = 1774;
    static final int CANVAS_HEIGHT = 887;

    // Equal-size crops include the complete rendered eye bodies plus a small
    // transparent safety margin. Supplied PNG alpha is retained untouched.
    static final Rect LEFT_SOURCE = new Rect(90, 50, 840, 850);
    static final Rect RIGHT_SOURCE = new Rect(930, 50, 1680, 850);

    static final float EYE_CENTRE_DISTANCE = 840f;
    static final float EYE_WIDTH = 750f;
    static final float EYE_HEIGHT = 800f;

    private BoopApprovedEyeGeometry() { }
}
