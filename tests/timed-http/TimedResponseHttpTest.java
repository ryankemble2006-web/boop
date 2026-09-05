package com.boop.alpha1;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;

// Production HTTP client, request builder, parser and reply; Android storage/auth
// and unrelated device transports are substituted below. No real house is used.
public final class TimedResponseHttpTest {
    public static void main(String[] args) throws Exception {
        AtomicReference<String> response = new AtomicReference<>();
        AtomicInteger status = new AtomicInteger(200), calls = new AtomicInteger();
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/conversation/process", exchange -> {
            calls.incrementAndGet();
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            HomeAssistantClient client = new HomeAssistantClient(
                    new SecureTokenStore("http://127.0.0.1:" + server.getAddress().getPort()),
                    new HomeAssistantAuth(), "Living Room");
            String emptySuccess = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[],\"failed\":[]},\"speech\":{\"plain\":{\"speech\":\"Command scheduled\"}}}}";
            response.set(emptySuccess);
            CommandOutcome scheduled = client.processTimed("turn off lights in 2 minutes");
            check(CommandOutcome.Status.SUCCESS, scheduled.status(), "scheduled action without device targets");
            check("Done.", LocalReply.forOutcome(scheduled), "spoken scheduling acknowledgement");
            JSONObject request = new JSONObject(requestBody.get());
            check("turn off lights in 2 minutes", request.getString("text"), "delay preserved");
            check("test-device", request.getString("device_id"), "device context preserved");
            check(1, calls.get(), "accepted request is not retried");

            String[] fixtures = {
                "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[{\"name\":\"Lights\",\"type\":\"area\"}],\"failed\":[]}}}",
                "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[],\"failed\":[{\"name\":\"Lights\",\"type\":\"area\"}]}}}",
                "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[{\"name\":\"One\",\"type\":\"area\"}],\"failed\":[{\"name\":\"Other\",\"type\":\"area\"}]}}}",
                "{\"response\":{\"response_type\":\"error\",\"data\":{\"code\":\"failed_to_handle\"}}}",
                "{\"response\":{\"response_type\":\"error\",\"data\":{\"code\":\"no_intent_match\"}}}",
                "{\"response\":{\"response_type\":\"query_answer\"}}",
                "not json"
            };
            CommandOutcome.Status[] expected = {CommandOutcome.Status.SUCCESS,
                CommandOutcome.Status.FAILED, CommandOutcome.Status.FAILED,
                CommandOutcome.Status.FAILED, CommandOutcome.Status.NO_MATCH,
                CommandOutcome.Status.FAILED, CommandOutcome.Status.FAILED};
            for (int i = 0; i < fixtures.length; i++) {
                response.set(fixtures[i]);
                check(expected[i], client.processTimed("test").status(), "response fixture " + i);
            }
            status.set(401);
            check(CommandOutcome.Status.AUTH_REQUIRED, client.processTimed("test").status(), "expired authorization");
            status.set(503);
            check(CommandOutcome.Status.UNREACHABLE, client.processTimed("test").status(), "server unavailable");
            status.set(200);
            response.set(emptySuccess);
            check(CommandOutcome.Status.FAILED, client.process("turn off lights").status(), "immediate command policy unchanged");
            System.out.println("PASS: scheduling acknowledgement, HTTP request preservation and 10 response/control cases");
        } finally { server.stop(0); }
    }
    private static void check(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) throw new AssertionError(label + ": expected " + expected + ", got " + actual);
    }
}

final class SecureTokenStore {
    private final String base;
    SecureTokenStore(String base) { this.base = base; }
    String getBaseUrl() { return base; }
    String getHaDeviceId() { return "test-device"; }
}
final class HomeAssistantAuth {
    String freshAccessToken() { return "local-test-token"; }
    static final class AuthRejectedException extends Exception {
        AuthRejectedException(String message) { super(message); }
    }
}
final class HomeAssistantLightColourClient {
    HomeAssistantLightColourClient(String area) { }
    CommandOutcome setColour(String base, String token, String colour) { throw new AssertionError("Unexpected colour execution"); }
}
final class HomeAssistantDirectMediaClient {
    HomeAssistantDirectMediaClient(String area) { }
    CommandOutcome processIfMedia(String base, String token, String text) { return null; }
}
