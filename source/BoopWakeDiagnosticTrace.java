package com.boop.alpha1;

/** In-memory, local-only trace of Android's post-wake SpeechRecognizer callbacks. */
final class BoopWakeDiagnosticTrace {
    private final long startedAtMs;
    private long readyAtMs = -1L;
    private long beginAtMs = -1L;
    private long endAtMs = -1L;
    private long terminalAtMs = -1L;
    private String partialText;
    private String finalText;
    private boolean resultReceived;
    private boolean errorReceived;
    private int errorCode;

    BoopWakeDiagnosticTrace(long startedAtMs) {
        this.startedAtMs = startedAtMs;
    }

    void ready(long atMs) {
        if (readyAtMs < 0L) readyAtMs = atMs;
    }

    void begin(long atMs) {
        if (beginAtMs < 0L) beginAtMs = atMs;
    }

    void partial(String text, long atMs) {
        String cleaned = clean(text);
        if (cleaned != null) partialText = cleaned;
    }

    void end(long atMs) {
        if (endAtMs < 0L) endAtMs = atMs;
    }

    void result(String text, long atMs) {
        if (terminal()) return;
        resultReceived = true;
        finalText = clean(text);
        terminalAtMs = atMs;
    }

    void error(int code, long atMs) {
        if (terminal()) return;
        errorReceived = true;
        errorCode = code;
        terminalAtMs = atMs;
    }

    boolean terminal() {
        return resultReceived || errorReceived;
    }

    String summary(long nowMs) {
        long elapsedMs = Math.max(0L, (terminal() ? terminalAtMs : nowMs) - startedAtMs);
        String state;
        if (errorReceived) {
            state = "ERROR " + errorCode;
        } else if (resultReceived) {
            state = "RESULT";
        } else {
            state = "PENDING";
        }
        return "WAKE ASR " + state
                + " +" + elapsedMs + "ms"
                + " ready=" + elapsed(readyAtMs)
                + " begin=" + elapsed(beginAtMs)
                + " end=" + elapsed(endAtMs)
                + " partial=" + quoted(partialText)
                + " final=" + quoted(finalText);
    }

    private String elapsed(long atMs) {
        return atMs < 0L ? "-" : Long.toString(Math.max(0L, atMs - startedAtMs));
    }

    private static String quoted(String value) {
        return value == null ? "-" : "\"" + value + "\"";
    }

    private static String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim().replaceAll("\\s+", " ").replace('"', '\'');
        return cleaned.isEmpty() ? null : cleaned;
    }
}
