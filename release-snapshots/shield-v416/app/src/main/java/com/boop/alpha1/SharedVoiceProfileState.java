package com.boop.alpha1;

/** Main-thread state: local voice controls remain usable; HA wins only while connected. */
final class SharedVoiceProfileState {
    private SharedVoiceProfileProtocol.Profile local, shared;
    private boolean connected;
    private SharedVoiceProfileProtocol.Profile queued, inFlight;

    SharedVoiceProfileState(SharedVoiceProfileProtocol.Profile profile) {
        if (profile == null) throw new IllegalArgumentException("Missing voice profile");
        local = shared = profile;
    }

    SharedVoiceProfileProtocol.Profile localProfile() { return local; }
    boolean isConnected() { return connected; }

    void connected(SharedVoiceProfileProtocol.Profile profile) {
        if (profile == null) throw new IllegalArgumentException("Missing shared voice profile");
        connected = true;
        local = shared = profile;
        queued = inFlight = null;
    }

    void disconnected() {
        connected = false;
        queued = inFlight = null;
    }

    void localChanged(SharedVoiceProfileProtocol.Profile profile) {
        if (profile == null) throw new IllegalArgumentException("Missing local voice profile");
        if (local.equals(profile)) return;
        local = profile;
        if (connected) queued = inFlight == null && profile.equals(shared) ? null : profile;
    }

    SharedVoiceProfileProtocol.Profile nextWrite() {
        if (!connected || inFlight != null || queued == null) return null;
        if (queued.equals(shared)) { queued = null; return null; }
        inFlight = queued;
        queued = null;
        return inFlight;
    }

    void remoteChanged(SharedVoiceProfileProtocol.Profile profile) {
        if (profile == null || !connected) return;
        shared = profile;
        if (queued == null && inFlight == null) local = profile;
    }

    void writeConfirmed(SharedVoiceProfileProtocol.Profile accepted) {
        if (accepted == null || !connected || inFlight == null) return;
        shared = accepted;
        inFlight = null;
        if (queued == null) local = accepted;
        else if (queued.equals(accepted)) { local = accepted; queued = null; }
    }
}
