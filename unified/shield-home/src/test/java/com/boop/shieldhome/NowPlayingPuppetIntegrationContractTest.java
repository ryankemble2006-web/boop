package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import org.junit.Test;

public final class NowPlayingPuppetIntegrationContractTest {
    @Test public void puppetViewCanBeGatedToHomeWithoutChangingMediaState() throws Exception {
        assertNotNull(ShieldNowPlayingPuppetView.class.getMethod("setSnapshot", NowPlayingSnapshot.class));
        assertNotNull(ShieldNowPlayingPuppetView.class.getMethod("setHomeVisible", boolean.class));
    }

    @Test public void launcherOwnsOneIndependentPuppetLayer() throws Exception {
        Field field = ShieldLauncherActivity.class.getDeclaredField("nowPlayingPuppetView");
        assertEquals(ShieldNowPlayingPuppetView.class, field.getType());
    }
}
