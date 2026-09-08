package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public final class FavouriteGrabSessionTest {
    @Test public void grabbedItemCanTravelAcrossWholeRowThenCommit() {
        FavouriteGrabSession session = FavouriteGrabSession.begin(
                List.of("a/.A", "b/.B", "c/.C", "d/.D"), "b/.B");

        assertTrue(session.move(1));
        assertTrue(session.move(1));
        assertEquals(List.of("a/.A", "c/.C", "d/.D", "b/.B"), session.current());
        assertEquals(3, session.index());
        assertEquals(session.current(), session.commit());
        assertFalse(session.move(1));
    }

    @Test public void cancelRestoresOriginalOrder() {
        FavouriteGrabSession session = FavouriteGrabSession.begin(
                List.of("a/.A", "b/.B", "c/.C"), "c/.C");
        assertTrue(session.move(-1));
        assertTrue(session.move(-1));
        assertEquals(List.of("c/.C", "a/.A", "b/.B"), session.current());
        assertEquals(List.of("a/.A", "b/.B", "c/.C"), session.cancel());
    }

    @Test public void missingItemCannotMove() {
        FavouriteGrabSession session = FavouriteGrabSession.begin(
                List.of("a/.A", "b/.B"), "missing/.M");
        assertEquals(-1, session.index());
        assertFalse(session.move(-1));
        assertFalse(session.move(1));
        assertEquals(List.of("a/.A", "b/.B"), session.commit());
    }
}
