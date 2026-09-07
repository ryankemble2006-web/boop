package com.boop.alpha1;

/** Fails if native conversation errors become house failures or lose their distinct advice. */
public final class BoopNativeReplyHarness {
    private static void reply(CommandOutcome outcome, String expected) {
        String actual = LocalReply.forOutcome(outcome);
        if (!expected.equals(actual)) throw new AssertionError(outcome.status() + ": " + actual);
    }
    public static void main(String[] args) {
        reply(CommandOutcome.assistantSetupRequired(), "Native Chat isn't connected yet. You can still use Free Chat.");
        reply(CommandOutcome.assistantAuthRequired(), "My chat connection needs attention. You can still use Free Chat.");
        reply(CommandOutcome.assistantQuota(), "My chat credit has run out. You can switch to Free Chat.");
        reply(CommandOutcome.assistantRateLimit(), "Chat is busy. Please try again in a moment.");
        reply(CommandOutcome.assistantTimeout(), "Chat took too long to answer. Please try again.");
        reply(CommandOutcome.assistantService(), "Chat is unavailable right now. Please try again later.");
        reply(CommandOutcome.success("lamp"), "Done.");
        reply(CommandOutcome.unreachable(), "I can't reach the house right now.");
        reply(CommandOutcome.authRequired(), "I need to reconnect to the house.");
        reply(CommandOutcome.assistantReply("A spoken answer."), "A spoken answer.");
        if (CommandOutcome.assistantAuthRequired().status() == CommandOutcome.Status.AUTH_REQUIRED)
            throw new AssertionError("conversation auth must not clear house credentials");
        System.out.println("BOOP_NATIVE_REPLIES_PASS");
    }
}
