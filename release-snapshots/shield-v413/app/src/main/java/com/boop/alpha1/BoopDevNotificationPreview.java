package com.boop.alpha1;

import java.util.List;

final class BoopDevNotificationPreview {
    private BoopDevNotificationPreview() { }

    static BoopNotificationPresentation presentation(
            BoopDevMenuModel.Action action,
            long nowMs) {
        switch (action) {
            case NOTIFICATION_FACEBOOK:
                return single("facebook", "Facebook", "BOOP Club", "A safe local Facebook demo.", nowMs);
            case NOTIFICATION_WHATSAPP:
                return single("whatsapp", "WhatsApp", "Alex", "Local WhatsApp demo message.", nowMs);
            case NOTIFICATION_GMAIL:
                return single("gmail", "Gmail", "BOOP Lab", "Signed build receipt is ready.", nowMs);
            case NOTIFICATION_X:
                return single("x", "X / Twitter", "@boopdemo", "A local X preview. No network used.", nowMs);
            case NOTIFICATION_YOUTUBE:
                return single("youtube", "YouTube", "BOOP Lab", "New local demo video.", nowMs);
            case NOTIFICATION_MESSENGER:
                return single("messenger", "Messenger", "Sam", "Messenger preview generated locally.", nowMs);
            case NOTIFICATION_INSTAGRAM:
                return single("instagram", "Instagram", "boop.demo", "Shared a local demo update.", nowMs);
            case NOTIFICATION_DISCORD:
                return single("discord", "Discord", "# boop-lab", "Discord preview generated locally.", nowMs);
            case NOTIFICATION_SPOTIFY:
                return single("spotify", "Spotify", "Now playing", "BOOP Demo Track", nowMs);
            case NOTIFICATION_REDDIT:
                return single("reddit", "Reddit", "r/boop", "Local Reddit preview.", nowMs);
            case NOTIFICATION_LOCKED:
                return BoopNotificationPresentation.from(
                        List.of(
                                card("locked-gmail-1", "gmail", "Gmail", "Private subject one", "Private body one", nowMs),
                                card("locked-gmail-2", "gmail", "Gmail", "Private subject two", "Private body two", nowMs + 1L),
                                card("locked-gmail-3", "gmail", "Gmail", "Private subject three", "Private body three", nowMs + 2L)),
                        BoopNotificationSurface.LOCKED,
                        true);
            case NOTIFICATION_BUNDLE:
                return BoopNotificationPresentation.from(
                        List.of(
                                card("bundle-facebook", "facebook", "Facebook", "BOOP Club", "Local bundle card.", nowMs),
                                card("bundle-whatsapp", "whatsapp", "WhatsApp", "Alex", "Local bundle card.", nowMs + 1L),
                                card("bundle-gmail", "gmail", "Gmail", "BOOP Lab", "Local bundle card.", nowMs + 2L),
                                card("bundle-discord", "discord", "Discord", "# boop-lab", "Local bundle card.", nowMs + 3L)),
                        BoopNotificationSurface.OVERLAY,
                        false);
            default:
                throw new IllegalArgumentException("Not a notification demo action: " + action);
        }
    }

    private static BoopNotificationPresentation single(
            String service,
            String appLabel,
            String title,
            String text,
            long nowMs) {
        return BoopNotificationPresentation.from(
                List.of(card("dev-" + service, service, appLabel, title, text, nowMs)),
                BoopNotificationSurface.OVERLAY,
                false);
    }

    private static BoopNotificationEnvelope card(
            String key,
            String service,
            String appLabel,
            String title,
            String text,
            long postTimeMs) {
        return new BoopNotificationEnvelope(
                key,
                "boop.dev." + service,
                appLabel,
                "boop-dev-preview",
                "BOOP Dev Preview",
                title,
                text,
                postTimeMs,
                false);
    }
}
