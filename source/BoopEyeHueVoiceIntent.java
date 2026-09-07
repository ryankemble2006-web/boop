package com.boop.alpha1;

import java.util.Locale;

final class BoopEyeHueVoiceIntent {
    private BoopEyeHueVoiceIntent() { }

    static boolean matches(String transcript) {
        if (transcript == null) return false;
        String value = transcript.trim().toLowerCase(Locale.UK)
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return value.equals("change eye colour")
                || value.equals("change eye color")
                || value.equals("change eyes colour")
                || value.equals("change eyes color")
                || value.equals("change my eye colour")
                || value.equals("change my eye color");
    }
}
