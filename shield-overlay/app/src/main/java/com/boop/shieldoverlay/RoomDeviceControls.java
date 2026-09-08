package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Physical room devices Home can actually operate, not HA entity plumbing. */
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

    static List<EntityCard> collapseToDevices(List<EntityCard> cards) {
        Map<String, EntityCard> bestByDevice = new LinkedHashMap<>();
        if (cards == null) return new ArrayList<>();

        for (EntityCard card : cards) {
            if (!isActionable(card)) continue;
            String deviceId = clean(card.deviceId());
            if (deviceId == null) {
                // BOOP Home deliberately shows devices, not loose HA entities/groups/helpers.
                continue;
            }
            EntityCard candidate = card;
            String deviceName = clean(card.deviceName());
            if (deviceName != null && !deviceName.equals(card.displayName())) {
                candidate = card.withDisplayName(deviceName);
            }
            EntityCard current = bestByDevice.get(deviceId);
            if (current == null || rank(candidate) > rank(current)) {
                bestByDevice.put(deviceId, candidate);
            }
        }
        return new ArrayList<>(bestByDevice.values());
    }

    private static int rank(EntityCard card) {
        String domain = card.domain();
        if ("fan".equals(domain)) return 30;
        if ("light".equals(domain)) return 30;
        if ("switch".equals(domain)) return 10;
        return 0;
    }

    private static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
