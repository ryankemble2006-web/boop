package com.boop.alpha1;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/** Match the response belonging to one command, not another client's latest ADB output. */
final class AdbCommandReceipt {
    private AdbCommandReceipt() { }
    static String fromService(String raw, String entity, String nonce) throws IOException {
        final JSONArray states;
        try { states = new JSONArray(raw == null ? "[]" : raw); }
        catch (JSONException legacyResponse) { return null; }
        String receipt = null;
        for (int i = 0; i < states.length(); i++) {
            JSONObject state = states.optJSONObject(i);
            if (state == null || !entity.equals(state.optString("entity_id"))) continue;
            String candidate = fromState(state, entity, nonce);
            if (candidate == null) continue;
            if (receipt != null && !receipt.equals(candidate)) throw new IOException("Conflicting ADB receipts");
            receipt = candidate;
        }
        return receipt;
    }
    static String fromState(JSONObject state, String entity, String nonce) {
        if (state == null || nonce == null || nonce.isEmpty()
                || (state.has("entity_id") && !entity.equals(state.optString("entity_id")))) return null;
        JSONObject attributes = state.optJSONObject("attributes");
        Object value = attributes == null ? null : attributes.opt("adb_response");
        if (!(value instanceof String)) return null;
        String[] lines = ((String) value).split("\r?\n", 2);
        if (!nonce.equals(lines[0])) return null;
        return lines.length == 1 ? "" : lines[1].trim();
    }
}
