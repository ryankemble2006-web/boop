package com.boop.shieldoverlay;

import org.json.JSONArray;
import org.json.JSONObject;

/** HA's compact registry uses a keyed category table, not necessarily an array. */
final class HaEntityCategory {
    private HaEntityCategory() { }

    static String resolve(Object reference, Object categories) {
        if (reference == null || reference == JSONObject.NULL) return null;
        if (reference instanceof String) {
            String value = ((String) reference).trim();
            return value.isEmpty() ? null : value;
        }
        if (reference instanceof Number) {
            int index = ((Number) reference).intValue();
            Object value = null;
            if (index >= 0 && categories instanceof JSONObject) {
                value = ((JSONObject) categories).opt(Integer.toString(index));
            } else if (index >= 0 && categories instanceof JSONArray) {
                value = ((JSONArray) categories).opt(index);
            }
            if (value instanceof String && !((String) value).trim().isEmpty()) {
                return ((String) value).trim();
            }
        }
        // Missing or unfamiliar metadata must not expose maintenance controls as devices.
        return "unknown";
    }
}
