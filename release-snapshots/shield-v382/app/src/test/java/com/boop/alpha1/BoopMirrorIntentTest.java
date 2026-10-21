package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class BoopMirrorIntentTest {
    @Test public void naturalOpenPhrasesAllReachMirror() {
        String[] phrases = {
                "mirror",
                "Mirror mode",
                "hey BOOP mirror",
                "BOOP, show me the mirror please",
                "could you open the mirror",
                "please bring up mirror",
                "turn mirror on",
                "switch on the mirror",
                "let me see the mirror",
                "I want the mirror"
        };
        for (String phrase : phrases) {
            assertEquals(phrase, BoopMirrorIntent.Action.OPEN, BoopMirrorIntent.actionFor(phrase));
        }
    }

    @Test public void naturalClosePhrasesReturnToBoop() {
        String[] phrases = {
                "close mirror",
                "hey BOOP hide the mirror",
                "turn off mirror",
                "switch mirror off please",
                "leave mirror mode",
                "back to BOOP",
                "bring BOOP back"
        };
        for (String phrase : phrases) {
            assertEquals(phrase, BoopMirrorIntent.Action.CLOSE, BoopMirrorIntent.actionFor(phrase));
        }
    }

    @Test public void ordinaryMirrorQuestionsAreNotCommands() {
        assertEquals(BoopMirrorIntent.Action.NONE,
                BoopMirrorIntent.actionFor("what is a mirror"));
        assertEquals(BoopMirrorIntent.Action.NONE,
                BoopMirrorIntent.actionFor("is the mirror clean"));
        assertEquals(BoopMirrorIntent.Action.NONE,
                BoopMirrorIntent.actionFor("tell me about mirrors"));
    }
}
