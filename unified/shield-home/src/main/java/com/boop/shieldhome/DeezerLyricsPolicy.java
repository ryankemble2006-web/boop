package com.boop.shieldhome;

/** Keeps the Now Playing lyrics shortcut scoped to native Deezer only. */
public final class DeezerLyricsPolicy {
    private DeezerLyricsPolicy() { }

    public static boolean available(String packageName) {
        return "deezer.android.app".equals(packageName == null ? "" : packageName.trim());
    }
}
