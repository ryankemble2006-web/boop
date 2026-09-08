package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import java.util.List;
import org.junit.Test;

public final class ShieldLauncherActivityContractTest {
    @Test public void launcherIsInternalActivityWithLocalPageTiming() {
        assertTrue(Activity.class.isAssignableFrom(ShieldLauncherActivity.class));
        assertEquals(140L, ShieldLauncherActivity.PAGE_TRANSITION_MS);
    }

    @Test public void favouriteEditsDelegateToStableOrderingRules() {
        List<String> start = List.of("a/.A", "b/.B", "c/.C");
        assertEquals(
                List.of("b/.B", "a/.A", "c/.C"),
                ShieldLauncherActivity.applyFavouriteEdit(
                        start, "b/.B", ShieldLauncherActivity.FavouriteEdit.MOVE_LEFT));
        assertEquals(
                List.of("a/.A", "c/.C", "b/.B"),
                ShieldLauncherActivity.applyFavouriteEdit(
                        start, "b/.B", ShieldLauncherActivity.FavouriteEdit.MOVE_RIGHT));
        assertEquals(
                List.of("a/.A", "c/.C"),
                ShieldLauncherActivity.applyFavouriteEdit(
                        start, "b/.B", ShieldLauncherActivity.FavouriteEdit.REMOVE));
    }

    @Test public void edgeMovesAreNoOps() {
        List<String> start = List.of("a/.A", "b/.B");
        assertEquals(
                start,
                ShieldLauncherActivity.applyFavouriteEdit(
                        start, "a/.A", ShieldLauncherActivity.FavouriteEdit.MOVE_LEFT));
        assertEquals(
                start,
                ShieldLauncherActivity.applyFavouriteEdit(
                        start, "b/.B", ShieldLauncherActivity.FavouriteEdit.MOVE_RIGHT));
    }
}
