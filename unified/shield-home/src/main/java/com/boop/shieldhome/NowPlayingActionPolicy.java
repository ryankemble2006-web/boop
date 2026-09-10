package com.boop.shieldhome;

import android.media.session.PlaybackState;

/** Pure capability checks for remote Now Playing controls. */
final class NowPlayingActionPolicy {
    private NowPlayingActionPolicy() { }

    static boolean canPrevious(long actions) {
        return has(actions, PlaybackState.ACTION_SKIP_TO_PREVIOUS);
    }

    static boolean canRewind(long actions) {
        return has(actions, PlaybackState.ACTION_REWIND);
    }

    static boolean canPlayPause(long actions, int playbackState) {
        if (playbackState == PlaybackState.STATE_PLAYING) {
            return has(actions, PlaybackState.ACTION_PAUSE)
                    || has(actions, PlaybackState.ACTION_PLAY_PAUSE);
        }
        return has(actions, PlaybackState.ACTION_PLAY)
                || has(actions, PlaybackState.ACTION_PLAY_PAUSE);
    }

    static boolean canFastForward(long actions) {
        return has(actions, PlaybackState.ACTION_FAST_FORWARD);
    }

    static boolean canNext(long actions) {
        return has(actions, PlaybackState.ACTION_SKIP_TO_NEXT);
    }

    private static boolean has(long actions, long action) {
        return (actions & action) != 0L;
    }
}
