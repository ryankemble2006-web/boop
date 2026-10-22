package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HomeAssistantLightColourSelector {
    private static final Set<String> COLOUR_MODES = Set.of(
            "hs", "xy", "rgb", "rgbw", "rgbww");

    private HomeAssistantLightColourSelector() { }

    static List<String> select(JSONArray areaEntities, JSONArray states) {
        Set<String> inArea = new HashSet<>();
        for (int i = 0; i < areaEntities.length(); i++) {
            String entityId = areaEntities.optString(i, "");
            if (entityId.startsWith("light.")) {
                inArea.add(entityId);
            }
        }

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < states.length(); i++) {
            JSONObject state = states.optJSONObject(i);
            if (state == null) {
                continue;
            }
            String entityId = state.optString("entity_id", "");
            if (!inArea.contains(entityId) || !"on".equalsIgnoreCase(state.optString("state", ""))) {
                continue;
            }

            JSONObject attrs = state.optJSONObject("attributes");
            JSONArray modes = attrs == null ? null : attrs.optJSONArray("supported_color_modes");
            if (!supportsColour(modes)) {
                continue;
            }
            selected.add(entityId);
        }

        selected.sort(String::compareTo);
        return selected;
    }

    private static boolean supportsColour(JSONArray modes) {
        if (modes == null) {
            return false;
        }
        for (int i = 0; i < modes.length(); i++) {
            if (COLOUR_MODES.contains(modes.optString(i, "").toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
