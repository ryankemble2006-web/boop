package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.media.session.PlaybackState;
import org.junit.Test;

public final class NowPlayingPuppetPolicyTest {
    @Test public void noMediaHidesPuppet() {
        assertEquals(NowPlayingPuppetPolicy.Mode.HIDDEN, NowPlayingPuppetPolicy.mode(null));
    }

    @Test public void pausedMediaGetsUpsetWithTheDj() {
        assertEquals(
                "UPSET",
                NowPlayingPuppetPolicy.mode(snapshot(PlaybackState.STATE_PAUSED)).name());
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

    @Test public void playbackGrooveHasVisibleDanceSquashAndLean() {
        NowPlayingPuppetMotion.Pose beat = NowPlayingPuppetMotion.groove(450L);
        assertTrue(Math.abs(beat.rotationDegrees) > 2.5f);
        assertTrue(Math.abs(beat.scale - 1f) > 0.015f);
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
