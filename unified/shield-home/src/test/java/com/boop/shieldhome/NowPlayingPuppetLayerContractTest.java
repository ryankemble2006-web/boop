package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;

/** Non-visual contract for the approved layered BOOP puppet blink. */
public final class NowPlayingPuppetLayerContractTest {
    @Test public void puppetUsesDedicatedLayeredRenderer() throws Exception {
        Field puppet = ShieldNowPlayingPuppetView.class.getDeclaredField("puppet");
        assertEquals("LayeredPuppetView", puppet.getType().getSimpleName());
    }

    @Test public void approvedEyeMasterIsPackagedAsItsOwnLayer() throws Exception {
        try {
            Class<?> drawable = Class.forName("com.boop.shieldhome.R$drawable");
            drawable.getDeclaredField("boop_approved_eyes");
        } catch (ClassNotFoundException | NoSuchFieldException missing) {
            fail("approved BOOP eye master must be packaged as boop_approved_eyes");
        }
    }

    @Test public void provenBlinkCurveDrivesOneTopLidToFullClosure() throws Exception {
        final Class<?> travel;
        try {
            travel = Class.forName("com.boop.shieldhome.NowPlayingPuppetLidTravel");
        } catch (ClassNotFoundException missing) {
            fail("top-lid travel helper is missing");
            return;
        }

        Method closure = travel.getDeclaredMethod("closureForOpenness", float.class);
        Method centre = travel.getDeclaredMethod("centreEdge", float.class);
        Method sides = travel.getDeclaredMethod("sideEdge", float.class);
        closure.setAccessible(true);
        centre.setAccessible(true);
        sides.setAccessible(true);

        assertEquals(0f, (Float) closure.invoke(null, 1f), 0.0001f);
        assertEquals(1f, (Float) closure.invoke(null, 0.05f), 0.0001f);
        assertTrue((Float) centre.invoke(null, 0.05f) > 1f);
        assertTrue((Float) sides.invoke(null, 0.05f) > 1f);
        assertTrue((Float) centre.invoke(null, 0.50f) < (Float) centre.invoke(null, 0.25f));
        assertTrue((Float) sides.invoke(null, 0.50f) < (Float) sides.invoke(null, 0.25f));
    }
}
