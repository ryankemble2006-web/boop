package com.boop.alpha1;

interface BoopSpeechBackend {
    interface Callback {
        void onDone();
        void onError(Throwable error);
    }

    /**
     * Starts an utterance. If this returns false, no callback will be delivered.
     * Once it returns true, exactly one terminal callback must be delivered.
     */
    boolean speak(String text, int speakerId, float pitch, float rate, Callback callback);

    void stop();
    void release();
}
