package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class NowPlayingPuppetEyePlacementTest {
    @Test public void approvedPairRemainsOneUndistortedLevelMaster() {
        NowPlayingPuppetEyePlacement.Box pair = NowPlayingPuppetEyePlacement.pairInHeadphoneSource();
        NowPlayingPuppetEyePlacement.Box left = NowPlayingPuppetEyePlacement.leftEyeInHeadphoneSource();
        NowPlayingPuppetEyePlacement.Box right = NowPlayingPuppetEyePlacement.rightEyeInHeadphoneSource();

        assertEquals(2f, pair.width() / pair.height(), 0.0001f);
        assertEquals(left.width(), right.width(), 0.0001f);
        assertEquals(left.height(), right.height(), 0.0001f);
        assertEquals(left.top, right.top, 0.0001f);
        assertEquals(left.bottom, right.bottom, 0.0001f);
        assertEquals(723f / 767f, left.width() / left.height(), 0.0001f);

        // The authority PNG itself is one source pixel left of mathematical canvas centre.
        // Preserve that exact quirk rather than silently 'correcting' BOOP's approved identity.
        float masterEyeMidpointX = (463.5f + 1308.5f) / 2f;
        float expectedMidpoint = pair.left
                + (masterEyeMidpointX / NowPlayingPuppetEyePlacement.MASTER_WIDTH) * pair.width();
        assertEquals(expectedMidpoint, (left.centreX() + right.centreX()) / 2f, 0.0001f);
    }
}
