package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import android.media.session.PlaybackState;
import org.junit.Test;

public final class NowPlayingPuppetPolicyTest {
    @Test public void noMediaHidesPuppet() {
        assertEquals(NowPlayingPuppetPolicy.Mode.HIDDEN, NowPlayingPuppetPolicy.mode(null));
    }

    @Test public void pausedMediaKeepsPuppetResting() {
        assertEquals(
                NowPlayingPuppetPolicy.Mode.REST,
                NowPlayingPuppetPolicy.mode(snapshot(PlaybackState.STATE_PAUSED)));
    }

    @Test public void playingMediaGrooves() {
        assertEquals(
                NowPlayingPuppetPolicy.Mode.GROOVE,
                NowPlayingPuppetPolicy.mode(snapshot(PlaybackState.STATE_PLAYING)));
    }

    @Test public void activeNonPlayingStatesRestRatherThanDisappear() {
        assertEquals(
                NowPlayingPuppetPolicy.Mode.REST,
                NowPlayingPuppetPolicy.mode(snapshot(PlaybackState.STATE_BUFFERING)));
    }

    private static NowPlayingSnapshot snapshot(int state) {
        return new NowPlayingSnapshot(
                1L,
                "player.example",
                "Track",
                "Artist",
                state,
                0L,
                0L,
                1000L,
                1f,
                0L,
                null);
    }
}
