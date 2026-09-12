package com.boop.shieldhome;

/** Maps existing Now Playing states to finished canonical Animation Lab clips. */
final class CanonicalMediaAnimationPolicy {
    private CanonicalMediaAnimationPolicy() { }

    static String steadyClip(NowPlayingPuppetPolicy.Mode mode) {
        if (mode == NowPlayingPuppetPolicy.Mode.GROOVE) return "music";
        if (mode == NowPlayingPuppetPolicy.Mode.UPSET) return "media_pause";
        return "idle";
    }

    static String trackChangeClip() {
        return "track_change";
    }
}
