package com.boop.shared;

/** Requests native TV playback; a successful dispatch is not playback confirmation. */
public final class DeezerPlaybackSequence {
    private DeezerPlaybackSequence() { }
    public interface Gateway {
        boolean current();
        boolean deezerActive() throws Exception;
        void openArtist() throws Exception;
        void awaitArtist() throws Exception;
        void pause() throws Exception;
        void awaitPause() throws Exception;
        void select() throws Exception;
    }
    public static boolean request(Gateway target) throws Exception {
        if (!target.current()) return false;
        target.openArtist();
        target.awaitArtist();
        if (!target.current() || !target.deezerActive()) return false;
        // The artist page's focused button toggles. Establish paused state first.
        target.pause();
        target.awaitPause();
        if (!target.current() || !target.deezerActive()) return false;
        target.select();
        return true;
    }
}
