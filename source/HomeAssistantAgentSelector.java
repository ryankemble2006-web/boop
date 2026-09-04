package com.boop.alpha1;

import java.util.List;
import java.util.Locale;

final class HomeAssistantAgentSelector {
    static final class Agent {
        private final String id;
        private final String name;

        Agent(String id, String name) {
            this.id = id == null ? "" : id;
            this.name = name == null ? "" : name;
        }

        String id() { return id; }
        String name() { return name; }
    }

    private HomeAssistantAgentSelector() { }

    static String select(List<Agent> agents) {
        if (agents == null) {
            return "";
        }

        String fallback = "";
        for (Agent agent : agents) {
            if (agent == null) {
                continue;
            }

            String id = agent.id().trim();
            if (id.isEmpty()
                    || id.equalsIgnoreCase("home_assistant")
                    || id.equalsIgnoreCase("conversation.home_assistant")) {
                continue;
            }

            if (fallback.isEmpty()) {
                fallback = id;
            }

            String haystack = (id + " " + agent.name()).toLowerCase(Locale.ROOT);
            if (haystack.contains("openai")
                    || haystack.contains("chatgpt")
                    || haystack.contains("gpt")) {
                return id;
            }
        }

        return fallback;
    }
}
