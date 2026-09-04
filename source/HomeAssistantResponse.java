package com.boop.alpha1;

import java.util.List;

final class HomeAssistantResponse {
    enum Kind {
        ACTION_DONE,
        QUERY_ANSWER,
        NO_INTENT_MATCH,
        NO_VALID_TARGETS,
        FAILED_TO_HANDLE,
        UNKNOWN_ERROR
    }

    static final class Target {
        private final String name;
        private final String type;
        private final String id;

        Target(String name, String type, String id) {
            this.name = name == null ? "" : name;
            this.type = type == null ? "" : type;
            this.id = id == null ? "" : id;
        }

        String name() { return name; }
        String type() { return type; }
        String id() { return id; }
    }

    private final Kind kind;
    private final List<Target> successTargets;
    private final List<Target> failedTargets;
    private final String speech;

    HomeAssistantResponse(Kind kind, List<Target> successTargets, List<Target> failedTargets, String speech) {
        this.kind = kind;
        this.successTargets = List.copyOf(successTargets);
        this.failedTargets = List.copyOf(failedTargets);
        this.speech = speech == null ? "" : speech;
    }

    Kind kind() { return kind; }
    List<Target> successTargets() { return successTargets; }
    List<Target> failedTargets() { return failedTargets; }
    String speech() { return speech; }
}
