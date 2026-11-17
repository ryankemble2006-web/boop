package com.boop.shieldhome;

import java.util.List;
import java.util.Locale;

/** Pure policy for simple HOME replacement and stock-launcher recovery. */
final class HomeReplacementPolicy {
    private static final String ANDROID_TV_HOME = "com.google.android.tvlauncher";
    private static final String GOOGLE_TV_HOME = "com.google.android.apps.tv.launcherx";

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

    static boolean isOwnResolvedHome(String resolvedPackage, String ownPackage) {
        return resolvedPackage != null
                && ownPackage != null
                && !ownPackage.isEmpty()
                && ownPackage.equals(resolvedPackage);
    }

    static boolean isProvisioningHome(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        String lower = packageName.toLowerCase(Locale.ROOT);
        return lower.contains("setupwraith")
                || lower.contains(".setup")
                || lower.contains("provision")
                || lower.contains("tvsetup");
    }

    static String selectStockHome(List<Candidate> candidates, String ownPackage) {
        return selectStockHome(candidates, ownPackage, null);
    }

    static String selectStockHome(
            List<Candidate> candidates,
            String ownPackage,
            String resolvedPackage) {
        if (candidates == null) return null;
        String own = ownPackage == null ? "" : ownPackage;

        Candidate knownStock = null;
        Candidate firstEnabled = null;
        Candidate firstDisabled = null;

        for (Candidate candidate : candidates) {
            if (!eligibleStockCandidate(candidate, own)) continue;

            if (candidate.packageName.equals(resolvedPackage)) {
                return candidate.packageName;
            }
            if (isKnownTvHome(candidate.packageName) && knownStock == null) {
                knownStock = candidate;
            }
            if (candidate.enabled && firstEnabled == null) {
                firstEnabled = candidate;
            }
            if (!candidate.enabled && firstDisabled == null) {
                firstDisabled = candidate;
            }
        }

        if (knownStock != null) return knownStock.packageName;
        if (firstEnabled != null) return firstEnabled.packageName;
        return firstDisabled == null ? null : firstDisabled.packageName;
    }

    private static boolean eligibleStockCandidate(Candidate candidate, String ownPackage) {
        if (candidate == null || candidate.packageName.isEmpty()) return false;
        if (candidate.packageName.equals(ownPackage)) return false;
        if (!candidate.systemApp) return false;
        return !isProvisioningHome(candidate.packageName);
    }

    private static boolean isKnownTvHome(String packageName) {
        return ANDROID_TV_HOME.equals(packageName) || GOOGLE_TV_HOME.equals(packageName);
    }
}
