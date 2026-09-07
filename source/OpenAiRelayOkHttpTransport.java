package com.boop.alpha1;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

final class OpenAiRelayOkHttpTransport implements OpenAiRelayAssistantClient.Transport {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public OpenAiRelayAssistantClient.RelayResponse post(
            OpenAiRelayConfig config,
            String text,
            String conversationId) throws IOException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("text", text);
        if (conversationId != null && !conversationId.isBlank()) {
            requestJson.put("conversation_id", conversationId);
        }

        Request request = new Request.Builder()
                .url(config.url)
                .header("Authorization", "Bearer " + config.token)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String raw = response.body() == null ? "" : response.body().string();
            JSONObject body = raw.isBlank() ? new JSONObject() : new JSONObject(raw);
            return new OpenAiRelayAssistantClient.RelayResponse(
                    response.code(),
                    body.optBoolean("ok", false),
                    body.optString("text", ""),
                    body.optString("conversation_id", ""),
                    body.optString("error", ""));
        }
    }
}
