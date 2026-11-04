package com.boop.alpha1;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class BoopAssistantIntegrationPolicyTest {
    @Test
    public void firstRunChoiceIsRequiredUntilUserDecides() {
        BoopAssistantIntegrationPolicy policy = new BoopAssistantIntegrationPolicy(null);
        assertTrue(policy.needsFirstRunChoice());

        policy = policy.withChoice(BoopAssistantIntegrationPolicy.Choice.KEEP_CURRENT);
        assertFalse(policy.needsFirstRunChoice());
    }

    @Test
    public void useBoopRequestsRoleButNeverClaimsItWasGranted() {
        BoopAssistantIntegrationPolicy policy = new BoopAssistantIntegrationPolicy(
                BoopAssistantIntegrationPolicy.Choice.USE_BOOP);
        assertTrue(policy.shouldRequestAssistantRole(false));
        assertFalse(policy.shouldRequestAssistantRole(true));
    }

    @Test
    public void keepCurrentNeverRequestsAssistantRole() {
        BoopAssistantIntegrationPolicy policy = new BoopAssistantIntegrationPolicy(
                BoopAssistantIntegrationPolicy.Choice.KEEP_CURRENT);
        assertFalse(policy.shouldRequestAssistantRole(false));
        assertFalse(policy.shouldRequestAssistantRole(true));
    }
}
