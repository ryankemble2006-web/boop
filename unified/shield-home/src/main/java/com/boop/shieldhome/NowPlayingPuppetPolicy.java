package com.boop.shieldhome;

import android.media.session.PlaybackState;

/** Pure visibility/motion policy for the launcher-owned headphones BOOP layer. */
final class NowPlayingPuppetPolicy {
    enum Mode {
        HIDDEN,
        REST,
        GROOVE
    }

    private NowPlayingPuppetPolicy() { }

    static Mode mode(NowPlayingSnapshot snapshot) {
        if (snapshot == null || !NowPlayingSelectionPolicy.eligible(snapshot.playbackState())) {
            return Mode.HIDDEN;
        }
        return snapshot.playbackState() == PlaybackState.STATE_PLAYING
                ? Mode.GROOVE
                : Mode.REST;
    }
}
