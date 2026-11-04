package com.boop.alpha1;

final class BoopWakeRecognitionCapability {
    private BoopWakeRecognitionCapability() { }

    static boolean canAttempt(int sdkInt, boolean recognizerAvailable) {
        return sdkInt >= 33 && recognizerAvailable;
    }
}
