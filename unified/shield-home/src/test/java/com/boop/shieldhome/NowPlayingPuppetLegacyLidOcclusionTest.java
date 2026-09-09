package com.boop.shieldhome;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import org.junit.Test;

/** Non-visual regression contract for the duplicate legacy headphone eyelid seen on Shield. */
public final class NowPlayingPuppetLegacyLidOcclusionTest {
    @Test public void legacyCapExtendsAboveAndAcrossEachOldEyeSlot() throws Exception {
        final Class<?> geometry;
        try {
            geometry = Class.forName("com.boop.shieldhome.NowPlayingPuppetLegacyLidOcclusion");
        } catch (ClassNotFoundException missing) {
            fail("legacy eyelid occlusion geometry is missing");
            return;
        }

        Method left = geometry.getDeclaredMethod("leftCap");
        Method right = geometry.getDeclaredMethod("rightCap");
        left.setAccessible(true);
        right.setAccessible(true);

        assertCapCoversOldLid(left.invoke(null), 395f, 465f, 711f, 790f);
        assertCapCoversOldLid(right.invoke(null), 757f, 515f, 1075f, 850f);
    }

    private static void assertCapCoversOldLid(
            Object cap,
            float oldLeft,
            float oldTop,
            float oldRight,
            float oldBottom) throws Exception {
        Class<?> box = cap.getClass();
        float left = box.getDeclaredField("left").getFloat(cap);
        float top = box.getDeclaredField("top").getFloat(cap);
        float right = box.getDeclaredField("right").getFloat(cap);
        float bottom = box.getDeclaredField("bottom").getFloat(cap);
        float width = oldRight - oldLeft;
        float height = oldBottom - oldTop;

        assertTrue("legacy cap must begin well above the old eye slot", top <= oldTop - height * 0.24f);
        assertTrue("legacy cap must overlap the top of the old eye", bottom >= oldTop + height * 0.20f);
        assertTrue("legacy cap must extend left of the old lid", left <= oldLeft - width * 0.08f);
        assertTrue("legacy cap must extend right of the old lid", right >= oldRight + width * 0.08f);
    }
}
