package com.boop.shieldhdrdebug;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OverlayGeometryTest {
    @Test
    public void eyesRemainSmallAndUpperRightAt1080p() {
        OverlayGeometry.Geometry geometry = OverlayGeometry.eyes(1920, 1080);
        assertEquals(269, geometry.width);
        assertEquals(138, geometry.height);
        assertEquals(58, geometry.x);
        assertEquals(32, geometry.y);
    }

    @Test
    public void panelIsLargeAndPhotoFriendlyAt1080p() {
        OverlayGeometry.Geometry geometry = OverlayGeometry.panel(1920, 1080);
        assertEquals(998, geometry.width);
        assertEquals(994, geometry.height);
        assertEquals(38, geometry.x);
        assertEquals(32, geometry.y);
    }
}
