package com.boop.alpha1;

final class CommandOutcome {
    enum Status {
        SUCCESS,
        TARGET_OFFLINE,
        NO_MATCH,
        NO_TARGET,
        FAILED,
        UNREACHABLE,
        AUTH_REQUIRED
    }

    private final Status status;
    private final String targetName;
    private final String area;

    private CommandOutcome(Status status, String targetName, String area) {
        this.status = status;
        this.targetName = targetName == null ? "" : targetName;
        this.area = area == null ? "" : area;
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

    Status status() { return status; }
    String targetName() { return targetName; }
    String area() { return area; }
}
