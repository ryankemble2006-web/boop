package com.boop.shieldhome;

import java.util.List;

/** Pure policy for simple HOME replacement and stock-launcher recovery. */
final class HomeReplacementPolicy {
    static final class Candidate {
        final String packageName;
        final boolean systemApp;
        final boolean enabled;

        Candidate(String packageName, boolean systemApp, boolean enabled) {
            this.packageName = packageName == null ? "" : packageName;
            this.systemApp = systemApp;
            this.enabled = enabled;
        }
    }

    private HomeReplacementPolicy() {}

    static boolean shouldAutoPrompt(boolean boopIsDefaultHome, boolean promptAlreadyShown) {
        return !boopIsDefaultHome && !promptAlreadyShown;
    }

    static String selectStockHome(List<Candidate> candidates, String ownPackage) {
        if (candidates == null) return null;
        String own = ownPackage == null ? "" : ownPackage;
        for (Candidate candidate : candidates) {
            if (candidate == null || candidate.packageName.isEmpty()) continue;
            if (candidate.packageName.equals(own)) continue;
            if (!candidate.systemApp) continue;
            return candidate.packageName;
        }
        return null;
    }
}
