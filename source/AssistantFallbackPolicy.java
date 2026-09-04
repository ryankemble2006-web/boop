package com.boop.alpha1;

final class AssistantFallbackPolicy {
    private AssistantFallbackPolicy() { }

    static boolean shouldAskAssistant(CommandOutcome.Status status) {
        return status == CommandOutcome.Status.NO_MATCH;
    }
}
