package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Non-visual arithmetic coverage for the approved eye master's full iris treatment. */
public final class BoopWakeIrisTintCoverageTest {
    @Test public void correctedMasterGeometryIncludesPreviouslyMissedBlueEdges() {
        assertTrue(BoopIrisTintMath.inIris(535, 340, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(733, 543, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(1233, 340, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(1431, 543, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(535, 543, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(1233, 543, 1774, 887));
    }

    @Test public void darkBlueIrisShadingRetintsWhileNeutralScleraStaysNeutral() {
        int darkUpperIris = 0xff07111e;
        assertNotEquals(darkUpperIris, BoopIrisTintMath.tint(darkUpperIris, 120));
        assertEquals(0xffc0d8e9, BoopIrisTintMath.tint(0xffc0d8e9, 120));
    }
}
