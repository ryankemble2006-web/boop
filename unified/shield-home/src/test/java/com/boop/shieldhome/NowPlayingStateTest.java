package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class NowPlayingStateTest {
    private static NowPlayingSnapshot snapshot(long id, String title, int state) {
        return new NowPlayingSnapshot(
                id,
                "player.pkg",
                title,
                "Artist",
                state,
                0L,
                1234L,
                5000L,
                1f,
                100L,
                null);
    }

    @Test public void subscriberImmediatelyReceivesCurrentAndCanUnsubscribe() {
        NowPlayingState bus = new NowPlayingState();
        List<NowPlayingSnapshot> seen = new ArrayList<>();

        Runnable unsubscribe = bus.subscribe(seen::add);
        assertEquals(1, seen.size());
        assertNull(seen.get(0));

        NowPlayingSnapshot first = snapshot(1L, "First", 3);
        bus.update(first);
        assertEquals(2, seen.size());
        assertNull(seen.get(0));
        assertEquals(first, seen.get(1));

        unsubscribe.run();
        bus.update(snapshot(2L, "Second", 2));
        assertEquals(2, seen.size());
        assertNull(seen.get(0));
        assertEquals(first, seen.get(1));
    }

    @Test public void equalSnapshotIsNotRepublished() {
        NowPlayingState bus = new NowPlayingState();
        List<NowPlayingSnapshot> seen = new ArrayList<>();
        bus.subscribe(seen::add);

        bus.update(snapshot(1L, "Same", 3));
        bus.update(snapshot(1L, "Same", 3));

        assertEquals(2, seen.size());
    }
}
