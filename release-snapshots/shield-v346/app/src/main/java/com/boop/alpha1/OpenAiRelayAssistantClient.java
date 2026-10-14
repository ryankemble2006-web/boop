package com.boop.alpha1;

import java.io.IOException;
import java.net.SocketTimeoutException;

final class OpenAiRelayAssistantClient {
    interface Transport {
        RelayResponse post(OpenAiRelayConfig config, String text, String conversationId) throws IOException;
    }

    static final class RelayResponse {
        final int httpStatus;
        final boolean ok;
        final String text;
        final String conversationId;
        final String error;

        RelayResponse(int httpStatus, boolean ok, String text, String conversationId, String error) {
            this.httpStatus = httpStatus;
            this.ok = ok;
            this.text = text == null ? "" : text.trim();
            this.conversationId = conversationId == null ? "" : conversationId.trim();
            this.error = error == null ? "" : error.trim();
        }
    }

    private final OpenAiRelayConfig config;
    private final Transport transport;
    private String conversationId = "";

    OpenAiRelayAssistantClient(OpenAiRelayConfig config, Transport transport) {
        this.config = config;
        this.transport = transport;
    }

    CommandOutcome ask(String text) {
        if (config == null || !config.configured()) {
            return CommandOutcome.assistantSetupRequired();
        }
        if (text == null || text.trim().isEmpty()) {
            return CommandOutcome.assistantFailed();
        }

        final RelayResponse response;
        try {
            response = transport.post(config, text.trim(), conversationId);
        } catch (SocketTimeoutException timeout) {
            return CommandOutcome.assistantTimeout();
        } catch (IOException unreachable) {
            return CommandOutcome.assistantService();
        } catch (RuntimeException malformed) {
            return CommandOutcome.assistantFailed();
        }

        if (response == null) {
            return CommandOutcome.assistantFailed();
        }
        if (response.httpStatus == 401 || response.httpStatus == 403 || "auth".equals(response.error)) {
            return CommandOutcome.assistantAuthRequired();
        }
        if (response.httpStatus == 408 || "timeout".equals(response.error) || "offline".equals(response.error)) {
            return CommandOutcome.assistantTimeout();
        }
        if ("quota".equals(response.error)) {
            return CommandOutcome.assistantQuota();
        }
        if ("rate_limit".equals(response.error)) {
            return CommandOutcome.assistantRateLimit();
        }
        if (response.httpStatus == 429) {
            return CommandOutcome.assistantRateLimit();
        }
        if ("service".equals(response.error) || response.httpStatus >= 500) {
            return CommandOutcome.assistantService();
        }
        if (response.httpStatus != 200 || !response.ok || response.text.isEmpty()) {
            return CommandOutcome.assistantFailed();
        }

        if (!response.conversationId.isEmpty()) {
            conversationId = response.conversationId;
        }
        return CommandOutcome.assistantReply(response.text);
    }
}
