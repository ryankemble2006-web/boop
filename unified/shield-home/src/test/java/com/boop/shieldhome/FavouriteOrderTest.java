package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class FavouriteOrderTest {
    @Test public void reconcileDropsStaleAndPreservesSavedOrder() {
        List<TvAppEntry> installed = List.of(
                new TvAppEntry("a/.A", "a", "A"),
                new TvAppEntry("b/.B", "b", "B"));

        assertEquals(
                List.of("b/.B", "a/.A"),
                FavouriteOrder.reconcile(
                        List.of("b/.B", "gone/.Gone", "a/.A"), installed));
    }

    @Test public void addDoesNotDuplicateExistingFavourite() {
        assertEquals(List.of("a", "b"), FavouriteOrder.add(List.of("a", "b"), "b"));
    }

    @Test public void removeAndMoveAreStable() {
        assertEquals(List.of("a", "c"), FavouriteOrder.remove(List.of("a", "b", "c"), "b"));
        assertEquals(List.of("b", "a", "c"), FavouriteOrder.move(List.of("a", "b", "c"), "b", -1));
        assertEquals(List.of("a", "c", "b"), FavouriteOrder.move(List.of("a", "b", "c"), "b", 1));
    }

    @Test public void edgeMoveLeavesOrderUnchanged() {
        assertEquals(List.of("a", "b"), FavouriteOrder.move(List.of("a", "b"), "a", -1));
        assertEquals(List.of("a", "b"), FavouriteOrder.move(List.of("a", "b"), "b", 1));
    }
}
