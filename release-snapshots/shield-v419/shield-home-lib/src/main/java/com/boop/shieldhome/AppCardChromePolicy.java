package com.boop.shieldhome;

/** Pure focus-state policy for floating app artwork. */
final class AppCardChromePolicy {
    private AppCardChromePolicy() { }

    static boolean showPlate(boolean focused, boolean selected, boolean grabbed) {
        return focused || selected || grabbed;
    }

    static boolean showPlate(boolean homeFavourite, boolean focused, boolean selected, boolean grabbed) {
        return !homeFavourite && showPlate(focused, selected, grabbed);
    }

    static boolean showBadge(boolean homeFavourite, boolean favourite, boolean grabbed) {
        return grabbed || (!homeFavourite && favourite);
    }

    static boolean emphasizeArtworkOnly(boolean homeFavourite) {
        return homeFavourite;
    }
}
