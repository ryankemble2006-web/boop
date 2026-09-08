package com.boop.alpha1;

/** Defines which audio history is handed from wake detection into command ASR. */
final class BoopWakeCommandAudioPolicy {
    private BoopWakeCommandAudioPolicy() { }

    static short[] recognizerPrelude(short[] wakeDetectionHistory) {
        // The command recognizer must start at the wake boundary. Feeding the
        // wake phrase itself makes Android finish recognition before the command.
        return new short[0];
    }
}
