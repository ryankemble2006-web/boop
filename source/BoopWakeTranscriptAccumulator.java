package com.boop.alpha1;

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
