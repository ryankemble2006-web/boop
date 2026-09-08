package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.Test;

public class BoopNotificationCoordinatorTest {
    @Test
    public void secondWithinFourSecondsCreatesBundleWithoutSecondCue() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationSettingsState settings = allowed("com.chat", "messages");

        assertTrue(coordinator.onPosted(
                fixture("k1", "com.chat", "messages", "A", "1"), 1000L, settings).playCue());

        BoopNotificationCoordinator.Decision second = coordinator.onPosted(
                fixture("k2", "com.chat", "messages", "B", "2"), 2200L, settings);
        assertEquals(BoopNotificationCoordinator.Kind.UPDATE, second.kind());
        assertFalse(second.playCue());
        assertEquals(2, second.bundle().size());
    }

    @Test
    public void mixedAppsBundleInsideWindowWhenEachIsAllowed() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationSettingsState settings = new BoopNotificationSettingsState(
                true,
                8000L,
                Set.of("com.chat", "com.mail"),
                Set.of(
                        BoopNotificationSettingsCodec.channelKey("com.chat", "messages"),
                        BoopNotificationSettingsCodec.channelKey("com.mail", "inbox")));

        coordinator.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, settings);
        BoopNotificationCoordinator.Decision second = coordinator.onPosted(
                fixture("k2", "com.mail", "inbox", "Mail", "2"), 2000L, settings);

        assertEquals(2, second.bundle().size());
        assertFalse(second.playCue());
    }

    @Test
    public void separateLateSecondNotificationStartsFreshSingle() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationSettingsState settings = allowed("com.chat", "messages");
        coordinator.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, settings);

        BoopNotificationCoordinator.Decision second = coordinator.onPosted(
                fixture("k2", "com.chat", "messages", "B", "2"), 6500L, settings);
        assertEquals(BoopNotificationCoordinator.Kind.PRESENT, second.kind());
        assertTrue(second.playCue());
        assertEquals(1, second.bundle().size());
        assertEquals("k2", second.bundle().get(0).key());
        assertEquals(2, coordinator.activeNotifications().size());
    }

    @Test
    public void repostExistingVisibleKeyUpdatesWithoutCue() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationSettingsState settings = allowed("com.chat", "messages");
        coordinator.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, settings);

        BoopNotificationCoordinator.Decision updated = coordinator.onPosted(
                fixture("k1", "com.chat", "messages", "A", "edited"), 1500L, settings);
        assertEquals(BoopNotificationCoordinator.Kind.UPDATE, updated.kind());
        assertFalse(updated.playCue());
        assertEquals("edited", updated.bundle().get(0).text());
    }

    @Test
    public void deniedNotificationNeverEntersActiveOrVisibleState() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationCoordinator.Decision decision = coordinator.onPosted(
                fixture("k1", "com.chat", "messages", "A", "1"),
                1000L,
                BoopNotificationSettingsState.defaults());

        assertEquals(BoopNotificationCoordinator.Kind.IGNORE, decision.kind());
        assertTrue(coordinator.activeNotifications().isEmpty());
        assertTrue(coordinator.visibleBundle().isEmpty());
    }

    @Test
    public void rebuildNeverInterruptsAndRemovalCleansState() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationSettingsState settings = allowed("com.chat", "messages");
        coordinator.rebuild(List.of(
                fixture("k1", "com.chat", "messages", "A", "1")), settings);

        assertEquals(1, coordinator.activeNotifications().size());
        assertTrue(coordinator.visibleBundle().isEmpty());
        coordinator.onRemoved("k1");
        assertTrue(coordinator.activeNotifications().isEmpty());
    }

    @Test
    public void dismissalClearsVisibleBundleButKeepsActiveNotifications() {
        BoopNotificationCoordinator coordinator = new BoopNotificationCoordinator(4000L);
        BoopNotificationSettingsState settings = allowed("com.chat", "messages");
        coordinator.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, settings);
        coordinator.onPresentationDismissed();

        assertTrue(coordinator.visibleBundle().isEmpty());
        assertEquals(1, coordinator.activeNotifications().size());
    }

    private static BoopNotificationSettingsState allowed(String packageName, String channelId) {
        return new BoopNotificationSettingsState(
                true,
                8000L,
                Set.of(packageName),
                Set.of(BoopNotificationSettingsCodec.channelKey(packageName, channelId)));
    }

    private static BoopNotificationEnvelope fixture(
            String key,
            String packageName,
            String channelId,
            String title,
            String text) {
        return new BoopNotificationEnvelope(
                key, packageName, packageName, channelId, channelId,
                title, text, 100L, true);
    }
}
