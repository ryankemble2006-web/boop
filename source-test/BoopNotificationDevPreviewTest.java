package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopNotificationDevPreviewTest {
    @Test
    public void unlockedPreviewUsesOneRichLocalPresentation() {
        BoopNotificationPresentation presentation = BoopDevNotificationPreview.presentation(
                BoopDevMenuModel.Action.NOTIFICATION_UNLOCKED, 1000L);

        assertEquals(BoopNotificationSurface.OVERLAY, presentation.surface());
        assertFalse(presentation.locked());
        assertEquals(1, presentation.cards().size());
        assertEquals("BOOP Dev", presentation.cards().get(0).title());
        assertEquals("Local preview. Android's notification shade is untouched.",
                presentation.cards().get(0).text());
    }

    @Test
    public void lockedPreviewUsesTheRealPrivacyRedaction() {
        BoopNotificationPresentation presentation = BoopDevNotificationPreview.presentation(
                BoopDevMenuModel.Action.NOTIFICATION_LOCKED, 2000L);

        assertEquals(BoopNotificationSurface.LOCKED, presentation.surface());
        assertTrue(presentation.locked());
        assertEquals(1, presentation.cards().size());
        assertNull(presentation.cards().get(0).title());
        assertNull(presentation.cards().get(0).text());
    }

    @Test
    public void bundlePreviewContainsThreeCardsWithoutARealNotification() {
        BoopNotificationPresentation presentation = BoopDevNotificationPreview.presentation(
                BoopDevMenuModel.Action.NOTIFICATION_BUNDLE, 3000L);

        assertEquals(BoopNotificationSurface.OVERLAY, presentation.surface());
        assertFalse(presentation.locked());
        assertEquals(3, presentation.cards().size());
        assertEquals("Mail", presentation.cards().get(0).appLabel());
        assertEquals("Calendar", presentation.cards().get(1).appLabel());
        assertEquals("Messages", presentation.cards().get(2).appLabel());
    }
}
