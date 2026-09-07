package com.boop.shieldoverlay;

import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.EYES;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_REST;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public final class DeezerPuppetPolicyTest {
    @Test
    public void exactAndroidPlaybackStateTableChoosesTheMode() {
        assertEquals(HEADPHONES_PLAYING, mode(3));

        assertEquals(HEADPHONES_REST, mode(2));
        assertEquals(HEADPHONES_REST, mode(4));
        assertEquals(HEADPHONES_REST, mode(5));
        assertEquals(HEADPHONES_REST, mode(6));
        assertEquals(HEADPHONES_REST, mode(8));
        assertEquals(HEADPHONES_REST, mode(9));
        assertEquals(HEADPHONES_REST, mode(10));
        assertEquals(HEADPHONES_REST, mode(11));

        assertEquals(EYES, mode(null));
        assertEquals(EYES, mode(0));
        assertEquals(EYES, mode(1));
        assertEquals(EYES, mode(7));
        assertEquals(EYES, mode(-1));
        assertEquals(EYES, mode(12));
        assertEquals(EYES, mode(Integer.MIN_VALUE));
        assertEquals(EYES, mode(Integer.MAX_VALUE));
    }

    @Test
    public void everyDisabledGateFallsBackToEyes() {
        assertEquals(EYES, DeezerPuppetPolicy.mode(false, true, true, 3));
        assertEquals(EYES, DeezerPuppetPolicy.mode(true, false, true, 3));
        assertEquals(EYES, DeezerPuppetPolicy.mode(true, true, false, 3));
        assertEquals(EYES, DeezerPuppetPolicy.mode(false, false, false, 3));
    }

    @Test
    public void selectionIgnoresNonDeezerAndIneligibleSessions() {
        List<DeezerPuppetPolicy.Session> sessions = Arrays.asList(
                session(1L, "com.spotify.music", 3),
                session(2L, "deezer.android.app.beta", 3),
                session(3L, "deezer.android.app", 7),
                session(4L, "deezer.android.app", 2));

        assertEquals(4L, DeezerPuppetPolicy.select(sessions, 0L));
        assertEquals(0L, DeezerPuppetPolicy.select(Collections.emptyList(), 4L));
    }

    @Test
    public void playingSessionWinsOverCurrentRestingSession() {
        List<DeezerPuppetPolicy.Session> sessions = Arrays.asList(
                session(10L, "deezer.android.app", 2),
                session(20L, "deezer.android.app", 3));

        assertEquals(20L, DeezerPuppetPolicy.select(sessions, 10L));
    }

    @Test
    public void currentEligibleSessionSurvivesListReordering() {
        DeezerPuppetPolicy.Session first = session(10L, "deezer.android.app", 2);
        DeezerPuppetPolicy.Session current = session(20L, "deezer.android.app", 4);

        assertEquals(20L, DeezerPuppetPolicy.select(Arrays.asList(first, current), 20L));
        assertEquals(20L, DeezerPuppetPolicy.select(Arrays.asList(current, first), 20L));
    }

    @Test
    public void firstEligibleSessionIsUsedWhenThereIsNoCurrentSession() {
        List<DeezerPuppetPolicy.Session> sessions = Arrays.asList(
                session(10L, "deezer.android.app", 4),
                session(20L, "deezer.android.app", 2));

        assertEquals(10L, DeezerPuppetPolicy.select(sessions, 99L));
    }

    @Test
    public void duplicateIdsDoNotDestabilizeAPlayingCurrentSession() {
        DeezerPuppetPolicy.Session currentRest = session(10L, "deezer.android.app", 2);
        DeezerPuppetPolicy.Session otherPlaying = session(20L, "deezer.android.app", 3);
        DeezerPuppetPolicy.Session currentPlaying = session(10L, "deezer.android.app", 3);
        DeezerPuppetPolicy.Session currentUnknown = session(10L, "deezer.android.app", 7);

        assertEquals(
                10L,
                DeezerPuppetPolicy.select(
                        Arrays.asList(
                                currentRest, otherPlaying, currentPlaying, currentUnknown),
                        10L));
        assertEquals(
                10L,
                DeezerPuppetPolicy.select(
                        Arrays.asList(
                                currentUnknown, currentPlaying, otherPlaying, currentRest),
                        10L));
    }

    @Test
    public void selectionMovesOnlyWhenCurrentEligibilityOrPriorityChanges() {
        assertEquals(
                20L,
                DeezerPuppetPolicy.select(
                        Arrays.asList(
                                session(10L, "deezer.android.app", 7),
                                session(20L, "deezer.android.app", 2)),
                        10L));
        assertEquals(
                20L,
                DeezerPuppetPolicy.select(
                        Arrays.asList(
                                session(10L, "deezer.android.app", 2),
                                session(20L, "deezer.android.app", 3)),
                        10L));
    }

    private static DeezerPuppetPolicy.Mode mode(Integer playbackState) {
        return DeezerPuppetPolicy.mode(true, true, true, playbackState);
    }

    private static DeezerPuppetPolicy.Session session(
            long id, String packageName, Integer playbackState) {
        return new DeezerPuppetPolicy.Session(id, packageName, playbackState);
    }
}
