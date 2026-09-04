package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test public void thinkingCallbacksWrapOnlyAssistantFallback() {
        List<String> events = new ArrayList<>();
        BoopCommandRouter.AssistantActivity activity = new BoopCommandRouter.AssistantActivity() {
            @Override public void onAssistantStarted() { events.add("start"); }
            @Override public void onAssistantFinished() { events.add("finish"); }
        };

        BoopCommandRouter localRouter = new BoopCommandRouter(
                text -> CommandOutcome.success("light"),
                text -> { events.add("assistant"); return CommandOutcome.assistantReply("unused"); },
                activity);
        localRouter.process("turn on the light");
        assertTrue(events.isEmpty());

        BoopCommandRouter assistantRouter = new BoopCommandRouter(
                text -> CommandOutcome.noMatch(),
                text -> { events.add("assistant"); return CommandOutcome.assistantReply("orange"); },
                activity);
        CommandOutcome result = assistantRouter.process("why are oranges orange");

        assertEquals(CommandOutcome.Status.ASSISTANT_REPLY, result.status());
        assertEquals(List.of("start", "assistant", "finish"), events);
    }

    @Test public void thinkingAlwaysFinishesWhenAssistantThrows() {
        List<String> events = new ArrayList<>();
        BoopCommandRouter.AssistantActivity activity = new BoopCommandRouter.AssistantActivity() {
            @Override public void onAssistantStarted() { events.add("start"); }
            @Override public void onAssistantFinished() { events.add("finish"); }
        };
        BoopCommandRouter router = new BoopCommandRouter(
                text -> CommandOutcome.noMatch(),
                text -> { throw new RuntimeException("boom"); },
                activity);

        try {
            router.process("question");
        } catch (RuntimeException expected) {
            // Expected: this test only locks the lifecycle cleanup.
        }

        assertEquals(List.of("start", "finish"), events);
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
