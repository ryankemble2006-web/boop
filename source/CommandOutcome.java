package com.boop.alpha1;

final class CommandOutcome {
    enum Status {
        SUCCESS,
        TARGET_OFFLINE,
        NO_MATCH,
        NO_TARGET,
        FAILED,
        UNREACHABLE,
        AUTH_REQUIRED,
        ASSISTANT_REPLY,
        ASSISTANT_NO_AGENT,
        ASSISTANT_UNREACHABLE,
        ASSISTANT_FAILED
    }

    private final Status status;
    private final String targetName;
    private final String area;
    private final String assistantSpeech;

    private CommandOutcome(Status status, String targetName, String area) {
        this(status, targetName, area, "");
    }

    private CommandOutcome(
            Status status,
            String targetName,
            String area,
            String assistantSpeech) {
        this.status = status;
        this.targetName = targetName == null ? "" : targetName;
        this.area = area == null ? "" : area;
        this.assistantSpeech = assistantSpeech == null ? "" : assistantSpeech;
    }

    static CommandOutcome success(String targetName) {
        return new CommandOutcome(Status.SUCCESS, targetName, "");
    }

    static CommandOutcome targetOffline(String targetName, String area) {
        return new CommandOutcome(Status.TARGET_OFFLINE, targetName, area);
    }

    static CommandOutcome noMatch() {
        return new CommandOutcome(Status.NO_MATCH, "", "");
    }

    static CommandOutcome noTarget() {
        return new CommandOutcome(Status.NO_TARGET, "", "");
    }

    static CommandOutcome failed() {
        return new CommandOutcome(Status.FAILED, "", "");
    }

    static CommandOutcome unreachable() {
        return new CommandOutcome(Status.UNREACHABLE, "", "");
    }

    static CommandOutcome authRequired() {
        return new CommandOutcome(Status.AUTH_REQUIRED, "", "");
    }

    static CommandOutcome assistantReply(String speech) {
        return new CommandOutcome(Status.ASSISTANT_REPLY, "", "", speech);
    }

    static CommandOutcome assistantNoAgent() {
        return new CommandOutcome(Status.ASSISTANT_NO_AGENT, "", "");
    }

    static CommandOutcome assistantUnreachable() {
        return new CommandOutcome(Status.ASSISTANT_UNREACHABLE, "", "");
    }

    static CommandOutcome assistantFailed() {
        return new CommandOutcome(Status.ASSISTANT_FAILED, "", "");
    }

    Status status() { return status; }
    String targetName() { return targetName; }
    String area() { return area; }
    String assistantSpeech() { return assistantSpeech; }
}
