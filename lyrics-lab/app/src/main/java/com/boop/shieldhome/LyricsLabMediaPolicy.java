package com.boop.shieldhome;

/** Lab must never attach a lyric recording identity to BOOP, Cast, podcasts or another player. */
final class LyricsLabMediaPolicy {
    private LyricsLabMediaPolicy() { }
    static String recordingId(String packageName, String type, String id) {
        return "deezer.android.app".equals(packageName) && "TRACK".equals(type)
                && id != null && id.matches("[1-9][0-9]{0,18}") ? id : "";
    }
}
