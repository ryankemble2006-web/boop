package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

final class HomeAssistantPipelineSelector {
    private HomeAssistantPipelineSelector() { }

    static String selectConversationEngine(String json) {
        try {
            JSONObject root = new JSONObject(json == null ? "{}" : json);
            JSONArray pipelines = root.optJSONArray("pipelines");
            if (pipelines == null) {
                return "";
            }

            String preferredPipeline = root.optString("preferred_pipeline", "").trim();
            if (!preferredPipeline.isEmpty()) {
                for (int i = 0; i < pipelines.length(); i++) {
                    JSONObject pipeline = pipelines.optJSONObject(i);
                    if (pipeline == null
                            || !preferredPipeline.equals(pipeline.optString("id", "").trim())) {
                        continue;
                    }
                    String engine = pipeline.optString("conversation_engine", "").trim();
                    if (isAssistantEngine(engine)) {
                        return engine;
                    }
                    break;
                }
            }

            for (int i = 0; i < pipelines.length(); i++) {
                JSONObject pipeline = pipelines.optJSONObject(i);
                if (pipeline == null) {
                    continue;
                }
                String engine = pipeline.optString("conversation_engine", "").trim();
                if (isAssistantEngine(engine)) {
                    return engine;
                }
            }
        } catch (Exception ignored) {
            return "";
        }
        return "";
    }

    private static boolean isAssistantEngine(String engine) {
        return engine != null
                && !engine.isBlank()
                && !"home_assistant".equalsIgnoreCase(engine)
                && !"conversation.home_assistant".equalsIgnoreCase(engine);
    }
}
