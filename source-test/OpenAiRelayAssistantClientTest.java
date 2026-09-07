package com.boop.alpha1;

import org.junit.Test;

/** Exercise the real client against an isolated mock upstream, never paid API traffic. */
public class OpenAiRelayAssistantClientTest {
    @Test public void relayProtocolAndFailureContract() throws Exception {
        OpenAiRelayAssistantClientHarness.main(new String[0]);
    }
}
