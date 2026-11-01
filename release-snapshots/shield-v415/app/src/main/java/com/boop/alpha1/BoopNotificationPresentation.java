package com.boop.alpha1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class BoopNotificationPresentation {
    private final List<BoopNotificationEnvelope> cards;
    private final BoopNotificationSurface surface;
    private final boolean locked;

    private BoopNotificationPresentation(
            List<BoopNotificationEnvelope> cards,
            BoopNotificationSurface surface,
            boolean locked) {
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        this.surface = surface;
        this.locked = locked;
    }

    static BoopNotificationPresentation from(
            List<BoopNotificationEnvelope> bundle,
            BoopNotificationSurface surface,
            boolean locked) {
        List<BoopNotificationEnvelope> cards = new ArrayList<>();
        if (bundle != null) {
            for (BoopNotificationEnvelope item : bundle) {
                if (item == null) continue;
                cards.add(locked ? item.redactedForLockScreen() : item);
            }
        }
        return new BoopNotificationPresentation(
                cards,
                surface == null ? BoopNotificationSurface.IN_PLACE : surface,
                locked);
    }

    List<BoopNotificationEnvelope> cards() { return cards; }
    BoopNotificationSurface surface() { return surface; }
    boolean locked() { return locked; }
}
