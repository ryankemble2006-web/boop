package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public final class HomeAssistantOpenCodeAgentSelectorTest {
    @Test public void exactBoopNameWinsEvenWhenIdDoesNotContainOpenCode() {
        String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.generic_opencode", "OpenCode"),
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.boop", "BOOP")));
        assertEquals("conversation.boop", selected);
    }

    @Test public void exactBoopMatchIsTrimmedAndCaseInsensitive() {
        String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.current", "  boop  ")));
        assertEquals("conversation.current", selected);
    }

    @Test public void unrelatedBoopSubstringIsIgnored() {
        String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
                new HomeAssistantOpenCodeAgentSelector.Agent("conversation.sboop", "Sboop")));
        assertEquals("", selected);
    }

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
