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

    @Test public void resolvedSystemHomeWinsOverOtherHomeCapableSystemPackages() {
        List<HomeReplacementPolicy.Candidate> candidates = List.of(
                new HomeReplacementPolicy.Candidate("com.google.android.tungsten.setupwraith", true, true),
                new HomeReplacementPolicy.Candidate("com.google.android.tvlauncher", true, true));

        assertEquals(
                "com.google.android.tvlauncher",
                HomeReplacementPolicy.selectStockHome(
                        candidates,
                        "com.boop.shieldhome",
                        "com.google.android.tvlauncher"));
    }

    @Test public void setupWraithIsNeverAStockLauncherTarget() {
        List<HomeReplacementPolicy.Candidate> candidates = List.of(
                new HomeReplacementPolicy.Candidate("com.google.android.tungsten.setupwraith", true, true),
                new HomeReplacementPolicy.Candidate("com.google.android.tvlauncher", true, true));

        assertEquals(
                "com.google.android.tvlauncher",
                HomeReplacementPolicy.selectStockHome(
                        candidates,
                        "com.boop.shieldhome",
                        "com.boop.shieldhome"));
        assertTrue(HomeReplacementPolicy.isProvisioningHome(
                "com.google.android.tungsten.setupwraith"));
    }

    @Test public void knownShieldStockHomeBeatsArbitrarySystemHomeWhenBoopIsResolved() {
        List<HomeReplacementPolicy.Candidate> candidates = List.of(
                new HomeReplacementPolicy.Candidate("com.vendor.somehome", true, true),
                new HomeReplacementPolicy.Candidate("com.google.android.tvlauncher", true, true));

        assertEquals(
                "com.google.android.tvlauncher",
                HomeReplacementPolicy.selectStockHome(
                        candidates,
                        "com.boop.shieldhome",
                        "com.boop.shieldhome"));
    }

    @Test public void disabledShieldStockLauncherRemainsAValidEmergencyCandidate() {
        List<HomeReplacementPolicy.Candidate> candidates = List.of(
                new HomeReplacementPolicy.Candidate("com.boop.shieldhome", false, true),
                new HomeReplacementPolicy.Candidate("com.google.android.tvlauncher", true, false));

        assertEquals(
                "com.google.android.tvlauncher",
                HomeReplacementPolicy.selectStockHome(
                        candidates,
                        "com.boop.shieldhome",
                        "com.boop.shieldhome"));
    }

    @Test public void defaultHomeTruthUsesResolvedPackageIdentity() {
        assertTrue(HomeReplacementPolicy.isOwnResolvedHome(
                "com.boop.shieldhome", "com.boop.shieldhome"));
        assertFalse(HomeReplacementPolicy.isOwnResolvedHome(
                "com.google.android.tvlauncher", "com.boop.shieldhome"));
        assertFalse(HomeReplacementPolicy.isOwnResolvedHome(null, "com.boop.shieldhome"));
    }
}
