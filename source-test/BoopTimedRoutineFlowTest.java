package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.time.LocalDateTime;

public final class BoopTimedRoutineFlowTest {
    @Test public void asksOnceOrRecurringThenBuildsOneShotDelay() {
        BoopTimedRoutineFlow flow = new BoopTimedRoutineFlow();
        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 19, 30, 0);

        BoopTimedRoutineFlow.Result first =
                flow.process("at 8pm turn the pond pump off", now);
        assertEquals(BoopTimedRoutineFlow.Kind.ASK_ONCE_OR_RECURRING, first.kind());
        assertNull(first.haCommand());

        BoopTimedRoutineFlow.Result second = flow.process("just once", now);
        assertEquals(BoopTimedRoutineFlow.Kind.RUN_ONCE, second.kind());
        assertEquals("turn the pond pump off in 30 minutes", second.haCommand());
    }

    @Test public void understandsTimeAtEndAndNextDay() {
        BoopTimedRoutineFlow flow = new BoopTimedRoutineFlow();
        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 21, 0, 0);

        assertEquals(
                BoopTimedRoutineFlow.Kind.ASK_ONCE_OR_RECURRING,
                flow.process("turn the pond pump off at 8 pm", now).kind());
        assertEquals(
                "turn the pond pump off in 23 hours",
                flow.process("once", now).haCommand());
    }

    @Test public void acceptsRecurringWordsButKeepsThatAsSeparateSeam() {
        BoopTimedRoutineFlow flow = new BoopTimedRoutineFlow();
        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 18, 0, 0);

        flow.process("at 20:00 turn off the pond pump", now);
        BoopTimedRoutineFlow.Result recurring = flow.process("daily", now);

        assertEquals(BoopTimedRoutineFlow.Kind.RECURRING_REQUESTED, recurring.kind());
        assertEquals("turn off the pond pump", recurring.actionText());
        assertEquals(20, recurring.hour());
        assertEquals(0, recurring.minute());
    }

    @Test public void unknownChoiceReasksRatherThanGuessing() {
        BoopTimedRoutineFlow flow = new BoopTimedRoutineFlow();
        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 18, 0, 0);

        flow.process("at 8pm turn off the pond pump", now);
        assertEquals(
                BoopTimedRoutineFlow.Kind.ASK_ONCE_OR_RECURRING,
                flow.process("erm maybe", now).kind());
    }

    @Test public void ordinaryCommandsAreNotHijacked() {
        BoopTimedRoutineFlow flow = new BoopTimedRoutineFlow();
        LocalDateTime now = LocalDateTime.of(2026, 9, 5, 18, 0, 0);

        assertEquals(BoopTimedRoutineFlow.Kind.NOT_TIMED, flow.process("lights purple", now).kind());
        assertEquals(BoopTimedRoutineFlow.Kind.NOT_TIMED, flow.process("what is on at 8pm", now).kind());
    }
}
