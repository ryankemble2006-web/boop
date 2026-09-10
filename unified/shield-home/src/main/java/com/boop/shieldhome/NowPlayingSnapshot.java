package com.boop.shieldhome;

import android.graphics.Bitmap;
import android.media.session.PlaybackState;

import java.util.Objects;

/** Immutable media-session state consumed by the launcher UI and puppet. */
public final class NowPlayingSnapshot {
    private final long sessionId;
    private final String packageName;
    private final String castAppName;
    private final String album;
    private final String title;
    private final String subtitle;
    private final int playbackState;
    private final long actions;
    private final long positionMs;
    private final long durationMs;
    private final float playbackSpeed;
    private final long updateTimeMs;
    private final Bitmap artwork;

    public NowPlayingSnapshot(
            long sessionId,
            String packageName,
            String title,
            String subtitle,
            int playbackState,
            long actions,
            long positionMs,
            long durationMs,
            float playbackSpeed,
            long updateTimeMs,
            Bitmap artwork) {
        this(sessionId,packageName,title,subtitle,playbackState,actions,positionMs,
                durationMs,playbackSpeed,updateTimeMs,artwork,"");
    }
    public NowPlayingSnapshot(long sessionId,String packageName,String title,String subtitle,
            int playbackState,long actions,long positionMs,long durationMs,float playbackSpeed,
            long updateTimeMs,Bitmap artwork,String castAppName) {
        this(sessionId,packageName,title,subtitle,playbackState,actions,positionMs,durationMs,
                playbackSpeed,updateTimeMs,artwork,castAppName,"");
    }
    public NowPlayingSnapshot(long sessionId,String packageName,String title,String subtitle,
            int playbackState,long actions,long positionMs,long durationMs,float playbackSpeed,
            long updateTimeMs,Bitmap artwork,String castAppName,String album) {
        this.album=clean(album);
        this.castAppName = clean(castAppName);
        this.sessionId = sessionId;
        this.packageName = clean(packageName);
        this.title = clean(title);
        this.subtitle = clean(subtitle);
        this.playbackState = playbackState;
        this.actions = actions;
        this.positionMs = Math.max(0L, positionMs);
        this.durationMs = Math.max(0L, durationMs);
        this.playbackSpeed = playbackSpeed;
        this.updateTimeMs = Math.max(0L, updateTimeMs);
        this.artwork = artwork;
    }

    public long sessionId() { return sessionId; }
    public String packageName() { return packageName; }
    public String castAppName() { return castAppName; }
    public String album() { return album; }
    public String title() { return title; }
    public String subtitle() { return subtitle; }
    public int playbackState() { return playbackState; }
    public long actions() { return actions; }
    public long positionMs() { return positionMs; }
    public long durationMs() { return durationMs; }
    public float playbackSpeed() { return playbackSpeed; }
    public long updateTimeMs() { return updateTimeMs; }
    public Bitmap artwork() { return artwork; }

    public boolean isPlaying() {
        return playbackState == PlaybackState.STATE_PLAYING;
    }

    public boolean isPaused() {
        return playbackState == PlaybackState.STATE_PAUSED;
    }

    public boolean canPrevious() { return NowPlayingActionPolicy.canPrevious(actions); }
    public boolean canRewind() { return NowPlayingActionPolicy.canRewind(actions); }
    public boolean canPlayPause() {
        return NowPlayingActionPolicy.canPlayPause(actions, playbackState);
    }
    public boolean canFastForward() { return NowPlayingActionPolicy.canFastForward(actions); }
    public boolean canNext() { return NowPlayingActionPolicy.canNext(actions); }

    /** Position suitable for a lightweight progress ticker between media-session callbacks. */
    public long estimatedPositionMs(long nowElapsedRealtimeMs) {
        long estimate = positionMs;
        if (isPlaying() && updateTimeMs > 0L && nowElapsedRealtimeMs > updateTimeMs) {
            estimate += Math.round((nowElapsedRealtimeMs - updateTimeMs) * playbackSpeed);
        }
        if (durationMs > 0L) {
            estimate = Math.min(estimate, durationMs);
        }
        return Math.max(0L, estimate);
    }

    public String trackKey() {
        return packageName + "\n" + title + "\n" + subtitle;
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof NowPlayingSnapshot)) return false;
        NowPlayingSnapshot that = (NowPlayingSnapshot) other;
        return sessionId == that.sessionId
                && playbackState == that.playbackState
                && actions == that.actions
                && positionMs == that.positionMs
                && durationMs == that.durationMs
                && Float.compare(playbackSpeed, that.playbackSpeed) == 0
                && updateTimeMs == that.updateTimeMs
                && castAppName.equals(that.castAppName)
                && packageName.equals(that.packageName)
                && album.equals(that.album)
                && title.equals(that.title)
                && subtitle.equals(that.subtitle)
                && Objects.equals(artwork, that.artwork);
    }

    @Override public int hashCode() {
        return Objects.hash(
                sessionId,
                packageName,
                title,
                subtitle,
                playbackState,
                actions,
                positionMs,
                durationMs,
                playbackSpeed,
                updateTimeMs,
                artwork,castAppName,album);
    }

    private static String clean(String value) {
        return value == null ? "" : value.toString().trim();
    }
}
