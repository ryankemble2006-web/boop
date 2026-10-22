package com.boop.alpha1;

import java.util.Locale;

final class BoopMirrorIntent {
    enum Action { NONE, OPEN, CLOSE }

    private BoopMirrorIntent() { }

    static Action actionFor(String transcript) {
        String command = canonical(transcript);
        if (command == null) {
            return Action.NONE;
        }

        if (isClose(command)) {
            return Action.CLOSE;
        }
        if (isOpen(command)) {
            return Action.OPEN;
        }
        return Action.NONE;
    }

    private static boolean isClose(String command) {
        if (command.equals("back to boop")
                || command.equals("boop back")
                || command.equals("eyes back")
                || command.equals("bring boop back")) {
            return true;
        }
        if (!containsWord(command, "mirror")) {
            return false;
        }
        return command.equals("close mirror")
                || command.equals("hide mirror")
                || command.equals("stop mirror")
                || command.equals("exit mirror")
                || command.equals("end mirror")
                || command.equals("dismiss mirror")
                || command.equals("turn mirror off")
                || command.equals("turn off mirror")
                || command.equals("switch mirror off")
                || command.equals("switch off mirror")
                || command.equals("turn the mirror off")
                || command.equals("turn off the mirror")
                || command.equals("switch the mirror off")
                || command.equals("switch off the mirror")
                || command.equals("close the mirror")
                || command.equals("hide the mirror")
                || command.equals("stop the mirror")
                || command.equals("exit the mirror")
                || command.equals("leave mirror mode")
                || command.equals("close mirror mode")
                || startsWithAny(command,
                        "close mirror ",
                        "hide mirror ",
                        "stop mirror ",
                        "exit mirror ",
                        "turn off mirror ",
                        "turn mirror off ",
                        "turn off the mirror ",
                        "turn the mirror off ",
                        "switch off the mirror ",
                        "switch the mirror off ");
    }

    private static boolean isOpen(String command) {
        if (command.equals("mirror") || command.equals("mirror mode")) {
            return true;
        }
        if (!containsWord(command, "mirror")) {
            return false;
        }
        return startsWithAny(command,
                "show mirror",
                "show the mirror",
                "show me mirror",
                "show me the mirror",
                "open mirror",
                "open the mirror",
                "start mirror",
                "start the mirror",
                "launch mirror",
                "launch the mirror",
                "bring up mirror",
                "bring up the mirror",
                "turn on mirror",
                "turn on the mirror",
                "turn mirror on",
                "turn the mirror on",
                "switch on mirror",
                "switch on the mirror",
                "switch mirror on",
                "switch the mirror on",
                "use mirror",
                "use the mirror",
                "let me see mirror",
                "let me see the mirror",
                "i want mirror",
                "i want the mirror",
                "i need mirror",
                "i need the mirror",
                "give me mirror",
                "give me the mirror");
    }

    private static String canonical(String transcript) {
        if (transcript == null) {
            return null;
        }
        String value = transcript.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        if (value.isEmpty()) {
            return null;
        }

        boolean changed;
        do {
            changed = false;
            String stripped = stripLeading(value,
                    "hey ", "hi ", "hello ", "ok ", "okay ", "please ",
                    "boop ", "hey boop ", "hi boop ", "hello boop ",
                    "can you ", "could you ", "would you ", "will you ");
            if (!stripped.equals(value)) {
                value = stripped;
                changed = true;
            }
        } while (changed && !value.isEmpty());

        while (value.endsWith(" please")) {
            value = value.substring(0, value.length() - " please".length()).trim();
        }
        return value.isEmpty() ? null : value;
    }

    private static String stripLeading(String value, String... prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return value.substring(prefix.length()).trim();
            }
        }
        return value;
    }

    private static boolean startsWithAny(String value, String... prefixes) {
        for (String prefix : prefixes) {
            if (value.equals(prefix) || value.startsWith(prefix + " ")) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWord(String value, String word) {
        return value.equals(word)
                || value.startsWith(word + " ")
                || value.endsWith(" " + word)
                || value.contains(" " + word + " ");
    }
}
