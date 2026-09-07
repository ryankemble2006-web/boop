package com.boop.alpha1;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

/** Local NO_MATCH fallback. No provider key, prompt logging or persistent transcript. */
final class OpenAiRelayAssistantClient implements AutoCloseable {
    private static final int MAX_TEXT_CHARS = 4000;
    private static final int MAX_REPLY_CHARS = 8000;
    private static final int MAX_RESPONSE_BYTES = 65536;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OpenAiRelayConfig config;
    private final OkHttpClient http;
    private String conversationId = "";
    private volatile Call activeCall;
    private volatile boolean closed;

    OpenAiRelayAssistantClient(OpenAiRelayConfig config) {
        this(config, new OkHttpClient());
    }

    OpenAiRelayAssistantClient(OpenAiRelayConfig config, OkHttpClient transport) {
        this.config = config;
        // Own the dispatcher/pool so closing a relay cannot cancel an HA connection.
        this.http = transport.newBuilder()
                .dispatcher(new Dispatcher()).connectionPool(new ConnectionPool())
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS)
                .followRedirects(false).followSslRedirects(false)
                .retryOnConnectionFailure(false).build();
    }

    synchronized CommandOutcome ask(String text) {
        if (closed) return CommandOutcome.assistantService();
        if (config == null || !config.configured()) return CommandOutcome.assistantSetupRequired();
        if (text == null || text.isBlank() || text.length() > MAX_TEXT_CHARS)
            return CommandOutcome.assistantFailed();
        try {
            JSONObject payload = new JSONObject().put("text", text);
            if (!conversationId.isEmpty()) payload.put("conversation_id", conversationId);
            Request request = new Request.Builder().url(config.url())
                    .header("Authorization", "Bearer " + config.token())
                    .header("Accept", "application/json")
                    .post(RequestBody.create(payload.toString(), JSON)).build();
            Call call = http.newCall(request);
            activeCall = call;
            if (closed) { call.cancel(); return CommandOutcome.assistantService(); }
            try (Response response = call.execute()) {
                int code = response.code();
                if (code == 401 || code == 403) return CommandOutcome.assistantAuthRequired();
                if (code == 408 || code == 504) return CommandOutcome.assistantTimeout();
                String raw = readBounded(response.body());
                JSONObject envelope = parseEnvelope(raw);
                if (code != 200) {
                    String error = envelope == null ? "" : envelope.optString("error", "");
                    if (code == 429) return "quota".equals(error)
                            ? CommandOutcome.assistantQuota() : CommandOutcome.assistantRateLimit();
                    if (code >= 500) return "offline".equals(error)
                            ? CommandOutcome.assistantUnreachable() : CommandOutcome.assistantService();
                    // A stale/invalid provider response ID may need a fresh conversation.
                    // Never auto-retry a request that might have incurred provider usage.
                    if (code == 400 && "bad_request".equals(error)) conversationId = "";
                    return CommandOutcome.assistantFailed();
                }
                if (envelope == null || !Boolean.TRUE.equals(envelope.opt("ok")))
                    return CommandOutcome.assistantFailed();
                Object reply = envelope.opt("text"), id = envelope.opt("conversation_id");
                if (!(reply instanceof String) || ((String) reply).isBlank()
                        || ((String) reply).length() > MAX_REPLY_CHARS
                        || !(id instanceof String) || !((String) id).matches("[A-Za-z0-9_-]{1,256}"))
                    return CommandOutcome.assistantFailed();
                conversationId = (String) id;
                return CommandOutcome.assistantReply(((String) reply).trim());
            }
        } catch (InterruptedIOException timeout) {
            return closed ? CommandOutcome.assistantService() : CommandOutcome.assistantTimeout();
        } catch (IOException offline) {
            return closed ? CommandOutcome.assistantService() : CommandOutcome.assistantUnreachable();
        } catch (JSONException | IllegalArgumentException malformed) {
            return CommandOutcome.assistantFailed();
        } finally {
            activeCall = null;
        }
    }

    private static JSONObject parseEnvelope(String raw) {
        if (raw == null) return null;
        try { return new JSONObject(raw); }
        catch (JSONException invalid) { return null; }
    }

    private static String readBounded(ResponseBody body) throws IOException {
        if (body == null || body.contentLength() > MAX_RESPONSE_BYTES) return null;
        if (body.source().request(MAX_RESPONSE_BYTES + 1L)) return null;
        return body.source().readUtf8();
    }

    @Override public void close() {
        closed = true;
        Call call = activeCall;
        if (call != null) call.cancel();
        http.dispatcher().cancelAll();
        http.connectionPool().evictAll();
        http.dispatcher().executorService().shutdown();
    }
}
