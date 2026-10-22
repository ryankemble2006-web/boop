package com.boop.alpha1;

import java.util.Locale;
import java.util.Set;

final class LightColourCommandParser {
    private static final Set<String> COLOURS = Set.of(
            "red", "orange", "yellow", "green", "blue", "purple", "pink",
            "cyan", "magenta", "white", "violet", "indigo", "turquoise",
            "teal", "lime", "gold", "coral");

    private LightColourCommandParser() { }

    static String parseColour(String text) {
        if (text == null) {
            return null;
        }

        String normalized = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty() || !containsLightWord(normalized)) {
            return null;
        }

        boolean imperative = normalized.startsWith("lights ")
                || normalized.startsWith("light ")
                || normalized.startsWith("make ")
                || normalized.startsWith("set ")
                || normalized.startsWith("turn ")
                || normalized.startsWith("change ");
        if (!imperative) {
            return null;
        }

        if (normalized.matches(".*\\blights?\\s+(on|off)\\b.*")
                || normalized.matches(".*\\bturn\\s+(on|off)\\b.*")) {
            return null;
        }

        String[] words = normalized.split(" ");
        for (int i = words.length - 1; i >= 0; i--) {
            if (COLOURS.contains(words[i])) {
                return words[i];
            }
        }
        return null;
    }

    private static boolean containsLightWord(String text) {
        return text.matches(".*\\blights?\\b.*");
    }
}
