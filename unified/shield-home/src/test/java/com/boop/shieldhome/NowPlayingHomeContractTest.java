package com.boop.shieldhome;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.widget.FrameLayout;
import java.lang.reflect.Method;
import org.junit.Test;

public final class NowPlayingHomeContractTest {
    @Test public void nowPlayingPanelExistsAndBindsLauncherSnapshot() throws Exception {
        Class<?> panel = Class.forName("com.boop.shieldhome.ShieldNowPlayingView");
        assertTrue(FrameLayout.class.isAssignableFrom(panel));
        Method bind = panel.getMethod(
                "bind", NowPlayingSnapshot.class, ShieldHomeView.Callbacks.class);
        assertNotNull(bind);
    }

    @Test public void homeSupportsMediaOnlySnapshotUpdates() throws Exception {
        assertNotNull(ShieldHomeView.class.getMethod("setNowPlaying", NowPlayingSnapshot.class));
        assertNotNull(ShieldHomeView.class.getMethod(
                "render",
                java.util.List.class,
                java.util.List.class,
                NowPlayingSnapshot.class,
                ShieldHomeView.Callbacks.class));
    }

    @Test public void homeCallbacksExposeNowPlayingTransportAndSourceActions() throws Exception {
        Class<?> callbacks = ShieldHomeView.Callbacks.class;
        for (String name : new String[] {
                "onNowPlayingPrevious",
                "onNowPlayingRewind",
                "onNowPlayingPlayPause",
                "onNowPlayingFastForward",
                "onNowPlayingNext",
                "onOpenNowPlayingSource"
        }) {
            assertNotNull(callbacks.getMethod(name));
        }
    }
}
