package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
