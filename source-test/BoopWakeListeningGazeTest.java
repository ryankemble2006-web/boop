package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import org.junit.Test;

public final class BoopWakeListeningGazeTest {
    @Test
    public void readingSweepMovesAcrossPageWithDownwardBiasAndClearZoom() {
        assertEquals(-48f, BoopListeningGaze.horizontalSourceOffset(0f), 0.001f);
        assertEquals(0f, BoopListeningGaze.horizontalSourceOffset(0.5f), 0.001f);
        assertEquals(48f, BoopListeningGaze.horizontalSourceOffset(1f), 0.001f);
        assertEquals(-48f, BoopListeningGaze.horizontalSourceOffset(-1f), 0.001f);
        assertEquals(48f, BoopListeningGaze.horizontalSourceOffset(2f), 0.001f);
        assertEquals(22f, BoopListeningGaze.verticalSourceOffset(), 0.001f);
        assertEquals(260f, BoopListeningGaze.patchRadiusSource(), 0.001f);
        assertTrue(BoopListeningGaze.zoom() >= 1.15f);
    }

    @Test
    public void movingReadingLayerReplacesBaseApertureInsteadOfStackingOverIt() throws Exception {
        Method clearMethod = null;
        for (Method method : BoopListeningGaze.class.getDeclaredMethods()) {
            if ("clearBaseApertureBeforeShiftedPatch".equals(method.getName())) {
                clearMethod = method;
                break;
            }
        }
        assertTrue("reading gaze must clear the stationary base aperture before drawing the shifted layer",
                clearMethod != null);
        clearMethod.setAccessible(true);
        assertEquals(Boolean.TRUE, clearMethod.invoke(null));
    }
}
