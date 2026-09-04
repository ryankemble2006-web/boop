package com.boop.alpha1;

final class BoopCommandRouter {
    interface LocalProcessor {
        CommandOutcome process(String text);
    }

    interface AssistantProcessor {
        CommandOutcome ask(String text);
    }

    private final LocalProcessor local;
    private final AssistantProcessor assistant;

    BoopCommandRouter(
            HomeAssistantClient local,
            HomeAssistantGeneralAssistantClient assistant) {
        this(local::process, assistant::ask);
    }

    BoopCommandRouter(LocalProcessor local, AssistantProcessor assistant) {
        this.local = local;
        this.assistant = assistant;
    }

    CommandOutcome process(String text) {
        CommandOutcome localOutcome = local.process(text);
        if (localOutcome.status() != CommandOutcome.Status.NO_MATCH) {
            return localOutcome;
        }
        return assistant.ask(text);
    }
}
