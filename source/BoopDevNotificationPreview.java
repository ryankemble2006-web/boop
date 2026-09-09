package com.boop.alpha1;

import java.util.List;

final class BoopDevNotificationPreview {
    private static final String LOCAL_PREVIEW_TEXT =
            "Local preview. Android's notification shade is untouched.";

    private BoopDevNotificationPreview() { }

    static BoopNotificationPresentation presentation(
            BoopDevMenuModel.Action action,
            long nowMs) {
        switch (action) {
            case NOTIFICATION_UNLOCKED:
                return BoopNotificationPresentation.from(
                        List.of(card(
                                "dev-unlocked",
                                "com.boop.dev.preview",
                                "BOOP Demo",
                                "BOOP Dev",
                                LOCAL_PREVIEW_TEXT,
                                nowMs)),
                        BoopNotificationSurface.OVERLAY,
                        false);
            case NOTIFICATION_LOCKED:
                return BoopNotificationPresentation.from(
                        List.of(card(
                                "dev-locked",
                                "com.boop.dev.preview",
                                "BOOP Demo",
                                "Private demo title",
                                "Private demo body",
                                nowMs)),
                        BoopNotificationSurface.LOCKED,
                        true);
            case NOTIFICATION_BUNDLE:
                return BoopNotificationPresentation.from(
                        List.of(
                                card(
                                        "dev-mail",
                                        "com.boop.dev.mail",
                                        "Mail",
                                        "New message",
                                        "Your local BOOP bundle preview.",
                                        nowMs),
                                card(
                                        "dev-calendar",
                                        "com.boop.dev.calendar",
                                        "Calendar",
                                        "Coming up",
                                        "A pretend calendar item.",
                                        nowMs + 1L),
                                card(
                                        "dev-messages",
                                        "com.boop.dev.messages",
                                        "Messages",
                                        "Hello from BOOP",
                                        "Still local. Nothing entered the Android shade.",
                                        nowMs + 2L)),
                        BoopNotificationSurface.OVERLAY,
                        false);
            default:
                throw new IllegalArgumentException("Not a notification demo action: " + action);
        }
    }

    private static BoopNotificationEnvelope card(
            String key,
            String packageName,
            String appLabel,
            String title,
            String text,
            long postTimeMs) {
        return new BoopNotificationEnvelope(
                key,
                packageName,
                appLabel,
                "boop-dev-preview",
                "BOOP Dev Preview",
                title,
                text,
                postTimeMs,
                false);
    }
}
