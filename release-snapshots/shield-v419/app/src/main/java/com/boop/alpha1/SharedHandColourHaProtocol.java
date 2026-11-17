package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/** Explicit HA helper identity; never derive an entity ID from its display name. */
final class SharedHandColourHaProtocol {
    private SharedHandColourHaProtocol() { }
    static JSONObject createBody() throws JSONException {
        return new JSONObject().put("name", SharedHandColourProtocol.NAME)
                .put("min", 0).put("max", 32).put("mode", "text")
                .put("pattern", SharedHandColourProtocol.MARKER);
    }
    static String findHelper(Object result) {
        if (!(result instanceof JSONArray)) throw new IllegalArgumentException("Invalid helper list");
        JSONArray list = (JSONArray) result;
        ArrayList<String> ids = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.optJSONObject(i);
            if (item != null && SharedHandColourProtocol.isOwnedHelper(
                    item.optString("pattern", ""), item.optInt("min", -1),
                    item.optInt("max", -1), item.optString("mode", ""), item.has("initial"))) {
                ids.add(item.optString("id", ""));
            }
        }
        return SharedHandColourProtocol.chooseHelper(ids);
    }
    static String findEntity(Object result, String helperId) {
        if (!(result instanceof JSONArray) || helperId == null) throw new IllegalArgumentException("Invalid registry");
        JSONArray list = (JSONArray) result;
        String entity = null;
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.optJSONObject(i);
            if (item == null || !"input_text".equals(item.optString("platform", ""))
                    || !helperId.equals(item.optString("unique_id", ""))) continue;
            String candidate = item.optString("entity_id", "");
            if (!candidate.matches("input_text\\.[a-z0-9_]+") || !item.isNull("disabled_by")) continue;
            if (entity != null) throw new IllegalArgumentException("Ambiguous colour entity");
            entity = candidate;
        }
        if (entity == null) throw new IllegalArgumentException("Colour helper is not available");
        return entity;
    }
    static Integer readHue(Object result, String entity) {
        if (!(result instanceof JSONArray)) throw new IllegalArgumentException("Invalid state list");
        JSONArray list = (JSONArray) result;
        String value = null;
        boolean found = false;
        for (int i = 0; i < list.length(); i++) {
            JSONObject state = list.optJSONObject(i);
            if (state == null || !entity.equals(state.optString("entity_id", ""))) continue;
            if (found) throw new IllegalArgumentException("Duplicate colour state");
            found = true; value = state.optString("state", "");
        }
        return SharedHandColourProtocol.decode(value);
    }
}
