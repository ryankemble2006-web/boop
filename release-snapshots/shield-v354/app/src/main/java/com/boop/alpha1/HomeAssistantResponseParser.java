package com.boop.alpha1;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class HomeAssistantResponseParser {
    private HomeAssistantResponseParser() { }

    static HomeAssistantResponse parse(String json) {
        final JSONObject root;
        try {
            root = new JSONObject(json);
        } catch (JSONException e) {
            return new HomeAssistantResponse(
                    HomeAssistantResponse.Kind.UNKNOWN_ERROR,
                    List.of(), List.of(), "");
        }

        JSONObject response = root.optJSONObject("response");
        if (response == null) {
            return new HomeAssistantResponse(
                    HomeAssistantResponse.Kind.UNKNOWN_ERROR,
                    List.of(), List.of(), "");
        }

        String responseType = response.optString("response_type", "");
        JSONObject data = response.optJSONObject("data");
        List<HomeAssistantResponse.Target> success = parseTargets(data, "success");
        List<HomeAssistantResponse.Target> failed = parseTargets(data, "failed");
        String speech = parseSpeech(response);

        if ("action_done".equals(responseType)) {
            return new HomeAssistantResponse(
                    HomeAssistantResponse.Kind.ACTION_DONE, success, failed, speech);
        }
        if ("query_answer".equals(responseType)) {
            return new HomeAssistantResponse(
                    HomeAssistantResponse.Kind.QUERY_ANSWER, success, failed, speech);
        }
        if ("error".equals(responseType)) {
            String code = data == null ? "" : data.optString("code", "");
            HomeAssistantResponse.Kind kind;
            switch (code) {
                case "no_intent_match":
                    kind = HomeAssistantResponse.Kind.NO_INTENT_MATCH;
                    break;
                case "no_valid_targets":
                    kind = HomeAssistantResponse.Kind.NO_VALID_TARGETS;
                    break;
                case "failed_to_handle":
                    kind = HomeAssistantResponse.Kind.FAILED_TO_HANDLE;
                    break;
                default:
                    kind = HomeAssistantResponse.Kind.UNKNOWN_ERROR;
                    break;
            }
            return new HomeAssistantResponse(kind, success, failed, speech);
        }

        return new HomeAssistantResponse(
                HomeAssistantResponse.Kind.UNKNOWN_ERROR, success, failed, speech);
    }

    private static List<HomeAssistantResponse.Target> parseTargets(JSONObject data, String key) {
        if (data == null) {
            return List.of();
        }
        JSONArray array = data.optJSONArray(key);
        if (array == null) {
            return List.of();
        }
        List<HomeAssistantResponse.Target> targets = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) {
                targets.add(new HomeAssistantResponse.Target(
                        item.optString("name", ""),
                        item.optString("type", ""),
                        item.optString("id", "")));
            }
        }
        return targets;
    }

    private static String parseSpeech(JSONObject response) {
        JSONObject speech = response.optJSONObject("speech");
        if (speech == null) {
            return "";
        }
        JSONObject plain = speech.optJSONObject("plain");
        return plain == null ? "" : plain.optString("speech", "");
    }
}
