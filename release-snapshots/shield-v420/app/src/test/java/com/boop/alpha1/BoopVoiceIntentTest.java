package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

public final class BoopVoiceIntentTest {
    @Test
    public void acceptsNaturalWaysToAskForAnotherVoice() {
        assertTrue(matches("Can I change your voice?"));
        assertTrue(matches("could you use a different voice"));
        assertTrue(matches("Try another voice"));
        assertTrue(matches("I don't like this voice"));
        assertTrue(matches("can I pick a new voice for you"));
        assertTrue(matches("can you sound different"));
        assertTrue(matches("swap voices"));
        assertTrue(matches("change voice"));
        assertTrue(matches("give me another voice"));
    }

    @Test
    public void doesNotStealUnrelatedSpeech() {
        assertFalse(matches("turn your volume up"));
        assertFalse(matches("what is voice acting"));
        assertFalse(matches("change the living room volume"));
        assertFalse(matches("can you hear my voice"));
        assertFalse(matches("tell me about voice assistants"));
        assertFalse(matches("change the voice volume"));
        assertFalse(matches("pause"));
    }

    private static boolean matches(String text) {
        try {
            Class<?> matcher = Class.forName("com.boop.alpha1.BoopVoiceIntent");
            Method method = matcher.getDeclaredMethod("matches", String.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(null, text);
        } catch (ReflectiveOperationException e) {
            fail("BoopVoiceIntent matcher is missing: " + e.getClass().getSimpleName());
            return false;
        }
    }
}
