package com.boop.shieldhome;

import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;

/** Starts the existing native Deezer Flow URI without opening a player screen. */
final class DeezerFlowController {
    enum Result { REQUESTED, IGNORED, UNAVAILABLE }
    private static final String DEEZER_PACKAGE = "deezer.android.app";
    private static final String FLOW_URI = "https://www.deezer.com/flow";
    private static final long REPEAT_GUARD_MS = 1200L;
    private MediaSession.Token lastToken;
    private long lastRequestMs;

    Result request(MediaController controller, NowPlayingSnapshot snapshot, long nowMs) {
        if (controller == null || snapshot == null || nowMs < 0
                || !DEEZER_PACKAGE.equals(snapshot.packageName())
                || !NowPlayingSelectionPolicy.eligible(snapshot.playbackState())) return Result.UNAVAILABLE;
        try {
            if (!DEEZER_PACKAGE.equals(controller.getPackageName())) return Result.UNAVAILABLE;
            PlaybackState live = controller.getPlaybackState();
            MediaSession.Token token = controller.getSessionToken();
            if (live == null || token == null || (live.getActions() & PlaybackState.ACTION_PLAY_FROM_URI) == 0)
                return Result.UNAVAILABLE;
            if (token.equals(lastToken) && nowMs >= lastRequestMs && nowMs - lastRequestMs < REPEAT_GUARD_MS)
                return Result.IGNORED;
            // The URI is already used by BOOP's native voice/music route. Dispatch is
            // a request, not proof that the provider has finished starting playback.
            controller.getTransportControls().playFromUri(Uri.parse(FLOW_URI), new Bundle());
            lastToken = token;
            lastRequestMs = nowMs;
            return Result.REQUESTED;
        } catch (RuntimeException unavailable) {
            return Result.UNAVAILABLE;
        }
    }
}