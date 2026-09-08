package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.media.session.PlaybackState;
import org.junit.Test;

public final class NowPlayingActionPolicyTest {
    @Test public void individualTransportActionsFollowAdvertisedBits() {
        long actions = PlaybackState.ACTION_SKIP_TO_PREVIOUS
                | PlaybackState.ACTION_REWIND
                | PlaybackState.ACTION_FAST_FORWARD
                | PlaybackState.ACTION_SKIP_TO_NEXT;

        assertTrue(NowPlayingActionPolicy.canPrevious(actions));
        assertTrue(NowPlayingActionPolicy.canRewind(actions));
        assertTrue(NowPlayingActionPolicy.canFastForward(actions));
        assertTrue(NowPlayingActionPolicy.canNext(actions));
        assertFalse(NowPlayingActionPolicy.canPlayPause(actions, 3));
    }

    @Test public void playingRequiresPauseOrPlayPauseCapability() {
        assertTrue(NowPlayingActionPolicy.canPlayPause(
                PlaybackState.ACTION_PAUSE, PlaybackState.STATE_PLAYING));
        assertTrue(NowPlayingActionPolicy.canPlayPause(
                PlaybackState.ACTION_PLAY_PAUSE, PlaybackState.STATE_PLAYING));
        assertFalse(NowPlayingActionPolicy.canPlayPause(
                PlaybackState.ACTION_PLAY, PlaybackState.STATE_PLAYING));
    }

    @Test public void pausedRequiresPlayOrPlayPauseCapability() {
        assertTrue(NowPlayingActionPolicy.canPlayPause(
                PlaybackState.ACTION_PLAY, PlaybackState.STATE_PAUSED));
        assertTrue(NowPlayingActionPolicy.canPlayPause(
                PlaybackState.ACTION_PLAY_PAUSE, PlaybackState.STATE_PAUSED));
        assertFalse(NowPlayingActionPolicy.canPlayPause(
                PlaybackState.ACTION_PAUSE, PlaybackState.STATE_PAUSED));
    }
}
