package com.boop.alpha1;

final class BoopWakeSessionState {
    enum State {
        DISARMED,
        ARMED,
        WAKE_CAPTURE,
        PROCESSING,
        SPEAKING
    }

    static final long COMMAND_WINDOW_MS = 3_000L;

    private State state = State.DISARMED;
    private boolean foreground;
    private boolean microphonePermission;
    private boolean recognitionSupported;
    private boolean voiceSettingsOpen;
    private boolean tapListening;
    private boolean ttsSpeaking;
    private boolean wakeFailed;
    private long commandDeadlineMs = -1L;

    State state() {
        return state;
    }

    long commandDeadlineMs() {
        return commandDeadlineMs;
    }

    boolean wakeFailed() {
        return wakeFailed;
    }

    void beginForegroundSession() {
        foreground = true;
        wakeFailed = false;
        recognitionSupported = false;
        commandDeadlineMs = -1L;
        state = State.DISARMED;
        reevaluate();
    }

    void endForegroundSession() {
        foreground = false;
        commandDeadlineMs = -1L;
        state = State.DISARMED;
    }

    void setMicrophonePermission(boolean granted) {
        microphonePermission = granted;
        reevaluate();
    }

    void setRecognitionSupported(boolean supported) {
        recognitionSupported = supported;
        reevaluate();
    }

    void setVoiceSettingsOpen(boolean open) {
        voiceSettingsOpen = open;
        reevaluate();
    }

    void setTapListening(boolean listening) {
        tapListening = listening;
        reevaluate();
    }

    void setTtsSpeaking(boolean speaking) {
        ttsSpeaking = speaking;
        reevaluate();
    }

    boolean acceptWake(long nowMs) {
        if (state != State.ARMED) {
            return false;
        }
        commandDeadlineMs = nowMs + COMMAND_WINDOW_MS;
        state = State.WAKE_CAPTURE;
        return true;
    }

    void markProcessing() {
        if (state == State.WAKE_CAPTURE) {
            state = State.PROCESSING;
        }
    }

    void finishProcessing() {
        if (state != State.PROCESSING) {
            return;
        }
        commandDeadlineMs = -1L;
        state = State.DISARMED;
        reevaluate();
    }

    void cancelWakeCapture() {
        if (state != State.WAKE_CAPTURE) {
            return;
        }
        commandDeadlineMs = -1L;
        state = State.DISARMED;
        reevaluate();
    }

    void failWakeSession() {
        wakeFailed = true;
        commandDeadlineMs = -1L;
        state = State.DISARMED;
    }

    private void reevaluate() {
        if (wakeFailed
                || !foreground
                || !microphonePermission
                || !recognitionSupported
                || voiceSettingsOpen
                || tapListening) {
            state = State.DISARMED;
            return;
        }

        if (ttsSpeaking) {
            state = State.SPEAKING;
            return;
        }

        if (state != State.WAKE_CAPTURE && state != State.PROCESSING) {
            state = State.ARMED;
        }
    }
}
