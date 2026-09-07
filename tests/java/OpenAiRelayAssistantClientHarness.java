package com.boop.alpha1;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;
import okio.Buffer;
import org.json.JSONObject;

/** Exercises the real client and JSON parser; only the network boundary is replaced. */
public final class OpenAiRelayAssistantClientHarness {
    private static final String TOKEN = "test-only-relay-token";
    private static final OpenAiRelayConfig CONFIG = new OpenAiRelayConfig("https://example.invalid/chat", TOKEN);
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static void status(CommandOutcome.Status expected, CommandOutcome actual) {
        check(actual.status() == expected, "Expected " + expected + ", got " + actual.status());
    }
    private static Response response(Request r, int code, String body) {
        return new Response.Builder().request(r).protocol(Protocol.HTTP_1_1).code(code)
                .message("test").body(ResponseBody.create(body, MediaType.get("application/json"))).build();
    }
    private static OpenAiRelayAssistantClient client(int code, String body) {
        return new OpenAiRelayAssistantClient(CONFIG, new OkHttpClient.Builder()
                .addInterceptor(chain -> response(chain.request(), code, body)).build());
    }
    public static void main(String[] args) throws Exception {
        List<Request> requests = new ArrayList<>();
        OkHttpClient http = new OkHttpClient.Builder().addInterceptor(chain -> {
            requests.add(chain.request());
            return response(chain.request(), 200, "{\"ok\":true,\"text\":\"Hello there.\",\"conversation_id\":\"resp_test_1\"}");
        }).build();
        try (OpenAiRelayAssistantClient c = new OpenAiRelayAssistantClient(new OpenAiRelayConfig("", ""), http)) {
            status(CommandOutcome.Status.ASSISTANT_SETUP_REQUIRED, c.ask("hello"));
            check(requests.isEmpty(), "missing setup made a network request");
        }
        try (OpenAiRelayAssistantClient c = new OpenAiRelayAssistantClient(CONFIG, http)) {
            CommandOutcome first = c.ask("first question");
            status(CommandOutcome.Status.ASSISTANT_REPLY, first);
            check("Hello there.".equals(first.assistantSpeech()), "success text lost");
            status(CommandOutcome.Status.ASSISTANT_REPLY, c.ask("follow up"));
            check(requests.size() == 2, "request repeated or omitted");
            for (Request r : requests) {
                check("POST".equals(r.method()), "must POST");
                check(("Bearer " + TOKEN).equals(r.header("Authorization")), "relay bearer missing");
                check(r.url().isHttps(), "transport not HTTPS");
                check(!r.url().toString().contains(TOKEN), "token in URL");
            }
            Buffer a = new Buffer(); requests.get(0).body().writeTo(a);
            Buffer b = new Buffer(); requests.get(1).body().writeTo(b);
            JSONObject initial = new JSONObject(a.readUtf8()), next = new JSONObject(b.readUtf8());
            check(initial.getString("text").equals("first question"), "question changed");
            check(!initial.has("conversation_id"), "initial conversation should be fresh");
            check(next.getString("conversation_id").equals("resp_test_1"), "conversation not continued");
            check(next.getString("text").equals("follow up"), "follow-up changed");
            int count = requests.size();
            status(CommandOutcome.Status.ASSISTANT_FAILED, c.ask(" "));
            status(CommandOutcome.Status.ASSISTANT_FAILED, c.ask("x".repeat(4001)));
            check(requests.size() == count, "invalid input sent upstream");
        }
        int[] codes = {401,403,408,429,429,500,502,503,504,400};
        String[] errors = {"auth","auth","timeout","quota","rate_limit","service","service","service","timeout","bad_request"};
        CommandOutcome.Status[] expected = {
            CommandOutcome.Status.ASSISTANT_AUTH_REQUIRED,CommandOutcome.Status.ASSISTANT_AUTH_REQUIRED,
            CommandOutcome.Status.ASSISTANT_TIMEOUT,CommandOutcome.Status.ASSISTANT_QUOTA,
            CommandOutcome.Status.ASSISTANT_RATE_LIMIT,CommandOutcome.Status.ASSISTANT_SERVICE,
            CommandOutcome.Status.ASSISTANT_SERVICE,CommandOutcome.Status.ASSISTANT_SERVICE,
            CommandOutcome.Status.ASSISTANT_TIMEOUT,CommandOutcome.Status.ASSISTANT_FAILED};
        for (int i=0;i<codes.length;i++) {
            try (OpenAiRelayAssistantClient c = client(codes[i], "{\"ok\":false,\"error\":\""+errors[i]+"\"}")) {
                status(expected[i], c.ask("question"));
            }
        }
        for (int code : new int[]{401,403,429,500,502}) {
            try (OpenAiRelayAssistantClient c = client(code, "not JSON")) {
                CommandOutcome.Status e = code==429 ? CommandOutcome.Status.ASSISTANT_RATE_LIMIT
                    : code>=500 ? CommandOutcome.Status.ASSISTANT_SERVICE : CommandOutcome.Status.ASSISTANT_AUTH_REQUIRED;
                status(e, c.ask("question"));
            }
        }
        String[] invalid = {"{}", "[]", "garbage", "{\"ok\":true,\"text\":\"\",\"conversation_id\":\"resp_1\"}",
            "{\"ok\":\"true\",\"text\":\"hello\",\"conversation_id\":\"resp_1\"}",
            "{\"ok\":true,\"text\":42,\"conversation_id\":\"resp_1\"}",
            "{\"ok\":true,\"text\":\"hello\",\"conversation_id\":42}",
            "{\"ok\":true,\"text\":\"hello\",\"conversation_id\":\"bad id\"}"};
        for (String value : invalid) {
            try (OpenAiRelayAssistantClient c=client(200,value)) { status(CommandOutcome.Status.ASSISTANT_FAILED,c.ask("question")); }
        }
        try (OpenAiRelayAssistantClient c=client(200, "x".repeat(65537))) {
            status(CommandOutcome.Status.ASSISTANT_FAILED, c.ask("question"));
        }
        for (boolean timeout : new boolean[]{false,true}) {
            OkHttpClient broken = new OkHttpClient.Builder().addInterceptor(chain -> {
                if(timeout) throw new InterruptedIOException("test timeout");
                throw new IOException("test offline");
            }).build();
            try(OpenAiRelayAssistantClient c=new OpenAiRelayAssistantClient(CONFIG,broken)) {
                status(timeout?CommandOutcome.Status.ASSISTANT_TIMEOUT:CommandOutcome.Status.ASSISTANT_UNREACHABLE,c.ask("question"));
            }
        }
        OpenAiRelayAssistantClient closed = client(200, "{}"); closed.close();
        status(CommandOutcome.Status.ASSISTANT_SERVICE, closed.ask("question"));
        System.out.println("BOOP_RELAY_PROTOCOL_PASS");
    }
}
