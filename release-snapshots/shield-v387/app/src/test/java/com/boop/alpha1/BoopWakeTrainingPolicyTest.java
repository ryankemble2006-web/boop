package com.boop.alpha1;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class BoopWakeTrainingPolicyTest {
    @Test public void changedCustomNameAutomaticallyFlowsIntoFiveSayTraining() {
        assertTrue(BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                "BOOP", "Steve", false));
        assertTrue(BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                "Steve", "Dave", false));
    }

    @Test public void closingSettingsDoesNotNagForAnUnchangedName() {
        assertFalse(BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                "Steve", "Steve", false));
    }

    @Test public void boopFallbackNeverNeedsTraining() {
        assertFalse(BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                "Steve", "BOOP", false));
    }

    @Test public void existingMatchingProfileDoesNotRetrainAutomatically() {
        assertFalse(BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                "BOOP", "Steve", true));
    }
}
