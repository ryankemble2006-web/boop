package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HomeAssistantMediaSelector {
    private HomeAssistantMediaSelector() { }

    static final class Candidate {
        private final String entityId;
        private final String name;
        private final int score;

        Candidate(String entityId, String name, int score) {
            this.entityId = entityId;
            this.name = name;
            this.score = score;
        }

        String entityId() { return entityId; }
        String name() { return name; }
        int score() { return score; }
    }

    static List<Candidate> rank(JSONArray areaEntities, JSONArray states) {
        Set<String> inArea = new HashSet<>();
        for (int i = 0; i < areaEntities.length(); i++) {
            String entityId = areaEntities.optString(i, "");
            if (entityId.startsWith("media_player.")) {
                inArea.add(entityId);
            }
        }

        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < states.length(); i++) {
            JSONObject state = states.optJSONObject(i);
            if (state == null) {
                continue;
            }
            String entityId = state.optString("entity_id", "");
            if (!inArea.contains(entityId)) {
                continue;
            }

            String stateName = state.optString("state", "").toLowerCase();
            if (stateName.isEmpty()
                    || "unavailable".equals(stateName)
                    || "unknown".equals(stateName)
                    || "off".equals(stateName)) {
                continue;
            }

            JSONObject attrs = state.optJSONObject("attributes");
            if (attrs == null) {
                attrs = new JSONObject();
            }
            int score = score(stateName, attrs);
            if (score <= 0) {
                continue;
            }

            String name = attrs.optString("friendly_name", entityId);
            candidates.add(new Candidate(entityId, name, score));
        }

        candidates.sort(Comparator
                .comparingInt(Candidate::score).reversed()
                .thenComparing(Candidate::entityId));
        return candidates;
    }

    private static int score(String state, JSONObject attrs) {
        int score = 0;
        switch (state) {
            case "playing": score += 1000; break;
            case "buffering": score += 900; break;
            case "paused": score += 650; break;
            case "idle": score += 120; break;
            case "on": score += 100; break;
            default: score += 25; break;
        }

        if (hasText(attrs, "media_title")) score += 500;
        if (hasText(attrs, "media_content_id")) score += 350;
        if (hasText(attrs, "app_id")) score += 600;
        if (hasText(attrs, "app_name")) score += 300;
        if (hasText(attrs, "media_artist")) score += 150;
        if (attrs.optDouble("media_position", 0.0) > 0.0) score += 100;
        return score;
    }

    private static boolean hasText(JSONObject attrs, String key) {
        String value = attrs.optString(key, "");
        return value != null && !value.trim().isEmpty();
    }
}
