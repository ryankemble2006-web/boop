package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class LocalReplyTest {
    @Test public void successIsPlain() {
        assertEquals("Done.", LocalReply.forOutcome(CommandOutcome.success("Fan")));
    }

    @Test public void unreachableIsHouseSpecific() {
        assertEquals("I can't reach the house right now.", LocalReply.forOutcome(CommandOutcome.unreachable()));
    }

    @Test public void offlineFanNamesLocalRoom() {
        assertEquals("The living room fan is offline.", LocalReply.forOutcome(CommandOutcome.targetOffline("Fan", "Living Room")));
    }

    @Test public void noTargetSaysCantFindThat() {
        assertEquals("I can't find that.", LocalReply.forOutcome(CommandOutcome.noTarget()));
    }
}
