package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Physical room devices Home can actually operate, not HA entity plumbing. */
final class RoomDeviceControls {
    // Home Assistant FanEntityFeature.TURN_OFF = 16, TURN_ON = 32.
    private static final long FAN_POWER_FEATURES = 16L | 32L;

    private RoomDeviceControls() { }

    static boolean isActionable(EntityCard card) {
        if (card == null || card.hidden()) return false;
        String category = card.entityCategory();
        if (category != null && !category.trim().isEmpty()) return false;
        String domain = card.domain();
        if (!("on".equals(card.state()) || "off".equals(card.state()))) return false;
        if ("fan".equals(domain)) return fanPowerSupported(card);
        return "light".equals(domain) || "switch".equals(domain);
    }

    static boolean isSemanticFan(EntityCard card) {
        if (card == null) return false;
        if ("fan".equals(card.domain())) return true;
        String label = ((card.displayName() == null ? "" : card.displayName()) + " "
                + (card.deviceName() == null ? "" : card.deviceName()) + " "
                + card.entityId()).toLowerCase(java.util.Locale.ROOT);
        return label.matches(".*\\bfan\\b.*") || label.contains("_fan") || label.contains("fan_");
    }

    static List<EntityCard> collapseToDevices(List<EntityCard> cards) {
        Map<String, EntityCard> bestByDevice = new LinkedHashMap<>();
        Map<String, Integer> bestRankByDevice = new LinkedHashMap<>();
        if (cards == null) return new ArrayList<>();

        for (EntityCard card : cards) {
            if (!isActionable(card)) continue;
            String deviceId = clean(card.deviceId());
            if (deviceId == null) {
                // BOOP Home deliberately shows devices, not loose HA entities/groups/helpers.
                continue;
            }
            int candidateRank = rank(card);
            EntityCard candidate = card;
            String deviceName = clean(card.deviceName());
            if (deviceName != null && !deviceName.equals(card.displayName())) {
                candidate = card.withDisplayName(deviceName);
            }
            Integer currentRank = bestRankByDevice.get(deviceId);
            if (currentRank == null || candidateRank > currentRank) {
                bestByDevice.put(deviceId, candidate);
                bestRankByDevice.put(deviceId, candidateRank);
            }
        }
        return new ArrayList<>(bestByDevice.values());
    }

    private static int rank(EntityCard card) {
        String domain = card.domain();
        if ("fan".equals(domain)) return 30;
        if ("light".equals(domain)) return 30;
        if ("switch".equals(domain)) return looksLikePowerSwitch(card) ? 20 : 10;
        return 0;
    }

    private static boolean fanPowerSupported(EntityCard card) {
        long features = card.supportedFeatures();
        // Older/unknown snapshots keep legacy behaviour. A known feature mask must
        // explicitly advertise both power operations on current Home Assistant.
        return features < 0 || (features & FAN_POWER_FEATURES) == FAN_POWER_FEATURES;
    }

    private static boolean looksLikePowerSwitch(EntityCard card) {
        String id = card.entityId().toLowerCase(java.util.Locale.ROOT);
        String name = card.displayName().toLowerCase(java.util.Locale.ROOT);
        return id.contains("power") || name.contains("power")
                || id.contains("on_off") || name.contains("on/off");
    }

    private static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
