package com.boop.shieldoverlay;

import org.json.JSONObject;

final class HaJsonStrings {
    private HaJsonStrings() { }

    static String optional(JSONObject object, String key) {
        if (object == null || key == null || object.isNull(key)) return null;
        String value = object.optString(key, null);
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
