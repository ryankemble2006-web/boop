package com.boop.alpha1;

import java.util.Locale;

final class BoopVoiceSettingsIntent {
    private BoopVoiceSettingsIntent() { }

    static boolean matches(String text) {
        if (text == null) {
            return false;
        }

        String normalized = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return false;
        }

        return normalized.contains("voice settings")
                || normalized.contains("voice controls")
                || normalized.contains("voice sliders")
                || normalized.contains("adjust your voice")
                || normalized.contains("tune your voice")
                || normalized.contains("adjust your pitch")
                || normalized.contains("change your pitch")
                || normalized.contains("adjust your cadence")
                || normalized.contains("change your cadence")
                || normalized.contains("adjust your speech speed")
                || normalized.contains("change your speech speed");
    }
}
