package com.boop.shieldhome;

import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;

/** Requests a Deezer Track Mix from the exact current track through the native media session. */
final class DeezerTrackMixController {
    enum Result { REQUESTED, IGNORED, UNAVAILABLE }

    private static final String DEEZER_PACKAGE = "deezer.android.app";
    private static final long REPEAT_GUARD_MS = 1200L;

    private MediaSession.Token lastToken;
    private String lastTrackId = "";
    private long lastRequestMs;

    boolean canRequest(MediaController controller, NowPlayingSnapshot snapshot, String trackId) {
        return isAvailable(controller, snapshot, trackId);
    }

    Result request(MediaController controller, NowPlayingSnapshot snapshot, String trackId, long nowMs) {
        if (nowMs < 0 || !isAvailable(controller, snapshot, trackId)) return Result.UNAVAILABLE;
        try {
            MediaSession.Token token = controller.getSessionToken();
            if (token.equals(lastToken) && trackId.equals(lastTrackId)
                    && nowMs >= lastRequestMs && nowMs - lastRequestMs < REPEAT_GUARD_MS)
                return Result.IGNORED;
            Uri trackUri = Uri.parse("https://www.deezer.com/track/" + trackId);
            controller.getTransportControls().playFromUri(trackUri, new Bundle());
            lastToken = token;
            lastTrackId = trackId;
            lastRequestMs = nowMs;
            return Result.REQUESTED;
        } catch (RuntimeException unavailable) {
            return Result.UNAVAILABLE;
        }
    }

    private boolean isAvailable(MediaController controller, NowPlayingSnapshot snapshot, String trackId) {
        if (controller == null || snapshot == null || trackId == null
                || !DeezerLyricsClient.validTrackId(trackId)
                || !DEEZER_PACKAGE.equals(snapshot.packageName())
                || !NowPlayingSelectionPolicy.eligible(snapshot.playbackState())) return false;
        try {
            PlaybackState live = controller.getPlaybackState();
            return DEEZER_PACKAGE.equals(controller.getPackageName())
                    && controller.getSessionToken() != null
                    && live != null
                    && NowPlayingSelectionPolicy.eligible(live.getState())
                    && (live.getActions() & PlaybackState.ACTION_PLAY_FROM_URI) != 0;
        } catch (RuntimeException unavailable) {
            return false;
        }
    }
}
