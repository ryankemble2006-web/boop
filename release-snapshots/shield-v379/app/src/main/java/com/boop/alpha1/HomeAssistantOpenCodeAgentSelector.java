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
        String boopOpenCode = "";
        String genericOpenCode = "";
        for (Agent agent : agents) {
            String id = agent.id.trim().toLowerCase(Locale.ROOT);
            String name = agent.name.trim().toLowerCase(Locale.ROOT);
            if (name.equals("boop") || id.equals("conversation.boop")) {
                return agent.id;
            }
            boolean openCode = id.contains("opencode") || name.contains("opencode");
            boolean boop = id.contains("boop") || name.contains("boop");
            if (openCode && boop && boopOpenCode.isBlank()) {
                boopOpenCode = agent.id;
            } else if (openCode && genericOpenCode.isBlank()) {
                genericOpenCode = agent.id;
            }
        }
        return boopOpenCode.isBlank() ? genericOpenCode : boopOpenCode;
    }
}
