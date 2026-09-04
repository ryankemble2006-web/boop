package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class AssistantFallbackPolicyTest {
    @Test public void onlyNoMatchFallsThroughToAssistant() {
        assertTrue(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.NO_MATCH));
        assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.SUCCESS));
        assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.TARGET_OFFLINE));
        assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.NO_TARGET));
        assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.FAILED));
        assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.UNREACHABLE));
        assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.AUTH_REQUIRED));
    }
}
