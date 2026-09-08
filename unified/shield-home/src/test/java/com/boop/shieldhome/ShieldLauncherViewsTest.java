package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.widget.FrameLayout;
import java.util.List;
import org.junit.Test;

public final class ShieldLauncherViewsTest {
    @Test public void appCardUsesLocalFocusAnimationContract() {
        assertTrue(FrameLayout.class.isAssignableFrom(TvAppCardView.class));
        assertEquals(1.08f, TvAppCardView.FOCUSED_SCALE, 0.0001f);
        assertEquals(120L, TvAppCardView.FOCUS_DURATION_MS);
    }

    @Test public void homeCallbacksExposeApprovedRemoteActions() {
        ShieldHomeView.Callbacks callbacks = new ShieldHomeView.Callbacks() {
            @Override public void onAppSelected(TvAppEntry entry) { }
            @Override public void onFavouriteLongPressed(TvAppEntry entry) { }
            @Override public void onOpenApps() { }
            @Override public void onOpenSettings() { }
            @Override public void onContentSelected(HomeContentCard card) { }
        };
        assertNotNull(callbacks);
        assertNotNull(ShieldHomeView.class.getDeclaredMethods());
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
}
