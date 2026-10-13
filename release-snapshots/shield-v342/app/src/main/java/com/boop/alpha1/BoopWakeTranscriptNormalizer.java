package com.boop.alpha1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BoopWakeTranscriptNormalizer {
    private BoopWakeTranscriptNormalizer() { }

    static String stripLeadingWakeWord(String text) {
        if (text == null) {
            return "";
        }
        String transcript = text.trim();
        String stripped = stripLeadingNaturalWakeCall(transcript, BoopWakeName.DEFAULT);
        return stripped == null ? transcript : stripped;
    }

    static String stripLeadingWakeWord(String text, String selectedName) {
        if (text == null || BoopWakeName.isDefault(selectedName)) {
            return stripLeadingWakeWord(text);
        }
        String transcript = text.trim();
        String stripped = stripLeadingNaturalWakeCall(transcript, selectedName);
        if (stripped != null) return stripped;
        // Keep BOOP as the permanent fallback even after a custom name is trained.
        return stripLeadingWakeWord(text);
    }

    private static String stripLeadingNaturalWakeCall(String transcript, String selectedName) {
        List<String> phrases = new ArrayList<>(BoopWakeKeywordBuilder.naturalPhrases(selectedName));
        // Match the whole call before the bare name ("BOOP WAKE UP", not just "BOOP").
        phrases.sort((left, right) -> Integer.compare(right.length(), left.length()));
        for (String phrase : phrases) {
            StringBuilder expression = new StringBuilder("^\\s*");
            for (String word : phrase.split(" ")) {
                if (expression.length() > 4) expression.append("[\\s\\p{P}]+");
                expression.append(Pattern.quote(word));
            }
            expression.append("(?![\\p{L}\\p{N}'’])[\\s\\p{P}]*");
            Matcher call = Pattern.compile(expression.toString(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(transcript);
            if (call.find()) return transcript.substring(call.end()).trim();
        }
        return null;
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
