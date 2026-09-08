package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public final class NowPlayingSelectionPolicyTest {
    private static NowPlayingSelectionPolicy.Candidate candidate(
            long id, String packageName, int playbackState) {
        return new NowPlayingSelectionPolicy.Candidate(id, packageName, playbackState);
    }

    @Test public void automaticPrefersPlayingSession() {
        List<NowPlayingSelectionPolicy.Candidate> sessions = List.of(
                candidate(1L, "paused.player", 2),
                candidate(2L, "playing.player", 3));

        assertEquals(2L, NowPlayingSelectionPolicy.select(sessions, 0L, ""));
    }

    @Test public void automaticKeepsCurrentPausedSessionWhenNothingIsPlaying() {
        List<NowPlayingSelectionPolicy.Candidate> sessions = List.of(
                candidate(1L, "first.player", 2),
                candidate(2L, "current.player", 2));

        assertEquals(2L, NowPlayingSelectionPolicy.select(sessions, 2L, ""));
    }

    @Test public void preferredPlayerWinsWhenEligibleEvenIfAnotherSessionIsPlaying() {
        List<NowPlayingSelectionPolicy.Candidate> sessions = List.of(
                candidate(1L, "preferred.player", 2),
                candidate(2L, "other.player", 3));

        assertEquals(1L, NowPlayingSelectionPolicy.select(
                sessions, 0L, "preferred.player"));
    }

    @Test public void absentPreferredPlayerFallsBackToAutomatic() {
        List<NowPlayingSelectionPolicy.Candidate> sessions = List.of(
                candidate(1L, "paused.player", 2),
                candidate(2L, "playing.player", 3));

        assertEquals(2L, NowPlayingSelectionPolicy.select(
                sessions, 0L, "missing.player"));
    }

    @Test public void stoppedAndNoneStatesAreNotEligible() {
        assertFalse(NowPlayingSelectionPolicy.eligible(0));
        assertFalse(NowPlayingSelectionPolicy.eligible(1));
        assertTrue(NowPlayingSelectionPolicy.eligible(2));
        assertTrue(NowPlayingSelectionPolicy.eligible(3));
        assertTrue(NowPlayingSelectionPolicy.eligible(6));
    }
}
