package com.boop.shieldoverlay;

/** Devices Home can actually operate, not HA helpers or maintenance entities. */
final class RoomDeviceControls {
    private RoomDeviceControls() { }

    static boolean isActionable(EntityCard card) {
        if (card == null || card.hidden()) return false;
        String category = card.entityCategory();
        if (category != null && !category.trim().isEmpty()) return false;
        String domain = card.domain();
        return ("light".equals(domain) || "switch".equals(domain) || "fan".equals(domain))
                && ("on".equals(card.state()) || "off".equals(card.state()));
    }
}
