package com.boop.alpha1;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public final class BoopPcmRingBufferTest {
    @Test public void snapshotIsChronologicalBeforeAndAfterWrap() {
        BoopPcmRingBuffer ring = new BoopPcmRingBuffer(5);
        ring.write(new short[]{1, 2, 3}, 3);
        assertArrayEquals(new short[]{1, 2, 3}, ring.snapshot());
        ring.write(new short[]{4, 5, 6, 7}, 4);
        assertArrayEquals(new short[]{3, 4, 5, 6, 7}, ring.snapshot());
    }

    @Test public void oversizedWriteKeepsNewestSamples() {
        BoopPcmRingBuffer small = new BoopPcmRingBuffer(4);
        small.write(new short[]{10, 11, 12, 13, 14, 15}, 6);
        assertArrayEquals(new short[]{12, 13, 14, 15}, small.snapshot());
    }
}
