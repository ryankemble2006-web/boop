package com.boop.alpha1;

import java.util.regex.Pattern;

final class BoopWakeTranscriptNormalizer {
    private static final Pattern LEADING_BOOP =
            Pattern.compile("(?i)^\\s*boop\\b[\\s,;:!?.-]*");

    private BoopWakeTranscriptNormalizer() { }

    static String stripLeadingWakeWord(String text) {
        if (text == null) {
            return "";
        }
        return LEADING_BOOP.matcher(text).replaceFirst("").trim();
    }
}
