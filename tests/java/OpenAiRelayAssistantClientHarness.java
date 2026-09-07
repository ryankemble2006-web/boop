package com.boop.alpha1;

import java.io.IOException;
import java.net.SocketTimeoutException;

final class BuildConfig {
    static final String BOOP_RELAY_URL = "";
    static final String BOOP_RELAY_TOKEN = "";
}

public final class OpenAiRelayAssistantClientHarness {
    private static final class FakeTransport implements OpenAiRelayAssistantClient.Transport {
        OpenAiRelayAssistantClient.RelayResponse response;
        IOException failure;
        int calls;
        String lastConversationId = "";

        @Override
        public OpenAiRelayAssistantClient.RelayResponse post(
                OpenAiRelayConfig config, String text, String conversationId) throws IOException {
            calls++;
            lastConversationId = conversationId == null ? "" : conversationId;
            if (failure != null) throw failure;
            return response;
        }
    }

    public static void main(String[] args) {
        missingConfigSkipsNetwork();
        successAndConversationContinuity();
        errorMappings();
        malformedSuccessFails();
        System.out.println("PASS: OpenAiRelayAssistantClientHarness");
    }

    private static void missingConfigSkipsNetwork() {
        FakeTransport fake = new FakeTransport();
        OpenAiRelayAssistantClient client = new OpenAiRelayAssistantClient(
                new OpenAiRelayConfig("", ""), fake);
        expect(client.ask("hello").status(), CommandOutcome.Status.ASSISTANT_SETUP_REQUIRED);
        expect(fake.calls, 0);
    }

    private static void successAndConversationContinuity() {
        FakeTransport fake = new FakeTransport();
        OpenAiRelayAssistantClient client = configured(fake);
        fake.response = new OpenAiRelayAssistantClient.RelayResponse(
                200, true, "first answer", "resp_1", "");
        CommandOutcome first = client.ask("first");
        expect(first.status(), CommandOutcome.Status.ASSISTANT_REPLY);
        expect(first.assistantSpeech(), "first answer");
        expect(fake.lastConversationId, "");

        fake.response = new OpenAiRelayAssistantClient.RelayResponse(
                200, true, "second answer", "resp_2", "");
        CommandOutcome second = client.ask("second");
        expect(second.status(), CommandOutcome.Status.ASSISTANT_REPLY);
        expect(fake.lastConversationId, "resp_1");
    }

    private static void errorMappings() {
        assertStatus(401, "", CommandOutcome.Status.ASSISTANT_AUTH_REQUIRED);
        assertStatus(403, "", CommandOutcome.Status.ASSISTANT_AUTH_REQUIRED);
        assertStatus(408, "", CommandOutcome.Status.ASSISTANT_TIMEOUT);
        assertStatus(429, "quota", CommandOutcome.Status.ASSISTANT_QUOTA);
        assertStatus(429, "rate_limit", CommandOutcome.Status.ASSISTANT_RATE_LIMIT);
        assertStatus(503, "service", CommandOutcome.Status.ASSISTANT_SERVICE);

        FakeTransport timeout = new FakeTransport();
        timeout.failure = new SocketTimeoutException("timeout");
        expect(configured(timeout).ask("hello").status(), CommandOutcome.Status.ASSISTANT_TIMEOUT);

        FakeTransport network = new FakeTransport();
        network.failure = new IOException("offline");
        expect(configured(network).ask("hello").status(), CommandOutcome.Status.ASSISTANT_SERVICE);
    }

    private static void malformedSuccessFails() {
        FakeTransport fake = new FakeTransport();
        fake.response = new OpenAiRelayAssistantClient.RelayResponse(200, true, "", "resp", "");
        expect(configured(fake).ask("hello").status(), CommandOutcome.Status.ASSISTANT_FAILED);
    }

    private static void assertStatus(int status, String error, CommandOutcome.Status expected) {
        FakeTransport fake = new FakeTransport();
        fake.response = new OpenAiRelayAssistantClient.RelayResponse(status, false, "", "", error);
        expect(configured(fake).ask("hello").status(), expected);
    }

    private static OpenAiRelayAssistantClient configured(FakeTransport fake) {
        return new OpenAiRelayAssistantClient(
                new OpenAiRelayConfig("https://relay.example.test", "relay-token"), fake);
    }

    private static void expect(Object actual, Object expected) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected " + expected + " but got " + actual);
        }
    }

    private static void expect(int actual, int expected) {
        if (actual != expected) {
            throw new AssertionError("expected " + expected + " but got " + actual);
        }
    }
}
