package com.boop.shieldhome;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import java.lang.reflect.Method;
import org.junit.Test;

public final class NowPlayingAndroidContractTest {
    @Test public void listenerServiceUsesAndroidNotificationListenerBoundary() throws Exception {
        assertTrue(NotificationListenerService.class.isAssignableFrom(
                ShieldNowPlayingListenerService.class));
        assertNotNull(ShieldNowPlayingListenerService.class.getDeclaredMethod("onListenerConnected"));
        assertNotNull(ShieldNowPlayingListenerService.class.getDeclaredMethod("onListenerDisconnected"));
    }

    @Test public void managerExposesStateAccessPreferenceAndTransportBoundary() throws Exception {
        assertNotNull(ShieldNowPlayingManager.class.getMethod("get", Context.class));
        assertNotNull(ShieldNowPlayingManager.class.getMethod("state"));
        assertNotNull(ShieldNowPlayingManager.class.getMethod("refreshAccess"));
        assertNotNull(ShieldNowPlayingManager.class.getMethod("openAccessSettings", Activity.class));
        assertNotNull(ShieldNowPlayingManager.class.getMethod("setPreferredPackage", String.class));
        for (String name : new String[] {
                "previous", "rewind", "togglePlayPause", "fastForward", "next"
        }) {
            Method method = ShieldNowPlayingManager.class.getMethod(name);
            assertNotNull(method);
        }
        assertNotNull(ShieldNowPlayingManager.class.getMethod("openSource", Activity.class));
    }
}
