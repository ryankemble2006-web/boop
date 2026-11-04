package com.boop.shieldhome;

/** A temporary search copy; the media-session snapshot is never modified. */
final class LyricsLookupQuery {
    static NowPlayingSnapshot edit(NowPlayingSnapshot original,String title,String artist) {
        String t=title==null?"":title.trim(),a=artist==null?"":artist.trim();
        if(original==null || t.isEmpty() || a.isEmpty() || t.length()>256 || a.length()>256)
            throw new IllegalArgumentException("Enter a track title and artist");
        return new NowPlayingSnapshot(original.sessionId(),original.packageName(),t,a,
                original.playbackState(),original.actions(),original.positionMs(),original.durationMs(),
                original.playbackSpeed(),original.updateTimeMs(),original.artwork(),original.castAppName(),"");
    }
}
