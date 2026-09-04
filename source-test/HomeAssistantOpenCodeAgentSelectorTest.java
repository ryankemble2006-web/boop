package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public final class HomeAssistantOpenCodeAgentSelectorTest {
    @Test public void selectsOnlyOpenCodeAgent() {
        String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
                new HomeAssistantOpenCodeAgentSelector.Agent("home_assistant", "Home Assistant"),
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.openai", "OpenAI Conversation"),
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.boop_opencode", "BOOP OpenCode")));
        assertEquals("conversation.boop_opencode", selected);
    }

    @Test public void genericThirdPartyAgentsAreIgnored() {
        String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.chatgpt", "ChatGPT"),
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.gemini", "Gemini")));
        assertEquals("", selected);
    }

    @Test public void emptyListMeansNoAgent() {
        assertEquals("", HomeAssistantOpenCodeAgentSelector.select(Collections.emptyList()));
    }
}
