package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;

public class BoopNotificationIntakePolicyTest {
    @Test
    public void deniedChannelCannotReadRichContent() {
        assertEquals(BoopNotificationIntakePolicy.Mode.OBSERVE_CHANNEL_ONLY,
                BoopNotificationIntakePolicy.decide(
                        BoopNotificationSettingsState.defaults(),
                        "com.chat.app", "messages"));
    }

    @Test
    public void allowedChannelMayReadRichContent() {
        BoopNotificationSettingsState state = fullyAllowed("com.chat.app", "messages");
        assertEquals(BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT,
                BoopNotificationIntakePolicy.decide(state, "com.chat.app", "messages"));
    }

    @Test
    public void fullyAllowedChannelCanReadRichContentButSiblingCannot() {
        BoopNotificationSettingsState state = fullyAllowed("com.chat", "messages");
        assertEquals(BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT,
                BoopNotificationIntakePolicy.decide(state, "com.chat", "messages"));
        assertEquals(BoopNotificationIntakePolicy.Mode.OBSERVE_CHANNEL_ONLY,
                BoopNotificationIntakePolicy.decide(state, "com.chat", "promotions"));
    }

    private static BoopNotificationSettingsState fullyAllowed(String packageName, String channelId) {
        return new BoopNotificationSettingsState(
                true,
                8000L,
                Set.of(packageName),
                Set.of(BoopNotificationSettingsCodec.channelKey(packageName, channelId)));
    }
}
