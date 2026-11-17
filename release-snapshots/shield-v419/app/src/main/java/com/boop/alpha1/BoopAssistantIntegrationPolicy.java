package com.boop.alpha1;

final class BoopAssistantIntegrationPolicy {
    enum Choice { USE_BOOP, KEEP_CURRENT }

    private final Choice choice;

    BoopAssistantIntegrationPolicy(Choice choice) {
        this.choice = choice;
    }

    boolean needsFirstRunChoice() {
        return choice == null;
    }

    BoopAssistantIntegrationPolicy withChoice(Choice next) {
        return new BoopAssistantIntegrationPolicy(next);
    }

    boolean shouldRequestAssistantRole(boolean alreadyHolder) {
        return choice == Choice.USE_BOOP && !alreadyHolder;
    }
}
