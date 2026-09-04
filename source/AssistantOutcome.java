package com.boop.alpha1;

final class AssistantOutcome {
    enum Status {
        REPLY,
        NO_AGENT,
        UNREACHABLE,
        AUTH_REQUIRED,
        FAILED
    }

    private final Status status;
    private final String speech;

    private AssistantOutcome(Status status, String speech) {
        this.status = status;
        this.speech = speech == null ? "" : speech;
    }

    static AssistantOutcome reply(String speech) {
        return new AssistantOutcome(Status.REPLY, speech);
    }

    static AssistantOutcome noAgent() {
        return new AssistantOutcome(Status.NO_AGENT, "");
    }

    static AssistantOutcome unreachable() {
        return new AssistantOutcome(Status.UNREACHABLE, "");
    }

    static AssistantOutcome authRequired() {
        return new AssistantOutcome(Status.AUTH_REQUIRED, "");
    }

    static AssistantOutcome failed() {
        return new AssistantOutcome(Status.FAILED, "");
    }

    Status status() { return status; }
    String speech() { return speech; }
}
