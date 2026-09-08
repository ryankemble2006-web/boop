package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public final class HomeReplacementPolicyTest {
    @Test public void firstLaunchPromptOnlyRunsOnceWhenBoopIsNotHome() {
        assertTrue(HomeReplacementPolicy.shouldAutoPrompt(false, false));
        assertFalse(HomeReplacementPolicy.shouldAutoPrompt(true, false));
        assertFalse(HomeReplacementPolicy.shouldAutoPrompt(false, true));
        assertFalse(HomeReplacementPolicy.shouldAutoPrompt(true, true));
    }

    @Test public void stockHomeSelectionPrefersSystemLauncherAndNeverBoop() {
        List<HomeReplacementPolicy.Candidate> candidates = List.of(
                new HomeReplacementPolicy.Candidate("com.boop.shieldhome", true, true),
                new HomeReplacementPolicy.Candidate("com.example.otherlauncher", false, true),
                new HomeReplacementPolicy.Candidate("com.google.android.tvlauncher", true, true));

        assertEquals(
                "com.google.android.tvlauncher",
                HomeReplacementPolicy.selectStockHome(candidates, "com.boop.shieldhome"));
    }

    @Test public void disabledSystemLauncherRemainsAValidEmergencyCandidate() {
        List<HomeReplacementPolicy.Candidate> candidates = List.of(
                new HomeReplacementPolicy.Candidate("com.boop.shieldhome", false, true),
                new HomeReplacementPolicy.Candidate("com.stock.home", true, false));

        assertEquals(
                "com.stock.home",
                HomeReplacementPolicy.selectStockHome(candidates, "com.boop.shieldhome"));
    }
}
