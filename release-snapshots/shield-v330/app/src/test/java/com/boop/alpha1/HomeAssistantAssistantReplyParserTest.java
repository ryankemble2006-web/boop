package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class HomeAssistantAssistantReplyParserTest {
    @Test public void extractsSpeechAndConversationId() {
        HomeAssistantAssistantReply reply = HomeAssistantAssistantReplyParser.parse(
                "{\"conversation_id\":\"thread-8\",\"response\":{\"response_type\":\"query_answer\",\"speech\":{\"plain\":{\"speech\":\"Because molecules scatter blue light.\"}},\"data\":{}}}");
        assertEquals("Because molecules scatter blue light.", reply.speech());
        assertEquals("thread-8", reply.conversationId());
    }

    @Test public void malformedReplyIsEmpty() {
        HomeAssistantAssistantReply reply = HomeAssistantAssistantReplyParser.parse("not json");
        assertEquals("", reply.speech());
        assertEquals("", reply.conversationId());
    }
}
