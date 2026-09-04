package com.boop.alpha1;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class HomeAssistantAssistantClient {
    private static final int TIMEOUT_MS = 5000;
    private static final long DISCOVERY_TIMEOUT_SECONDS = 6L;
    private static final int PIPELINE_LIST_MESSAGE_ID = 1;
    private static final int AGENT_LIST_MESSAGE_ID = 2;

    private final SecureTokenStore tokenStore;
    private final HomeAssistantAuth auth;
    private final OkHttpClient webSocketClient = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build();

    private String cachedAgentId = "";
    private String conversationId = "";

    HomeAssistantAssistantClient(SecureTokenStore tokenStore, HomeAssistantAuth auth) {
        this.tokenStore = tokenStore;
        this.auth = auth;
    }

    synchronized AssistantOutcome ask(String text) {
        String baseUrl = tokenStore.getBaseUrl();
        String deviceId = tokenStore.getHaDeviceId();
        if (baseUrl == null || baseUrl.isBlank() || deviceId == null || deviceId.isBlank()) {
            return AssistantOutcome.authRequired();
        }

        try {
            String accessToken = auth.freshAccessToken();

            if (cachedAgentId.isBlank()) {
                DiscoveryResult discovery = discoverAgent(baseUrl, accessToken);
                switch (discovery.status) {
                    case READY:
                        cachedAgentId = discovery.agentId;
                        break;
                    case NO_AGENT:
                        return AssistantOutcome.noAgent();
                    case AUTH_REQUIRED:
                        return AssistantOutcome.authRequired();
                    case UNREACHABLE:
                        return AssistantOutcome.unreachable();
                    case FAILED:
                    default:
                        return AssistantOutcome.failed();
                }
            }

            HomeAssistantAssistantReply reply = postConversation(
                    baseUrl,
                    accessToken,
                    text,
                    deviceId,
                    cachedAgentId,
                    conversationId);
            if (!reply.conversationId().isBlank()) {
                conversationId = reply.conversationId();
            }
            if (reply.speech().isBlank()) {
                return AssistantOutcome.failed();
            }
            return AssistantOutcome.reply(reply.speech());
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            return AssistantOutcome.authRequired();
        } catch (IOException e) {
            return AssistantOutcome.unreachable();
        } catch (Exception e) {
            return AssistantOutcome.failed();
        }
    }

    void close() {
        webSocketClient.dispatcher().executorService().shutdown();
        webSocketClient.connectionPool().evictAll();
    }

    private DiscoveryResult discoverAgent(String baseUrl, String accessToken) {
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicReference<DiscoveryResult> result = new AtomicReference<>(DiscoveryResult.unreachable());
        AtomicReference<WebSocket> socketRef = new AtomicReference<>();

        try {
            Request request = new Request.Builder()
                    .url(webSocketUrl(baseUrl))
                    .build();

            WebSocket socket = webSocketClient.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    try {
                        JSONObject message = new JSONObject(text);
                        String type = message.optString("type", "");

                        if ("auth_required".equals(type)) {
                            webSocket.send(new JSONObject()
                                    .put("type", "auth")
                                    .put("access_token", accessToken)
                                    .toString());
                            return;
                        }

                        if ("auth_ok".equals(type)) {
                            sendPipelineList(webSocket);
                            return;
                        }

                        if ("auth_invalid".equals(type)) {
                            finish(
                                    finished,
                                    result,
                                    done,
                                    DiscoveryResult.authRequired());
                            webSocket.close(1000, "auth rejected");
                            return;
                        }

                        if (!"result".equals(type)) {
                            return;
                        }

                        int messageId = message.optInt("id", -1);
                        if (messageId == PIPELINE_LIST_MESSAGE_ID) {
                            if (message.optBoolean("success", false)) {
                                Object rawResult = message.opt("result");
                                String selected = HomeAssistantPipelineSelector.selectConversationEngine(
                                        rawResult == null ? "{}" : rawResult.toString());
                                if (!selected.isBlank()) {
                                    finish(
                                            finished,
                                            result,
                                            done,
                                            DiscoveryResult.ready(selected));
                                    webSocket.close(1000, "pipeline selected");
                                    return;
                                }
                            }

                            sendAgentList(webSocket);
                            return;
                        }

                        if (messageId == AGENT_LIST_MESSAGE_ID) {
                            if (!message.optBoolean("success", false)) {
                                finish(finished, result, done, DiscoveryResult.failed());
                                webSocket.close(1000, "agent list failed");
                                return;
                            }

                            List<HomeAssistantAgentSelector.Agent> agents = parseAgents(message.opt("result"));
                            String selected = HomeAssistantAgentSelector.select(agents);
                            finish(
                                    finished,
                                    result,
                                    done,
                                    selected.isBlank()
                                            ? DiscoveryResult.noAgent()
                                            : DiscoveryResult.ready(selected));
                            webSocket.close(1000, "agent selected");
                        }
                    } catch (Exception e) {
                        finish(finished, result, done, DiscoveryResult.failed());
                        webSocket.close(1000, "invalid response");
                    }
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    finish(finished, result, done, DiscoveryResult.unreachable());
                }
            });
            socketRef.set(socket);

            if (!done.await(DISCOVERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                finish(finished, result, done, DiscoveryResult.unreachable());
            }
        } catch (Exception e) {
            finish(finished, result, done, DiscoveryResult.unreachable());
        } finally {
            WebSocket socket = socketRef.get();
            if (socket != null && finished.get()) {
                socket.cancel();
            }
        }

        return result.get();
    }

    private static void sendPipelineList(WebSocket webSocket) throws Exception {
        webSocket.send(new JSONObject()
                .put("id", PIPELINE_LIST_MESSAGE_ID)
                .put("type", "assist_pipeline/pipeline/list")
                .toString());
    }

    private static void sendAgentList(WebSocket webSocket) throws Exception {
        webSocket.send(new JSONObject()
                .put("id", AGENT_LIST_MESSAGE_ID)
                .put("type", "conversation/agent/list")
                .put("language", Locale.getDefault().toLanguageTag())
                .toString());
    }

    private static void finish(
            AtomicBoolean finished,
            AtomicReference<DiscoveryResult> result,
            CountDownLatch done,
            DiscoveryResult value) {
        if (finished.compareAndSet(false, true)) {
            result.set(value);
            done.countDown();
        }
    }

    private static List<HomeAssistantAgentSelector.Agent> parseAgents(Object rawResult) {
        JSONArray array = null;
        if (rawResult instanceof JSONArray) {
            array = (JSONArray) rawResult;
        } else if (rawResult instanceof JSONObject) {
            JSONObject object = (JSONObject) rawResult;
            array = object.optJSONArray("agents");
            if (array == null) {
                array = object.optJSONArray("conversation_agents");
            }
        }

        List<HomeAssistantAgentSelector.Agent> agents = new ArrayList<>();
        if (array == null) {
            return agents;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject agent = array.optJSONObject(i);
            if (agent == null) {
                continue;
            }
            agents.add(new HomeAssistantAgentSelector.Agent(
                    agent.optString("id", ""),
                    agent.optString("name", "")));
        }
        return agents;
    }

    private static HomeAssistantAssistantReply postConversation(
            String baseUrl,
            String accessToken,
            String text,
            String deviceId,
            String agentId,
            String conversationId) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + "/api/conversation/process").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject body = HomeAssistantConversationRequest.build(
                    text,
                    Locale.getDefault().toLanguageTag(),
                    deviceId,
                    agentId,
                    conversationId);
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int status = connection.getResponseCode();
            if (status == 401 || status == 403) {
                throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            }
            if (status < 200 || status >= 300) {
                throw new IOException("Home Assistant assistant HTTP " + status);
            }

            return HomeAssistantAssistantReplyParser.parse(readAll(connection.getInputStream()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String webSocketUrl(String baseUrl) {
        String clean = HomeAssistantAuthUrls.trim(baseUrl);
        if (clean.startsWith("https://")) {
            clean = "wss://" + clean.substring("https://".length());
        } else if (clean.startsWith("http://")) {
            clean = "ws://" + clean.substring("http://".length());
        }
        return clean + "/api/websocket";
    }

    private static String readAll(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private enum DiscoveryStatus {
        READY,
        NO_AGENT,
        AUTH_REQUIRED,
        UNREACHABLE,
        FAILED
    }

    private static final class DiscoveryResult {
        private final DiscoveryStatus status;
        private final String agentId;

        private DiscoveryResult(DiscoveryStatus status, String agentId) {
            this.status = status;
            this.agentId = agentId == null ? "" : agentId;
        }

        static DiscoveryResult ready(String agentId) {
            return new DiscoveryResult(DiscoveryStatus.READY, agentId);
        }

        static DiscoveryResult noAgent() {
            return new DiscoveryResult(DiscoveryStatus.NO_AGENT, "");
        }

        static DiscoveryResult authRequired() {
            return new DiscoveryResult(DiscoveryStatus.AUTH_REQUIRED, "");
        }

        static DiscoveryResult unreachable() {
            return new DiscoveryResult(DiscoveryStatus.UNREACHABLE, "");
        }

        static DiscoveryResult failed() {
            return new DiscoveryResult(DiscoveryStatus.FAILED, "");
        }
    }
}
