package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/** Exact timed-data preflight, then BOOP's own screen. Never launches or clicks inside Deezer. */
final class DeezerLyricsBrowser {
    private final NativeLyricsLoader loader = new NativeLyricsLoader();
    private NowPlayingSnapshot pinnedSnapshot;
    private String pinnedIdentity = "";

    void cancel() {
        loader.cancel();
        pinnedSnapshot = null;
        pinnedIdentity = "";
    }
    void destroy() { cancel(); loader.destroy(); }
    void onHostPaused() { cancel(); }
    void onTrackChanged(ShieldNowPlayingManager manager) {
        if (pinnedSnapshot != null && !pinnedIdentity.equals(identity(manager, pinnedSnapshot))) cancel();
    }
    void open(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        if (activity == null || manager == null || requested == null
                || !DeezerLyricsPolicy.available(requested.packageName())) return;
        String deezerId = manager.deezerLyricsTrackId(requested);
        String cacheId = deezerId.isEmpty()
                ? "meta:" + Integer.toHexString(requested.trackKey().hashCode()) : deezerId;
        String requestIdentity = requested.sessionId() + ":" + cacheId;
        if (requestIdentity.equals(pinnedIdentity)) return;
        cancel();
        pinnedSnapshot = requested;
        pinnedIdentity = requestIdentity;
        loader.load(requested, cacheId, requestIdentity, document -> {
            if (!requestIdentity.equals(pinnedIdentity)) return;
            // A completed lookup must release the button even if a dialog or another
            // window briefly took focus. Never launch late, and never latch entry busy.
            pinnedSnapshot = null;
            pinnedIdentity = "";
            if (!validHost(activity) || !requestIdentity.equals(identity(manager, requested))) return;
            if (document.status() == DeezerLyricsDocument.Status.UNAVAILABLE) {
                message(activity, "No lyrics for this track.");
            } else if (document.status() == DeezerLyricsDocument.Status.UNKNOWN) {
                message(activity, "Couldn't check lyrics just now.");
            } else {
                try {
                    activity.startActivity(new Intent(activity, ShieldLyricsActivity.class));
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (RuntimeException unavailable) {
                    message(activity, "Couldn't open lyrics just now.");
                }
            }
        });
    }
    private static String identity(ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        if (manager == null || requested == null) return "";
        String id = manager.deezerLyricsTrackId(requested);
        String cacheId = id.isEmpty()
                ? "meta:" + Integer.toHexString(requested.trackKey().hashCode()) : id;
        return requested.sessionId() + ":" + cacheId;
    }
    private static boolean validHost(Activity activity) {
        return !activity.isFinishing() && !activity.isDestroyed() && activity.hasWindowFocus();
    }
    private static void message(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
