package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class OverlayGeometryTest {
    @Test public void usesFourteenPercentWidthAndThreePercentInsets() {
        OverlayGeometry.Geometry g = OverlayGeometry.calculate(1920, 1080);

        assertEquals(269, g.width());
        assertEquals(138, g.height());
        assertEquals(58, g.x());
        assertEquals(32, g.y());
    }
}
