package com.boop.alpha1;

import java.util.Locale;

final class BoopEyeHueVoiceIntent {
    private BoopEyeHueVoiceIntent() { }

    static boolean matches(String transcript) {
        if (transcript == null) {
            return false;
        }

        String value = transcript.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (value.isEmpty()) {
            return false;
        }

        // Mirror the tolerant local matching style used by Voice Settings.
        // This intentionally accepts wake-word prefixes such as
        // "boop change eye colour" and harmless natural wording variations.
        boolean eyeConcept = value.contains("eye colour")
                || value.contains("eye color")
                || value.contains("eyes colour")
                || value.contains("eyes color")
                || value.contains("eye hue")
                || value.contains("eyes hue")
                // Common recognizer homophone when "eye" is heard as "I".
                || value.contains("i colour")
                || value.contains("i color");

        if (!eyeConcept) {
            return false;
        }

        return value.contains("change")
                || value.contains("adjust")
                || value.contains("set")
                || value.contains("choose")
                || value.contains("pick")
                || value.contains("settings")
                || value.equals("eye colour")
                || value.equals("eye color")
                || value.equals("eye hue")
                || value.equals("eyes colour")
                || value.equals("eyes color")
                || value.equals("eyes hue");
    }
}
