package com.boop.alpha1;

final class HomeAssistantClient {
    CommandOutcome process(String text) { return CommandOutcome.noMatch(); }
}

final class HomeAssistantGeneralAssistantClient {
    CommandOutcome ask(String text) { return CommandOutcome.assistantFailed(); }
}

public final class BoopChatModeHarness {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(BoopChatMode.fromStored(null) == BoopChatMode.OPENCODE, "first install defaults to OpenCode");
        check(BoopChatMode.fromStored("corrupt") == BoopChatMode.OPENCODE, "unknown preference is safe");
        for (BoopChatMode mode : BoopChatMode.values()) {
            check(BoopChatMode.fromStored(mode.storedValue()) == mode, "stored choice round trip");
        }
        check(BoopChatMode.fromStored("native_chat") == BoopChatMode.NATIVE_CHAT,
                "native ChatGPT mode persists explicitly");

        BoopChatModeHold hold = new BoopChatModeHold();
        hold.begin(100, 50, 80, 8);
        check(!hold.tryOpen(3099), "no early menu");
        check(hold.tryOpen(3100), "menu at exactly three seconds");
        check(!hold.tryOpen(6100), "only one menu per hold");
        hold.begin(100, 50, 80, 8);
        hold.move(57, 80, 1);
        check(hold.tryOpen(3100), "small finger drift is allowed");
        hold.begin(100, 50, 80, 8);
        hold.move(59, 80, 1);
        hold.move(50, 80, 1);
        check(!hold.tryOpen(5000), "moving away and back cancels permanently");
        hold.begin(100, 50, 80, 8);
        hold.move(50, 89, 1);
        check(!hold.tryOpen(5000), "vertical motion cancels");
        hold.begin(100, 50, 80, 8);
        hold.move(50, 80, 2);
        check(!hold.tryOpen(5000), "multi-touch cancels");
        hold.begin(100, 50, 80, 8);
        hold.cancel();
        check(!hold.tryOpen(5000), "release/pause/cancel prevents delayed menu");
        hold.begin(100, 50, 80, 8);
        check(!hold.tryOpen(99), "backwards time cannot open menu");
        check(hold.tryOpen(4100), "delayed main-thread callback still works");
        hold.begin(100, 50, 80, 8);
        hold.move(Float.NaN, 80, 1);
        check(!hold.tryOpen(5000), "non-finite coordinates cancel");

        final int[] calls = new int[4];
        BoopCommandRouter.AssistantActivity activity = new BoopCommandRouter.AssistantActivity() {
            public void onAssistantStarted() { calls[1]++; }
            public void onAssistantFinished() { calls[2]++; }
        };
        BoopCommandRouter noMatch = new BoopCommandRouter(
                text -> CommandOutcome.noMatch(),
                text -> { calls[0]++; return CommandOutcome.assistantReply("opencode"); }, activity);
        check(noMatch.process("a silly question", false).status() == CommandOutcome.Status.NO_MATCH,
                "Free Chat returns a web handoff only after a local no-match");
        check(calls[0] == 0 && calls[1] == 0 && calls[2] == 0, "Free Chat never touches OpenCode or its animation");
        check(noMatch.process("a silly question").status() == CommandOutcome.Status.ASSISTANT_REPLY,
                "default OpenCode behavior is unchanged");
        check(calls[0] == 1 && calls[1] == 1 && calls[2] == 1, "OpenCode lifecycle balances");

        CommandOutcome nativeReply = noMatch.processWithAssistant("native question", text -> {
            calls[3]++;
            return CommandOutcome.assistantReply("native");
        });
        check(nativeReply.status() == CommandOutcome.Status.ASSISTANT_REPLY,
                "native assistant can be selected after local no-match");
        check(calls[3] == 1, "native assistant invoked exactly once");
        check(calls[1] == 2 && calls[2] == 2, "native assistant lifecycle balances");

        final boolean[] enabled = {true};
        BoopCommandRouter switchedWhileLocal = new BoopCommandRouter(text -> {
            enabled[0] = false;
            return CommandOutcome.noMatch();
        }, text -> { throw new AssertionError("mode changed during local processing"); });
        check(switchedWhileLocal.process("question", () -> enabled[0]).status() == CommandOutcome.Status.NO_MATCH,
                "mode is checked after local processing, not before it");

        CommandOutcome[] localResults = {CommandOutcome.success("Media"), CommandOutcome.noTarget(),
                CommandOutcome.targetOffline("Light", "Room"), CommandOutcome.failed(),
                CommandOutcome.unreachable(), CommandOutcome.authRequired()};
        for (CommandOutcome result : localResults) {
            final int[] assistantCalls = {0};
            BoopCommandRouter local = new BoopCommandRouter(text -> result,
                    text -> { assistantCalls[0]++; return CommandOutcome.assistantReply("wrong"); }, activity);
            check(local.process("local command", false) == result, "Free Chat preserves local outcome " + result.status());
            check(local.process("local command", true) == result, "OpenCode preserves local outcome " + result.status());
            check(local.processWithAssistant("local command", text -> {
                assistantCalls[0]++;
                return CommandOutcome.assistantReply("wrong");
            }) == result, "native ChatGPT preserves local outcome " + result.status());
            check(assistantCalls[0] == 0, "non-NO_MATCH never invokes any conversation processor");
        }

        BoopCommandRouter failing = new BoopCommandRouter(text -> CommandOutcome.noMatch(),
                text -> { throw new IllegalStateException("offline"); }, activity);
        int started = calls[1], finished = calls[2];
        try { failing.process("question", true); throw new AssertionError("expected exception"); }
        catch (IllegalStateException expected) { }
        check(calls[1] == started + 1 && calls[2] == finished + 1, "assistant animation finishes even on failure");
        System.out.println("PASS: " + checks + " chat mode/hold/local-first checks");
    }
}
