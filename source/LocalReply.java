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
                return "Which room?";
            case NO_MATCH:
                return "I didn't understand that.";
            case AUTH_REQUIRED:
                return "I need to reconnect to the house.";
            case FAILED:
            default:
                return "That didn't work.";
        }
    }
}
