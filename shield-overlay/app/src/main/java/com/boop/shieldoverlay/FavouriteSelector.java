package com.boop.shieldoverlay;

import java.util.List;

public final class FavouriteSelector {
    public EntityCard select(String selectedAreaId, List<EntityCard> cards) {
        String areaId = clean(selectedAreaId);
        if (areaId == null || cards == null) {
            return null;
        }

        for (EntityCard card : cards) {
            if (!isCandidate(areaId, card)) {
                continue;
            }
            return card;
        }
        return null;
    }

    private static boolean isCandidate(String selectedAreaId, EntityCard card) {
        if (card == null || !selectedAreaId.equals(card.areaId()) || card.hidden()) {
            return false;
        }

        String category = clean(card.entityCategory());
        if (category != null
                && ("config".equalsIgnoreCase(category)
                || "diagnostic".equalsIgnoreCase(category))) {
            return false;
        }

        String domain = card.domain();
        if (!("light".equals(domain)
                || "switch".equals(domain)
                || "fan".equals(domain)
                || "input_boolean".equals(domain))) {
            return false;
        }

        return "on".equals(card.state()) || "off".equals(card.state());
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
