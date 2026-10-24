package com.boop.shieldhome;

import java.util.Locale;

/** Strict track-favourite semantics. A dislike, artist or playlist action is never a match. */
public final class DeezerFavouritePolicy {
    public static final int UNKNOWN = -1, NOT_SAVED = 0, SAVED = 1;
    private DeezerFavouritePolicy() { }

    public static int action(CharSequence label) {
        if (label == null) return 0;
        String text = label.toString().trim().toLowerCase(Locale.ROOT)
                .replace("favourites", "favorites").replaceAll("\\s+", " ");
        switch (text) {
            case "add to favorites":
            case "add track to favorites":
            case "add this track to favorites":
            case "add song to favorites":
            case "add this song to favorites":
                return 1;
            case "remove from favorites":
            case "remove track from favorites":
            case "remove this track from favorites":
            case "remove song from favorites":
            case "remove this song from favorites":
                return -1;
            default: return 0;
        }
    }

    public static int state(Boolean heart, boolean addOffered, boolean removeOffered) {
        if (heart != null) {
            if (addOffered != removeOffered && heart.booleanValue() == addOffered) return UNKNOWN;
            return heart ? SAVED : NOT_SAVED;
        }
        if (addOffered == removeOffered) return UNKNOWN;
        return removeOffered ? SAVED : NOT_SAVED;
    }

    public static boolean sameTrack(long expectedSession, String expectedTrack,
            long actualSession, String actualTrack) {
        return expectedSession > 0 && expectedSession == actualSession
                && expectedTrack != null && !expectedTrack.isEmpty()
                && expectedTrack.equals(actualTrack);
    }
}
