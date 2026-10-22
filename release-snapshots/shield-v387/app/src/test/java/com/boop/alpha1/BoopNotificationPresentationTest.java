package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import org.junit.Test;

public class BoopNotificationPresentationTest {
    @Test
    public void lockedPresentationRemovesRichText() {
        BoopNotificationEnvelope notification = fixture(
                "k1", "com.chat", "messages", "Alice", "Dinner?");

        BoopNotificationPresentation presentation = BoopNotificationPresentation.from(
                List.of(notification), BoopNotificationSurface.LOCKED, true);

        assertEquals(BoopNotificationSurface.LOCKED, presentation.surface());
        assertEquals(1, presentation.cards().size());
        BoopNotificationEnvelope card = presentation.cards().get(0);
        assertEquals("k1", card.key());
        assertEquals("com.chat", card.packageName());
        assertEquals("Chat", card.appLabel());
        assertNull(card.title());
        assertNull(card.text());
    }

    @Test
    public void unlockedPresentationRetainsRichText() {
        BoopNotificationEnvelope notification = fixture(
                "k1", "com.chat", "messages", "Alice", "Dinner?");

        BoopNotificationPresentation presentation = BoopNotificationPresentation.from(
                List.of(notification), BoopNotificationSurface.OVERLAY, false);

        assertEquals("Alice", presentation.cards().get(0).title());
        assertEquals("Dinner?", presentation.cards().get(0).text());
    }

    private static BoopNotificationEnvelope fixture(
            String key,
            String packageName,
            String channelId,
            String title,
            String text) {
        return new BoopNotificationEnvelope(
                key, packageName, "Chat", channelId, "Messages",
                title, text, 100L, true);
    }
}
