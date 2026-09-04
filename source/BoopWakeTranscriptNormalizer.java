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

final class BoopWakeTranscriptAccumulator {
    private String latestPartial;

    void rememberPartial(String transcript) {
        String cleaned = clean(transcript);
        if (cleaned != null) {
            latestPartial = cleaned;
        }
    }

    String chooseFinal(String finalTranscript) {
        String cleanedFinal = clean(finalTranscript);
        return cleanedFinal != null ? cleanedFinal : latestPartial;
    }

    void reset() {
        latestPartial = null;
    }

    private static String clean(String transcript) {
        if (transcript == null) {
            return null;
        }
        String cleaned = transcript.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
