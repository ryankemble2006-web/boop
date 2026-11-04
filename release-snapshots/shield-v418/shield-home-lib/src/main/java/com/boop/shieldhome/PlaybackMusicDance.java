package com.boop.shieldhome;

/** A smooth, explicitly synthetic groove while playback is active; independent of Android animator scale. */
final class PlaybackMusicDance {
    static float level(long nowMs, long startMs) {
        if (startMs < 0L || nowMs < startMs) return 0f;
        long phase = (nowMs - startMs) % 640L;
        if (phase >= 420L) return 0f;
        double rise = Math.sin(Math.PI * phase / 420.0);
        return (float) (0.9 * rise * rise);
    }
    private PlaybackMusicDance() { }
}
