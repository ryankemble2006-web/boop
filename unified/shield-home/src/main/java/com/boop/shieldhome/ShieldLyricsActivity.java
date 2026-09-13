package com.boop.shieldhome;

import android.app.Activity;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

/** BOOP-owned presentation following the existing media session, without taking audio focus. */
public final class ShieldLyricsActivity extends Activity {
    private final NativeLyricsLoader loader = new NativeLyricsLoader();
    private ShieldNowPlayingManager manager;
    private ShieldLyricsView presentation;
    private Runnable unsubscribe;
    private String identity = "";
    private boolean started;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        manager = ShieldNowPlayingManager.get(this);
        presentation = new ShieldLyricsView(this, FocusChrome.accentColor(this), new ShieldLyricsView.Controls() {
            @Override public void previous() { manager.previous(); }
            @Override public void playPause() { manager.togglePlayPause(); }
            @Override public void next() { manager.next(); }
            @Override public void seek(long milliseconds) { manager.seekBy(milliseconds); }
            @Override public void close() { finish(); }
        });
        setContentView(presentation);
    }
    @Override protected void onStart() {
        super.onStart();
        started = true;
        identity = "";
        presentation.setRunning(true);
        unsubscribe = manager.state().subscribe(this::changed);
        manager.refreshAccess();
    }
    private void changed(NowPlayingSnapshot snapshot) {
        if (!started || isFinishing()) return;
        presentation.setSnapshot(snapshot, clockKnown(snapshot));
        String id = snapshot == null ? "" : manager.deezerLyricsTrackId(snapshot);
        String next = id.isEmpty() ? "" : snapshot.sessionId() + ":" + id;
        if (next.equals(identity) && !next.isEmpty()) return;
        identity = next;
        loader.cancel();
        presentation.setDocument(null);
        if (snapshot == null || !NowPlayingSelectionPolicy.eligible(snapshot.playbackState())) {
            presentation.setStatus("Nothing playing.");
        } else if (!DeezerLyricsPolicy.available(snapshot.packageName())) {
            presentation.setStatus("Lyrics are available here for Deezer.");
        } else if (id.isEmpty()) {
            presentation.setStatus("Couldn't identify this track for lyrics.");
        } else {
            presentation.setStatus("Finding lyrics…");
            loader.load(id, next, document -> {
                if (!started || !next.equals(identity) || isFinishing()) return;
                if (document.status() == DeezerLyricsDocument.Status.AVAILABLE) {
                    presentation.setDocument(document);
                    presentation.setStatus("");
                } else {
                    presentation.setDocument(null);
                    presentation.setStatus(document.status() == DeezerLyricsDocument.Status.UNAVAILABLE
                            ? "No lyrics for this track." : "Couldn't check lyrics just now.");
                }
            });
        }
    }
    private boolean clockKnown(NowPlayingSnapshot snapshot) {
        if (snapshot == null) return false;
        try {
            MediaSession.Token token = manager.sessionToken(snapshot.sessionId());
            PlaybackState state = token == null ? null : new MediaController(this, token).getPlaybackState();
            return state != null && state.getPosition() >= 0 && state.getLastPositionUpdateTime() > 0;
        } catch (RuntimeException unavailable) { return false; }
    }
    @Override protected void onStop() {
        started = false;
        loader.cancel();
        if (unsubscribe != null) unsubscribe.run();
        unsubscribe = null;
        presentation.setRunning(false);
        super.onStop();
    }
    @Override protected void onDestroy() { loader.destroy(); super.onDestroy(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: manager.togglePlayPause(); return true;
                case KeyEvent.KEYCODE_MEDIA_NEXT: manager.next(); return true;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS: manager.previous(); return true;
                default: break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
