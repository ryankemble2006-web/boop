package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/** Exact timed-data preflight, then BOOP's own screen. Never launches or clicks inside Deezer. */
final class DeezerLyricsBrowser {
    private final NativeLyricsLoader loader = new NativeLyricsLoader();
    private NowPlayingSnapshot pinnedSnapshot;
    private String pinnedIdentity = "";
    private LyricsLookupDialog lookupDialog;
    private long revision;

    void cancel() {
        revision++;
        loader.cancel();
        if (lookupDialog != null) lookupDialog.dismiss();
        lookupDialog = null;
        pinnedSnapshot = null;
        pinnedIdentity = "";
    }
    void destroy() { cancel(); loader.destroy(); }
    void onHostPaused() { cancel(); }
    void onTrackChanged(ShieldNowPlayingManager manager) {
        if (pinnedSnapshot != null && !pinnedIdentity.equals(identity(manager, pinnedSnapshot))) cancel();
    }
    void open(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        search(activity, manager, requested, requested, false);
    }
    private void search(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested,
            NowPlayingSnapshot query, boolean fresh) {
        if (activity == null || manager == null || requested == null
                || !DeezerLyricsPolicy.available(requested.packageName())) return;
        String deezerId = manager.deezerLyricsTrackId(requested);
        String cacheId = deezerId.isEmpty()
                ? "meta:" + Integer.toHexString((requested.trackKey()+"\n"+requested.album()+"\n"+requested.durationMs()).hashCode()) : deezerId;
        String requestIdentity = requested.sessionId() + ":" + cacheId;
        if (requestIdentity.equals(pinnedIdentity)) return;
        cancel();
        pinnedSnapshot = requested;
        pinnedIdentity = requestIdentity;
        java.util.function.Consumer<DeezerLyricsDocument> completion = document -> {
            if (!requestIdentity.equals(pinnedIdentity)) return;
            // A completed lookup must release the button even if a dialog or another
            // window briefly took focus. Never launch late, and never latch entry busy.
            pinnedSnapshot = null;
            pinnedIdentity = "";
            if (!validHost(activity) || !requestIdentity.equals(identity(manager, requested))) return;
            if (document.status() == DeezerLyricsDocument.Status.UNAVAILABLE) {
                edit(activity, manager, requested, query, "No lyrics for this track.");
            } else if (document.status() == DeezerLyricsDocument.Status.UNKNOWN) {
                edit(activity, manager, requested, query, "Couldn't check lyrics just now.");
            } else {
                try {
                    activity.startActivity(new Intent(activity, ShieldLyricsActivity.class));
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (RuntimeException unavailable) {
                    message(activity, "Couldn't open lyrics just now.");
                }
            }
        };
        if (fresh) loader.reload(query, cacheId, requestIdentity, completion);
        else loader.load(requested, cacheId, requestIdentity, completion);
    }
    private void edit(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot original,
            NowPlayingSnapshot query, String message) {
        pinnedSnapshot = original;
        pinnedIdentity = identity(manager, original);
        String expected = pinnedIdentity;
        long ticket = revision;
        LyricsLookupDialog dialog = new LyricsLookupDialog(activity, query, message, edited -> {
            if (ticket != revision || activity.isFinishing() || activity.isDestroyed()
                    || !expected.equals(identity(manager, original))) return;
            // Android delivers OnDismissListener later. Release the editor pin now;
            // its delayed dismissal must not clear the new lookup's identity.
            lookupDialog = null;
            pinnedSnapshot = null;
            pinnedIdentity = "";
            search(activity, manager, original, edited, true);
        });
        lookupDialog = dialog;
        dialog.setOnDismissListener(ignored -> {
            if (lookupDialog != dialog) return;
            lookupDialog = null;
            pinnedSnapshot = null;
            pinnedIdentity = "";
        });
        dialog.show();
    }
    private static String identity(ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        if (manager == null || requested == null) return "";
        NowPlayingSnapshot current = manager.state().current();
        if (current == null || current.sessionId() != requested.sessionId()
                || !current.trackKey().equals(requested.trackKey())
                || !current.album().equals(requested.album())
                || current.durationMs() != requested.durationMs()) return "";
        String id = manager.deezerLyricsTrackId(requested);
        String cacheId = id.isEmpty()
                ? "meta:" + Integer.toHexString((requested.trackKey()+"\n"+requested.album()+"\n"+requested.durationMs()).hashCode()) : id;
        return requested.sessionId() + ":" + cacheId;
    }
    private static boolean validHost(Activity activity) {
        return !activity.isFinishing() && !activity.isDestroyed() && activity.hasWindowFocus();
    }
    private static void message(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
