package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;

public final class HomeAssistantAgentSelectorTest {
    @Test public void prefersOpenAiAgentAndIgnoresBuiltIn() {
        assertEquals(
                "conversation.openai_conversation",
                HomeAssistantAgentSelector.select(List.of(
                        new HomeAssistantAgentSelector.Agent("home_assistant", "Home Assistant"),
                        new HomeAssistantAgentSelector.Agent("conversation.other", "Other"),
                        new HomeAssistantAgentSelector.Agent("conversation.openai_conversation", "OpenAI Conversation"))));
    }

    @Test public void fallsBackToFirstNonBuiltInAgent() {
        assertEquals(
                "conversation.local_llm",
                HomeAssistantAgentSelector.select(List.of(
                        new HomeAssistantAgentSelector.Agent("conversation.home_assistant", "Home Assistant"),
                        new HomeAssistantAgentSelector.Agent("conversation.local_llm", "Local Brain"),
                        new HomeAssistantAgentSelector.Agent("conversation.second", "Second Brain"))));
    }

    @Test public void returnsEmptyWhenOnlyBuiltInExists() {
        assertEquals(
                "",
                HomeAssistantAgentSelector.select(List.of(
                        new HomeAssistantAgentSelector.Agent("home_assistant", "Home Assistant"),
                        new HomeAssistantAgentSelector.Agent("conversation.home_assistant", "Home Assistant"))));
    }
}
