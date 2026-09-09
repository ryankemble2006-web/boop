package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopDevMenuIntentTest {
    @Test
    public void exactSpokenDeveloperMenuIsLocalIntent() {
        assertTrue(BoopDevMenuIntent.matches("developer menu"));
        assertTrue(BoopDevMenuIntent.matches("Developer menu."));
        assertTrue(BoopDevMenuIntent.matches("  developer menu!  "));
    }

    @Test
    public void oldOrUnrelatedSpeechDoesNotBecomeDeveloperMenu() {
        assertFalse(BoopDevMenuIntent.matches(null));
        assertFalse(BoopDevMenuIntent.matches(""));
        assertFalse(BoopDevMenuIntent.matches("dev menu"));
        assertFalse(BoopDevMenuIntent.matches("open settings"));
        assertFalse(BoopDevMenuIntent.matches("open developer menu"));
    }
}
