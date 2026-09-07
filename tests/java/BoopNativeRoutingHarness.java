package com.boop.alpha1;

/** Mutations caught: sending non-NO_MATCH to a provider, double local calls, wrong mode. */
public final class BoopNativeRoutingHarness {
    static void check(boolean ok, String reason) { if (!ok) throw new AssertionError(reason); }
    public static void main(String[] args) {
        check(BoopChatMode.fromStored("native_chat") == BoopChatMode.NATIVE_CHAT, "native preference missing");
        check(BoopChatMode.fromStored(BoopChatMode.NATIVE_CHAT.storedValue()) == BoopChatMode.NATIVE_CHAT, "native round trip");
        check(BoopChatMode.fromStored(null) == BoopChatMode.OPENCODE, "default changed");
        check(BoopChatMode.fromStored("future-value") == BoopChatMode.OPENCODE, "unknown preference fallback");
        CommandOutcome[] localOutcomes = {CommandOutcome.success("test"), CommandOutcome.targetOffline("test","room"),
            CommandOutcome.noTarget(), CommandOutcome.failed(), CommandOutcome.unreachable(), CommandOutcome.authRequired(),
            CommandOutcome.noMatch()};
        for (BoopChatMode mode : BoopChatMode.values()) {
            for (CommandOutcome local : localOutcomes) {
                int[] calls = new int[5];
                BoopCommandRouter router = new BoopCommandRouter(text -> { calls[0]++; return local; },
                    text -> { calls[1]++; return CommandOutcome.assistantReply("opencode"); },
                    new BoopCommandRouter.AssistantActivity() {
                        public void onAssistantStarted() { calls[3]++; }
                        public void onAssistantFinished() { calls[4]++; }
                    });
                BoopCommandRouter.AssistantProcessor nativeChat = text -> { calls[2]++; return CommandOutcome.assistantReply("native"); };
                CommandOutcome result = router.process("question", () -> mode != BoopChatMode.FREE_CHAT,
                    mode == BoopChatMode.NATIVE_CHAT ? nativeChat : null);
                boolean chat = local.status() == CommandOutcome.Status.NO_MATCH && mode != BoopChatMode.FREE_CHAT;
                check(calls[0] == 1, "local processing must happen exactly once");
                check(calls[1] == (chat && mode == BoopChatMode.OPENCODE ? 1:0), "incorrect OpenCode call");
                check(calls[2] == (chat && mode == BoopChatMode.NATIVE_CHAT ? 1:0), "incorrect native call");
                check(calls[3] == (chat?1:0) && calls[4] == calls[3], "unbalanced assistant activity");
                if (!chat) check(result == local, "local/browser outcome was rewritten");
                else check(result.assistantSpeech().equals(mode == BoopChatMode.NATIVE_CHAT?"native":"opencode"), "wrong response");
            }
        }
        int[] done = new int[2];
        BoopCommandRouter throwing = new BoopCommandRouter(t -> CommandOutcome.noMatch(), t -> CommandOutcome.assistantFailed(),
            new BoopCommandRouter.AssistantActivity() {
                public void onAssistantStarted() { done[0]++; }
                public void onAssistantFinished() { done[1]++; }
            });
        try {
            throwing.process("q", () -> true, t -> { throw new IllegalStateException("test boundary"); });
            throw new AssertionError("exception swallowed");
        } catch (IllegalStateException expected) { check(done[0]==1 && done[1]==1, "finish omitted on exception"); }
        int[] disabled = new int[1];
        throwing.process("q", () -> false, t -> {disabled[0]++; return CommandOutcome.assistantReply("bad");});
        check(disabled[0] == 0, "stale request reached relay");
        System.out.println("BOOP_NATIVE_ROUTING_PASS");
    }
}
