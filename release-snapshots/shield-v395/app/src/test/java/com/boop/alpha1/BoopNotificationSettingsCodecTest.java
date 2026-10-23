package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import org.junit.Test;

public class BoopNotificationSettingsCodecTest {
    @Test
    public void defaultStateDeniesEverything() {
        BoopNotificationSettingsState state = BoopNotificationSettingsState.defaults();
        assertFalse(BoopNotificationPolicy.allows(state, "com.chat.app", "messages"));
        assertEquals(8000L, state.timeoutMs());
    }

    @Test
    public void settingsRoundTripUnicodeAndSeparators() {
        Set<String> apps = Set.of("com.example|odd", "com.chat.app");
        Set<String> channels = Set.of(
                BoopNotificationSettingsCodec.channelKey("com.example|odd", "messages/a|b"),
                BoopNotificationSettingsCodec.channelKey("com.chat.app", "family"));
        BoopNotificationSettingsState original =
                new BoopNotificationSettingsState(true, 12000L, apps, channels);
        String encoded = BoopNotificationSettingsCodec.encodeState(original);
        assertEquals(original, BoopNotificationSettingsCodec.decodeState(encoded));
    }

    @Test
    public void channelMetadataRoundTripsUnicodeAndSeparators() {
        BoopNotificationChannelInfo original = new BoopNotificationChannelInfo(
                "com.example|odd", "messages/a|b", "Messages • 家族",
                true, false, false, 1234L);
        assertEquals(original, BoopNotificationChannelInfo.decode(original.encode()));
        assertTrue(original.nativeEffectsSilent());
    }
}
