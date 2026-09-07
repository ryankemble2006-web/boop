package com.boop.alpha1;

public final class LocalReplyRelayHarness {
    public static void main(String[] args) {
        expect(CommandOutcome.assistantSetupRequired(), "Chat mode needs setting up first.");
        expect(CommandOutcome.assistantAuthRequired(), "Chat mode needs reconnecting.");
        expect(CommandOutcome.assistantQuota(), "Chat mode has no allowance left right now.");
        expect(CommandOutcome.assistantRateLimit(), "Chat is busy. Try again in a moment.");
        expect(CommandOutcome.assistantTimeout(), "Chat took too long to answer.");
        expect(CommandOutcome.assistantService(), "I can't reach chat right now.");
        System.out.println("PASS: LocalReplyRelayHarness");
    }

    private static void expect(CommandOutcome outcome, String expected) {
        String actual = LocalReply.forOutcome(outcome);
        if (!expected.equals(actual)) {
            throw new AssertionError("expected '" + expected + "' but got '" + actual + "'");
        }
        if (actual.contains("house") && outcome.status().name().startsWith("ASSISTANT_")) {
            throw new AssertionError("conversation failure must not be described as a house failure");
        }
    }
}
