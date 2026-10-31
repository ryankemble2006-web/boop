package com.boop.alpha1;

/** Defines which audio history is handed from wake detection into command ASR. */
final class BoopWakeCommandAudioPolicy {
    // Wake detection consumes 1,600 samples at a time at 16 kHz. Keep only that
    // final 100 ms block so speech that begins in the detector's terminal block
    // is not chopped off. Never feed the old one-second wake-history ring back
    // into Android command ASR.
    private static final int COMMAND_BRIDGE_SAMPLES = 1_600;

    private BoopWakeCommandAudioPolicy() { }

    static short[] recognizerPrelude(short[] wakeDetectionHistory) {
        if (wakeDetectionHistory == null || wakeDetectionHistory.length == 0) {
            return new short[0];
        }
        int count = Math.min(COMMAND_BRIDGE_SAMPLES, wakeDetectionHistory.length);
        short[] bridge = new short[count];
        System.arraycopy(
                wakeDetectionHistory,
                wakeDetectionHistory.length - count,
                bridge,
                0,
                count);
        return bridge;
    }
}
