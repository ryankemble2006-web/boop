package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopDevMenuIntentTest {
    @Test
    public void exactSpokenDevMenuIsLocalIntent() {
        assertTrue(BoopDevMenuIntent.matches("dev menu"));
        assertTrue(BoopDevMenuIntent.matches("Dev menu."));
        assertTrue(BoopDevMenuIntent.matches("  dev menu!  "));
    }

    @Test
    public void unrelatedSpeechDoesNotBecomeDevMenu() {
        assertFalse(BoopDevMenuIntent.matches(null));
        assertFalse(BoopDevMenuIntent.matches(""));
        assertFalse(BoopDevMenuIntent.matches("open settings"));
        assertFalse(BoopDevMenuIntent.matches("open dev menu"));
    }
}
