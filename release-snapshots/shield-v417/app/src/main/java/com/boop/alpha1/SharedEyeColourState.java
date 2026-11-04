package com.boop.alpha1;

/** Main-thread state: local slider remains usable; HA is authoritative only when connected. */
final class SharedEyeColourState {
    private int localHue, sharedHue;
    private boolean connected;
    private Integer queued, inFlight;
    SharedEyeColourState(int hue) {
        SharedEyeColourProtocol.requireHue(hue);
        localHue = sharedHue = hue;
    }
    int localHue() { return localHue; }
    boolean isConnected() { return connected; }
    void connected(int shared) {
        SharedEyeColourProtocol.requireHue(shared);
        connected = true;
        localHue = sharedHue = shared;
        queued = inFlight = null;
    }
    void disconnected() {
        connected = false;
        queued = inFlight = null;
    }
    void localChanged(int hue) {
        SharedEyeColourProtocol.requireHue(hue);
        if (localHue == hue) return;
        localHue = hue;
        if (connected) queued = inFlight == null && hue == sharedHue ? null : hue;
    }
    Integer nextWrite() {
        if (!connected || inFlight != null || queued == null) return null;
        if (queued == sharedHue) { queued = null; return null; }
        inFlight = queued;
        queued = null;
        return inFlight;
    }
    void remoteChanged(int hue) {
        SharedEyeColourProtocol.requireHue(hue);
        if (!connected) return;
        sharedHue = hue;
        if (queued == null && inFlight == null) localHue = hue;
    }
    /** Confirm with a fresh HA read, never just the colour sent or a transport ACK. */
    void writeConfirmed(int acceptedHue) {
        SharedEyeColourProtocol.requireHue(acceptedHue);
        if (!connected || inFlight == null) return;
        sharedHue = acceptedHue;
        inFlight = null;
        if (queued == null) localHue = acceptedHue;
        else if (queued == acceptedHue) { localHue = acceptedHue; queued = null; }
    }
}
