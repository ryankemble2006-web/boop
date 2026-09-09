package com.boop.alpha1;

import java.util.Locale;

final class BoopDevMenuIntent {
    private BoopDevMenuIntent() { }

    static boolean matches(String transcript) {
        if (transcript == null) return false;
        String normalized = transcript.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[\\p{Punct}\\s]+$", "").trim();
        normalized = normalized.replaceAll("\\s+", " ");
        return "dev menu".equals(normalized);
    }
}
