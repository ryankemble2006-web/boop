package com.boop.alpha1;

import java.util.function.BooleanSupplier;

final class BoopCommandRouter {
    interface LocalProcessor {
        CommandOutcome process(String text);
    }

    interface AssistantProcessor {
        CommandOutcome ask(String text);
    }

    interface AssistantActivity {
        void onAssistantStarted();
        void onAssistantFinished();
    }

    private static final AssistantActivity NO_ASSISTANT_ACTIVITY = new AssistantActivity() {
        @Override public void onAssistantStarted() { }
        @Override public void onAssistantFinished() { }
    };

    private final LocalProcessor local;
    private final AssistantProcessor assistant;
    private final AssistantActivity assistantActivity;

    BoopCommandRouter(
            HomeAssistantClient local,
            HomeAssistantGeneralAssistantClient assistant) {
        this(local::process, assistant::ask, NO_ASSISTANT_ACTIVITY);
    }

    BoopCommandRouter(LocalProcessor local, AssistantProcessor assistant) {
        this(local, assistant, NO_ASSISTANT_ACTIVITY);
    }

    BoopCommandRouter(
            LocalProcessor local,
            AssistantProcessor assistant,
            AssistantActivity assistantActivity) {
        this.local = local;
        this.assistant = assistant;
        this.assistantActivity = assistantActivity;
    }

    CommandOutcome process(String text) {
        return process(text, true);
    }

    CommandOutcome process(String text, boolean useAssistant) {
        return process(text, () -> useAssistant);
    }

    CommandOutcome process(String text, BooleanSupplier useAssistant) {
        CommandOutcome localOutcome = local.process(text);
        if (localOutcome.status() != CommandOutcome.Status.NO_MATCH) {
            return localOutcome;
        }
        if (!useAssistant.getAsBoolean()) {
            return localOutcome;
        }

        assistantActivity.onAssistantStarted();
        try {
            return assistant.ask(text);
        } finally {
            assistantActivity.onAssistantFinished();
        }
    }
}
