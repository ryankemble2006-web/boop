package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import org.junit.Test;

public class BoopNotificationPolicyTest {
    @Test
    public void masterAppAndChannelAreAllRequired() {
        Set<String> apps = Set.of("com.chat.app");
        Set<String> channels = Set.of(
                BoopNotificationSettingsCodec.channelKey("com.chat.app", "messages"));

        assertFalse(BoopNotificationPolicy.allows(
                new BoopNotificationSettingsState(false, 8000L, apps, channels),
                "com.chat.app", "messages"));
        assertFalse(BoopNotificationPolicy.allows(
                new BoopNotificationSettingsState(true, 8000L, Set.of(), channels),
                "com.chat.app", "messages"));
        assertFalse(BoopNotificationPolicy.allows(
                new BoopNotificationSettingsState(true, 8000L, apps, Set.of()),
                "com.chat.app", "messages"));
        assertTrue(BoopNotificationPolicy.allows(
                new BoopNotificationSettingsState(true, 8000L, apps, channels),
                "com.chat.app", "messages"));
    }

    @Test
    public void nullStateFailsClosed() {
        assertFalse(BoopNotificationPolicy.allows(null, "com.chat.app", "messages"));
    }
}
