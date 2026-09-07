package com.boop.alpha1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
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

    static String stripLeadingWakeWord(String text, String selectedName) {
        if (text == null || BoopWakeName.isDefault(selectedName)) {
            return stripLeadingWakeWord(text);
        }
        String transcript = text.trim();
        List<String> phrases = new ArrayList<>(BoopWakeKeywordBuilder.naturalPhrases(selectedName));
        // Match the whole call before the bare name ("Steve wake up", not just "Steve").
        phrases.sort((left, right) -> Integer.compare(right.length(), left.length()));
        for (String phrase : phrases) {
            StringBuilder expression = new StringBuilder("^");
            for (String word : phrase.split(" ")) {
                if (expression.length() > 1) expression.append("[\\s\\p{P}]+");
                expression.append(Pattern.quote(word));
            }
            expression.append("(?![\\p{L}\\p{N}'’])[\\s\\p{P}]*");
            Matcher call = Pattern.compile(expression.toString(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(transcript);
            if (call.find()) return transcript.substring(call.end()).trim();
        }
        // Keep the established BOOP fallback and non-addressed commands unchanged.
        return stripLeadingWakeWord(text);
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
