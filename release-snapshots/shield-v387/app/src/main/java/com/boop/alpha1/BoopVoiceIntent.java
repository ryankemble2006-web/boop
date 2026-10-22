package com.boop.alpha1;

import java.util.Locale;

final class BoopVoiceIntent {
    private BoopVoiceIntent() { }

    static boolean matches(String text) {
        if (text == null) {
            return false;
        }

        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return false;
        }

        if (normalized.contains("sound different") || normalized.contains("sounds different")) {
            return true;
        }

        boolean hasVoice = containsWord(normalized, "voice") || containsWord(normalized, "voices");
        if (!hasVoice) {
            return false;
        }

        boolean explicitlyDifferent = normalized.contains("different voice")
                || normalized.contains("another voice")
                || normalized.contains("new voice")
                || normalized.contains("other voice")
                || normalized.contains("different voices")
                || normalized.contains("another voices");

        if (containsAnyWord(normalized, "volume", "louder", "quieter") && !explicitlyDifferent) {
            return false;
        }

        if (explicitlyDifferent) {
            return true;
        }

        if (containsAnyWord(normalized, "change", "switch", "swap") && hasVoice) {
            return true;
        }

        return (normalized.contains("dont like") || normalized.contains("do not like"))
                && (normalized.contains("your voice")
                || normalized.contains("this voice")
                || normalized.contains("the voice"));
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replace("'", "")
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private static boolean containsAnyWord(String text, String... words) {
        for (String word : words) {
            if (containsWord(text, word)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWord(String text, String word) {
        return (" " + text + " ").contains(" " + word + " ");
    }
}
