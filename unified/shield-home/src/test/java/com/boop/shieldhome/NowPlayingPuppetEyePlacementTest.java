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
        assertEquals(pair.centreX(), (left.centreX() + right.centreX()) / 2f, 0.0001f);
    }
}
