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
        BoopNotificationSettingsState state = new BoopNotificationSettingsState(
                true,
                8000L,
                Set.of("com.chat.app"),
                Set.of(BoopNotificationSettingsCodec.channelKey("com.chat.app", "messages")));
        assertEquals(BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT,
                BoopNotificationIntakePolicy.decide(state, "com.chat.app", "messages"));
    }
}
