package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.provider.Settings;
import android.widget.FrameLayout;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;

public final class ShieldLauncherViewsTest {
    @Test public void appCardUsesLocalFocusAndGrabAnimationContract() {
        assertTrue(FrameLayout.class.isAssignableFrom(TvAppCardView.class));
        assertEquals(1.08f, TvAppCardView.FOCUSED_SCALE, 0.0001f);
        assertEquals(1.14f, TvAppCardView.GRABBED_SCALE, 0.0001f);
        assertEquals(120L, TvAppCardView.FOCUS_DURATION_MS);
    }

    @Test public void favouriteCardsExposeBannerFirstBinding() throws Exception {
        Method method = TvAppCardView.class.getMethod("bindFavourite", TvAppEntry.class);
        assertNotNull(method);
    }

    @Test public void homeCallbacksExposeGrabHomeRowsAndRealShieldSettings() {
        ShieldHomeView.Callbacks callbacks = new ShieldHomeView.Callbacks() {
            @Override public void onAppSelected(TvAppEntry entry) { }
            @Override public void onFavouriteOrderCommitted(List<String> components) { }
            @Override public void onOpenApps() { }
            @Override public void onOpenHomeRows() { }
            @Override public void onOpenSystemSettings() { }
            @Override public void onContentSelected(HomeContentCard card) { }
        };
        assertNotNull(callbacks);
        assertNotNull(ShieldHomeView.class.getDeclaredMethods());
        assertEquals(Settings.ACTION_SETTINGS, ShieldLauncherActivity.systemSettingsAction());
    }

    @Test public void appsCallbacksExposeLaunchAndFavouriteToggleOnly() {
        ShieldAppsView.Callbacks callbacks = new ShieldAppsView.Callbacks() {
            @Override public void onAppSelected(TvAppEntry entry) { }
            @Override public void onToggleFavourite(TvAppEntry entry) { }
        };
        assertNotNull(callbacks);
        assertNotNull(ShieldAppsView.class.getDeclaredMethods());
    }

    @Test public void settingsCallbacksExposeIndependentRowsAndHomeChooser() {
        ShieldHomeSettingsView.Callbacks callbacks = new ShieldHomeSettingsView.Callbacks() {
            @Override public void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled) { }
            @Override public void onChooseHomeApp() { }
            @Override public void onBackHome() { }
        };
        assertNotNull(callbacks);
        assertEquals(List.of(OptionalRowRegistry.Key.PLAY_NEXT, OptionalRowRegistry.Key.APP_CHANNELS),
                List.of(OptionalRowRegistry.Key.values()));
    }

    @Test public void launcherSettingsExposeNowPlayingSetupContract() throws Exception {
        assertEquals("Launcher Settings", ShieldHomeSettingsView.launcherSettingsLabel());
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getMethod("onOpenNowPlayingAccess"));
        assertNotNull(ShieldHomeSettingsView.Callbacks.class.getMethod("onChooseNowPlayingPlayer"));
        assertNotNull(ShieldHomeSettingsView.class.getMethod(
                "render",
                boolean.class,
                boolean.class,
                boolean.class,
                boolean.class,
                String.class,
                ShieldHomeSettingsView.Callbacks.class));
        assertNotNull(ShieldNowPlayingManager.class.getMethod("hasAccess"));
        assertNotNull(ShieldNowPlayingManager.class.getMethod("openAccessSettings", Activity.class));
    }
}
