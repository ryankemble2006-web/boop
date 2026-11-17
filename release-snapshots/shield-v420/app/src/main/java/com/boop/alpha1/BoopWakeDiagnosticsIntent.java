package com.boop.alpha1;

import java.util.Locale;

final class BoopWakeDiagnosticsIntent {
    private BoopWakeDiagnosticsIntent() { }

    static boolean matches(String transcript) {
        if (transcript == null) return false;
        String normalized = transcript
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return "show diagnostics".equals(normalized)
                || "show diagnostic".equals(normalized);
    }
}
