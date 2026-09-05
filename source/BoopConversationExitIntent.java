package com.boop.alpha1;

import java.util.Locale;
import java.util.Set;

final class BoopConversationExitIntent {
    private static final Set<String> GRATITUDE = Set.of(
            "thanks", "thank you", "thankyou", "cheers", "ta", "much appreciated",
            "appreciate it", "many thanks", "thanks very much", "thank you very much",
            "lovely thanks", "lovely thank you", "perfect thanks", "great thanks",
            "brilliant thanks");

    private static final Set<String> SLEEP = Set.of(
            "goodnight", "good night", "night night", "go to sleep", "go back to sleep",
            "back to sleep", "back to sleep now", "sleep now", "bedtime", "time for bed",
            "rest now");

    private static final Set<String> GOODBYE = Set.of(
            "bye", "goodbye", "bye bye", "see you", "see you later");

    private static final Set<String> DISMISSAL = Set.of(
            "go away", "leave me alone", "leave me be", "off you go", "you can go",
            "you can go now", "thats all", "that is all", "we are done", "were done",
            "stop listening", "shoo", "fuck off", "piss off", "bugger off");

    private BoopConversationExitIntent() { }

    static String replyFor(String transcript) {
        String normalized = normalize(transcript);
        if (normalized.isEmpty()) {
            return null;
        }

        normalized = stripBoop(normalized);

        if (GRATITUDE.contains(normalized)) {
            return "You're welcome.";
        }
        if (SLEEP.contains(normalized)) {
            return "Goodnight.";
        }
        if (GOODBYE.contains(normalized)) {
            return "Bye.";
        }
        if (DISMISSAL.contains(normalized)) {
            return "Okay.";
        }
        return null;
    }

    private static String normalize(String transcript) {
        if (transcript == null) {
            return "";
        }
        return transcript.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String stripBoop(String normalized) {
        if (normalized.equals("boop")) {
            return normalized;
        }
        if (normalized.startsWith("boop ")) {
            normalized = normalized.substring(5).trim();
        }
        if (normalized.endsWith(" boop")) {
            normalized = normalized.substring(0, normalized.length() - 5).trim();
        }
        return normalized;
    }
}
