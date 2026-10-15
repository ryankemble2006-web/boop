package com.boop.alpha1;

interface BoopSpeechBackend {
    interface Callback {
        void onDone();
        void onError(Throwable error);
        /** Cancellation is terminal, but must not select an interrupted voice demo. */
        default void onCancelled() { onDone(); }
    }

    /**
     * Starts an utterance. If this returns false, no callback will be delivered.
     * Once it returns true, exactly one terminal callback must be delivered.
     */
    boolean speak(String text, int speakerId, float pitch, float rate, Callback callback);

    void stop();
    void release();
}
