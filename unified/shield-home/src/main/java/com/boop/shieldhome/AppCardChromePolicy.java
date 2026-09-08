package com.boop.shieldhome;

/** Pure focus-state policy for floating app artwork. */
final class AppCardChromePolicy {
    private AppCardChromePolicy() { }

    static boolean showPlate(boolean focused, boolean selected, boolean grabbed) {
        return focused || selected || grabbed;
    }
}
