package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class BoopWakeNameIntentTest {
    @Test public void parsesRequiredRenamePhrases() {
        assertSet("your new name is Steve", "Steve");
        assertSet("your name is Jarvis", "Jarvis");
        assertSet("I'm calling you AI Dood", "AI Dood");
        assertSet("from now on you're Sir Boopington", "Sir Boopington");
        assertSet("change name to Steve", "Steve");
    }

    @Test public void parsesRequiredResetPhrases() {
        assertReset("your name is BOOP again");
        assertReset("reset your name");
        assertReset("go back to BOOP");
    }

    @Test public void ordinaryCommandsRemainOrdinary() {
        assertEquals(BoopWakeNameIntent.Action.NONE, BoopWakeNameIntent.parse("turn on the lamp").action());
    }

    private static void assertSet(String text, String expected) {
        BoopWakeNameIntent.Result result = BoopWakeNameIntent.parse(text);
        assertEquals(BoopWakeNameIntent.Action.SET, result.action());
        assertEquals(expected, result.name());
    }

    private static void assertReset(String text) {
        BoopWakeNameIntent.Result result = BoopWakeNameIntent.parse(text);
        assertEquals(BoopWakeNameIntent.Action.RESET, result.action());
        assertEquals("BOOP", result.name());
    }
}
