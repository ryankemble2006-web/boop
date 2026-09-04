package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class HomeAssistantPipelineSelectorTest {
    @Test public void usesPreferredAssistPipelineConversationEngine() {
        String json = "{"
                + "\"pipelines\":["
                + "{\"id\":\"local\",\"name\":\"Home Assistant\",\"conversation_engine\":\"conversation.home_assistant\"},"
                + "{\"id\":\"boop\",\"name\":\"BOOP\",\"conversation_engine\":\"conversation.openai_conversation\"}"
                + "],"
                + "\"preferred_pipeline\":\"boop\""
                + "}";

        assertEquals(
                "conversation.openai_conversation",
                HomeAssistantPipelineSelector.selectConversationEngine(json));
    }

    @Test public void fallsBackToFirstNonBuiltInConversationEngine() {
        String json = "{"
                + "\"pipelines\":["
                + "{\"id\":\"local\",\"conversation_engine\":\"conversation.home_assistant\"},"
                + "{\"id\":\"ai\",\"conversation_engine\":\"conversation.some_ai\"}"
                + "]"
                + "}";

        assertEquals(
                "conversation.some_ai",
                HomeAssistantPipelineSelector.selectConversationEngine(json));
    }
}
