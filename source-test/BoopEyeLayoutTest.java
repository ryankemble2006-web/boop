package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopEyeLayoutTest {
    private static final float EPSILON = 0.75f;

    @Test public void portraitKeepsExistingFullFaceMode() {
        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(1440, 3120);
        assertFalse(layout.landscape());
    }

    @Test public void landscapeKeepsPortraitEyeCentreDistanceAndCentresPair() {
        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(3120, 1440);
        assertTrue(layout.landscape());

        float portraitScale = 1440f / BoopEyeLayout.SOURCE_WIDTH;
        float expectedDistance = BoopEyeLayout.SOURCE_EYE_CENTRE_DISTANCE * portraitScale;
        float actualDistance = layout.right().centerX() - layout.left().centerX();

        assertEquals(expectedDistance, actualDistance, EPSILON);
        assertEquals(1560f,
                (layout.left().centerX() + layout.right().centerX()) / 2f,
                EPSILON);
        assertEquals(720f, layout.left().centerY(), EPSILON);
        assertEquals(720f, layout.right().centerY(), EPSILON);
    }

    @Test public void landscapeMakesEachEyeTwentyPercentLargerWithoutWideningFace() {
        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(3120, 1440);
        float portraitScale = 1440f / BoopEyeLayout.SOURCE_WIDTH;
        float portraitEyeWidth = BoopEyeLayout.SOURCE_EYE_WIDTH * portraitScale;
        float portraitEyeHeight = BoopEyeLayout.SOURCE_EYE_HEIGHT * portraitScale;

        assertEquals(portraitEyeWidth * 1.20f, layout.left().width(), EPSILON);
        assertEquals(portraitEyeHeight * 1.20f, layout.left().height(), EPSILON);
        assertEquals(layout.left().width(), layout.right().width(), EPSILON);
        assertEquals(layout.left().height(), layout.right().height(), EPSILON);
    }
}
