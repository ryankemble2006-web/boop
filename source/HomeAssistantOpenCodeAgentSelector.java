package com.boop.alpha1;

import java.util.List;
import java.util.Locale;

final class HomeAssistantOpenCodeAgentSelector {
    static final class Agent {
        final String id;
        final String name;

        Agent(String id, String name) {
            this.id = id == null ? "" : id;
            this.name = name == null ? "" : name;
        }
    }

    private HomeAssistantOpenCodeAgentSelector() { }

    static String select(List<Agent> agents) {
        String fallback = "";
        for (Agent agent : agents) {
            String id = agent.id.toLowerCase(Locale.ROOT);
            String name = agent.name.toLowerCase(Locale.ROOT);
            if (!id.contains("opencode") && !name.contains("opencode")) {
                continue;
            }
            if ((id.contains("boop") && id.contains("opencode"))
                    || (name.contains("boop") && name.contains("opencode"))) {
                return agent.id;
            }
            if (fallback.isBlank()) {
                fallback = agent.id;
            }
        }
        return fallback;
    }
}
