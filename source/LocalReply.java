package com.boop.alpha1;

import java.util.Locale;

final class LocalReply {
    private LocalReply() { }

    static String forOutcome(CommandOutcome outcome) {
        switch (outcome.status()) {
            case SUCCESS:
                return "Done.";
            case UNREACHABLE:
                return "I can't reach the house right now.";
            case TARGET_OFFLINE:
                return "The " + outcome.area().toLowerCase(Locale.ROOT) + " "
                        + outcome.targetName().toLowerCase(Locale.ROOT) + " is offline.";
            case NO_TARGET:
                return "I can't find that.";
            case NO_MATCH:
                return "I didn't understand that.";
            case AUTH_REQUIRED:
                return "I need to reconnect to the house.";
            case ASSISTANT_REPLY:
                return outcome.assistantSpeech();
            case ASSISTANT_NO_AGENT:
                return "I can control the house, but I don't have my assistant connected yet.";
            case ASSISTANT_UNREACHABLE:
                return "I can still control the house, but I can't reach my assistant right now.";
            case ASSISTANT_FAILED:
                return "I can still control the house, but my assistant didn't answer that.";
            case FAILED:
            default:
                return "That didn't work.";
        }
    }
}
