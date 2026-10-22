package com.boop.shieldhome;

import java.util.List;

/** Pure deterministic selection policy for Android media sessions. */
final class NowPlayingSelectionPolicy {
    static final class Candidate {
        final long id;
        final String packageName;
        final int playbackState;

        Candidate(long id, String packageName, int playbackState) {
            this.id = id;
            this.packageName = packageName == null ? "" : packageName;
            this.playbackState = playbackState;
        }
    }

    private NowPlayingSelectionPolicy() { }

    static boolean eligible(int state) {
        switch (state) {
            case 2:  // PAUSED
            case 3:  // PLAYING
            case 4:  // FAST_FORWARDING
            case 5:  // REWINDING
            case 6:  // BUFFERING
            case 8:  // CONNECTING
            case 9:  // SKIPPING_TO_PREVIOUS
            case 10: // SKIPPING_TO_NEXT
            case 11: // SKIPPING_TO_QUEUE_ITEM
                return true;
            default:
                return false;
        }
    }

    static long select(List<Candidate> candidates, long currentId, String preferredPackage) {
        if (candidates == null || candidates.isEmpty()) {
            return 0L;
        }
        String preferred = preferredPackage == null ? "" : preferredPackage.trim();
        if (!preferred.isEmpty()) {
            long preferredResult = selectWithin(candidates, currentId, preferred);
            if (preferredResult != 0L) {
                return preferredResult;
            }
        }
        return selectWithin(candidates, currentId, null);
    }

    private static long selectWithin(List<Candidate> candidates, long currentId, String packageFilter) {
        long firstEligible = 0L;
        long firstPlaying = 0L;
        boolean currentEligible = false;
        boolean currentPlaying = false;

        for (Candidate candidate : candidates) {
            if (candidate == null
                    || (packageFilter != null && !packageFilter.equals(candidate.packageName))
                    || !eligible(candidate.playbackState)) {
                continue;
            }
            if (firstEligible == 0L) {
                firstEligible = candidate.id;
            }
            boolean playing = candidate.playbackState == 3;
            if (playing && firstPlaying == 0L) {
                firstPlaying = candidate.id;
            }
            if (candidate.id == currentId) {
                currentEligible = true;
                currentPlaying = playing;
            }
        }

        if (currentPlaying) return currentId;
        if (firstPlaying != 0L) return firstPlaying;
        if (currentEligible) return currentId;
        return firstEligible;
    }
}
