package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class BoopCommandRouterTest {
    @Test public void onlyNoMatchFallsThroughToAssistant() {
        for (CommandOutcome.Status status : new CommandOutcome.Status[]{
                CommandOutcome.Status.SUCCESS,
                CommandOutcome.Status.NO_TARGET,
                CommandOutcome.Status.TARGET_OFFLINE,
                CommandOutcome.Status.FAILED,
                CommandOutcome.Status.UNREACHABLE,
                CommandOutcome.Status.AUTH_REQUIRED}) {
            Counter assistantCalls = new Counter();
            BoopCommandRouter router = new BoopCommandRouter(
                    text -> outcome(status),
                    text -> { assistantCalls.value++; return CommandOutcome.assistantReply("chat"); });
            CommandOutcome result = router.process("anything");
            assertEquals(status, result.status());
            assertEquals(0, assistantCalls.value);
        }

        Counter assistantCalls = new Counter();
        BoopCommandRouter router = new BoopCommandRouter(
                text -> CommandOutcome.noMatch(),
                text -> { assistantCalls.value++; return CommandOutcome.assistantReply("blue sky"); });
        CommandOutcome result = router.process("why is the sky blue");
        assertEquals(CommandOutcome.Status.ASSISTANT_REPLY, result.status());
        assertEquals("blue sky", result.assistantSpeech());
        assertEquals(1, assistantCalls.value);
    }

    private static CommandOutcome outcome(CommandOutcome.Status status) {
        switch (status) {
            case SUCCESS: return CommandOutcome.success("thing");
            case NO_TARGET: return CommandOutcome.noTarget();
            case TARGET_OFFLINE: return CommandOutcome.targetOffline("thing", "Living Room");
            case FAILED: return CommandOutcome.failed();
            case UNREACHABLE: return CommandOutcome.unreachable();
            case AUTH_REQUIRED: return CommandOutcome.authRequired();
            default: throw new IllegalArgumentException();
        }
    }

    private static final class Counter { int value; }
}
