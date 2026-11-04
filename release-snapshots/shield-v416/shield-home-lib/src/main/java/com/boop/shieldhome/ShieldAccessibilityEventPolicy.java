package com.boop.shieldhome;

import android.view.accessibility.AccessibilityEvent;

/** Keeps the protected Home override event separate from media-session notification events. */
final class ShieldAccessibilityEventPolicy {
    enum Action {
        HOME_WINDOW,
        MEDIA_NOTIFICATION,
        IGNORE
    }

    private ShieldAccessibilityEventPolicy() { }

    static Action actionFor(int eventType) {
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return Action.HOME_WINDOW;
        }
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return Action.MEDIA_NOTIFICATION;
        }
        return Action.IGNORE;
    }
}
