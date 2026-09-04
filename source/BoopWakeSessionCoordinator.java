package com.boop.alpha1;

final class BoopWakeSessionCoordinator {
    interface Engine {
        boolean arm();
        void suspendAll();
        void shutdown();
    }

    private final Engine engine;
    private final BoopWakeSessionState state = new BoopWakeSessionState();
    private boolean engineArmed;
    private boolean shutdown;

    BoopWakeSessionCoordinator(Engine engine) {
        this.engine = engine;
    }

    BoopWakeSessionState.State state() {
        return state.state();
    }

    boolean wakeFailed() {
        return state.wakeFailed();
    }

    void beginForegroundSession() {
        if (shutdown) return;
        state.beginForegroundSession();
        engineArmed = false;
        syncEngine();
    }

    void endForegroundSession() {
        if (shutdown) return;
        state.endForegroundSession();
        syncEngine();
    }

    void setMicrophonePermission(boolean granted) {
        if (shutdown) return;
        state.setMicrophonePermission(granted);
        syncEngine();
    }

    void setRecognitionSupported(boolean supported) {
        if (shutdown) return;
        state.setRecognitionSupported(supported);
        syncEngine();
    }

    void setVoiceSettingsOpen(boolean open) {
        if (shutdown) return;
        state.setVoiceSettingsOpen(open);
        syncEngine();
    }

    void onTapStarted() {
        if (shutdown) return;
        state.setTapListening(true);
        syncEngine();
    }

    void onTapFinished() {
        if (shutdown) return;
        state.setTapListening(false);
        syncEngine();
    }

    void onTtsStarting() {
        if (shutdown) return;
        state.setTtsSpeaking(true);
        syncEngine();
    }

    void onTtsFinished() {
        if (shutdown) return;
        state.setTtsSpeaking(false);
        syncEngine();
    }

    boolean onWakeDetected(long detectedAtMs) {
        if (shutdown) return false;
        boolean accepted = state.acceptWake(detectedAtMs);
        syncEngine();
        return accepted;
    }

    void markWakeProcessing() {
        if (shutdown) return;
        state.markProcessing();
        syncEngine();
    }

    void cancelWakeCapture() {
        if (shutdown) return;
        state.cancelWakeCapture();
        syncEngine();
    }

    void finishWakeProcessing() {
        if (shutdown) return;
        state.finishProcessing();
        syncEngine();
    }

    void failWakeSession() {
        if (shutdown) return;
        state.failWakeSession();
        syncEngine();
    }

    void shutdown() {
        if (shutdown) return;
        shutdown = true;
        engineArmed = false;
        engine.shutdown();
    }

    private void syncEngine() {
        switch (state.state()) {
            case ARMED:
                if (engineArmed) {
                    return;
                }
                if (engine.arm()) {
                    engineArmed = true;
                } else {
                    state.failWakeSession();
                    engine.suspendAll();
                    engineArmed = false;
                }
                return;
            case WAKE_CAPTURE:
                return;
            case DISARMED:
            case PROCESSING:
            case SPEAKING:
            default:
                if (engineArmed) {
                    engine.suspendAll();
                    engineArmed = false;
                }
        }
    }
}
