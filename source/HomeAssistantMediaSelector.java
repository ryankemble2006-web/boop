package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HomeAssistantMediaSelector {
    private static final int FEATURE_PAUSE = 1;
    private static final int FEATURE_PREVIOUS_TRACK = 16;
    private static final int FEATURE_NEXT_TRACK = 32;
    private static final int FEATURE_VOLUME_STEP = 1024;
    private static final int FEATURE_PLAY = 16384;

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
        return rank(areaEntities, states, null);
    }

    static List<Candidate> rank(
            JSONArray areaEntities,
            JSONArray states,
            MediaCommand command) {
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
            if (command != null && (!supports(command, attrs)
                    || !stateFitsCommand(command, stateName))) {
                continue;
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

    private static boolean supports(MediaCommand command, JSONObject attrs) {
        int supportedFeatures = attrs.optInt("supported_features", 0);
        int required;
        switch (command) {
            case PAUSE:
                required = FEATURE_PAUSE;
                break;
            case RESUME:
                required = FEATURE_PLAY;
                break;
            case NEXT:
                required = FEATURE_NEXT_TRACK;
                break;
            case PREVIOUS:
                required = FEATURE_PREVIOUS_TRACK;
                break;
            case VOLUME_UP:
            case VOLUME_DOWN:
                required = FEATURE_VOLUME_STEP;
                break;
            default:
                return false;
        }
        return (supportedFeatures & required) != 0;
    }

    private static boolean stateFitsCommand(MediaCommand command, String state) {
        switch (command) {
            case PAUSE:
                return "playing".equals(state) || "buffering".equals(state);
            case RESUME:
                return "paused".equals(state);
            case NEXT:
            case PREVIOUS:
                return "playing".equals(state)
                        || "paused".equals(state)
                        || "buffering".equals(state);
            case VOLUME_UP:
            case VOLUME_DOWN:
                return true;
            default:
                return false;
        }
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
