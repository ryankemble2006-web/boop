package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class BoopNotificationDevPreviewTest {
    @Test
    public void eachApprovedServiceUsesOneUnlockedLocalPresentation() {
        Map<BoopDevMenuModel.Action, String> expected = new LinkedHashMap<>();
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_FACEBOOK, "Facebook");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_WHATSAPP, "WhatsApp");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_GMAIL, "Gmail");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_X, "X / Twitter");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_YOUTUBE, "YouTube");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_MESSENGER, "Messenger");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_INSTAGRAM, "Instagram");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_DISCORD, "Discord");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_SPOTIFY, "Spotify");
        expected.put(BoopDevMenuModel.Action.NOTIFICATION_REDDIT, "Reddit");

        long now = 1_000L;
        for (Map.Entry<BoopDevMenuModel.Action, String> entry : expected.entrySet()) {
            BoopNotificationPresentation presentation =
                    BoopDevNotificationPreview.presentation(entry.getKey(), now++);
            assertEquals(BoopNotificationSurface.OVERLAY, presentation.surface());
            assertFalse(presentation.locked());
            assertEquals(1, presentation.cards().size());
            assertEquals(entry.getValue(), presentation.cards().get(0).appLabel());
        }
    }

    @Test
    public void lockedPreviewUsesRealPrivacyRedactionAndRetainsCount() {
        BoopNotificationPresentation presentation = BoopDevNotificationPreview.presentation(
                BoopDevMenuModel.Action.NOTIFICATION_LOCKED, 2_000L);

        assertEquals(BoopNotificationSurface.LOCKED, presentation.surface());
        assertTrue(presentation.locked());
        assertEquals(3, presentation.cards().size());
        for (BoopNotificationEnvelope card : presentation.cards()) {
            assertNull(card.title());
            assertNull(card.text());
        }
    }

    @Test
    public void bundlePreviewContainsOnlyFakeServiceCards() {
        BoopNotificationPresentation presentation = BoopDevNotificationPreview.presentation(
                BoopDevMenuModel.Action.NOTIFICATION_BUNDLE, 3_000L);

        assertEquals(BoopNotificationSurface.OVERLAY, presentation.surface());
        assertFalse(presentation.locked());
        assertEquals(4, presentation.cards().size());
        assertEquals("Facebook", presentation.cards().get(0).appLabel());
        assertEquals("WhatsApp", presentation.cards().get(1).appLabel());
        assertEquals("Gmail", presentation.cards().get(2).appLabel());
        assertEquals("Discord", presentation.cards().get(3).appLabel());
    }
}
