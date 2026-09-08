package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import android.view.accessibility.AccessibilityEvent;

import org.junit.Test;

public final class ShieldAccessibilityEventPolicyTest {
    @Test public void keepsHomeWindowHandlingAndAddsMediaNotificationHandling() {
        assertEquals(
                ShieldAccessibilityEventPolicy.Action.HOME_WINDOW,
                ShieldAccessibilityEventPolicy.actionFor(
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED));
        assertEquals(
                ShieldAccessibilityEventPolicy.Action.MEDIA_NOTIFICATION,
                ShieldAccessibilityEventPolicy.actionFor(
                        AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED));
        assertEquals(
                ShieldAccessibilityEventPolicy.Action.IGNORE,
                ShieldAccessibilityEventPolicy.actionFor(
                        AccessibilityEvent.TYPE_VIEW_FOCUSED));
    }
}
