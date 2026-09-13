package com.boop.shieldhome;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.util.List;

/** A separate, foreground-only Deezer observer. No BOOP bridge, HOME, audio focus or background control. */
final class LyricsLabSession {
    interface Listener { void changed(Frame frame, boolean access); }
    static final class Frame {
        final NowPlayingSnapshot snapshot;
        final String trackId;
        final boolean clockKnown;
        Frame(NowPlayingSnapshot snapshot, String trackId, boolean clockKnown) {
            this.snapshot = snapshot; this.trackId = trackId; this.clockKnown = clockKnown;
        }
        String identity() {
            return snapshot.sessionId() + ":" + trackId + ":" + snapshot.trackKey()
                    + ":" + snapshot.album() + ":" + snapshot.durationMs();
        }
    }
    private static LyricsLabSession instance;
    static synchronized LyricsLabSession get(Context context) {
        if (instance == null) instance = new LyricsLabSession(context.getApplicationContext());
        return instance;
    }
    static void accessChanged() {
        LyricsLabSession current = instance;
        if (current != null) current.main.post(current::refresh);
    }
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final MediaSessionManager sessions;
    private final ComponentName accessComponent;
    private final NowPlayingArtworkResolver artwork;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsChanged = ignored -> refresh();
    private final MediaController.Callback callback = new MediaController.Callback() {
        @Override public void onMetadataChanged(MediaMetadata metadata) { publish(); }
        @Override public void onPlaybackStateChanged(PlaybackState state) { refresh(); }
        @Override public void onSessionDestroyed() { refresh(); }
    };
    private Listener listener;
    private MediaController selected;
    private boolean registered;
    private long sessionId;
    private Frame frame;
    private LyricsLabSession(Context context) {
        this.context = context;
        sessions = context.getSystemService(MediaSessionManager.class);
        accessComponent = new ComponentName(context, LyricsLabMediaListener.class);
        artwork = new NowPlayingArtworkResolver(context, main, this::publish);
    }
    ComponentName accessComponent() { return accessComponent; }
    boolean hasAccess() {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        try { return manager != null && manager.isNotificationListenerAccessGranted(accessComponent); }
        catch (RuntimeException unavailable) { return false; }
    }
    void start(Listener listener) { stop(); this.listener = listener; refresh(); }
    void stop() {
        listener = null;
        detach();
        artwork.clear();
    }
    private void detach() {
        if (selected != null) {
            try { selected.unregisterCallback(callback); } catch (RuntimeException ignored) { }
        }
        selected = null; frame = null;
        if (registered && sessions != null) {
            try { sessions.removeOnActiveSessionsChangedListener(sessionsChanged); } catch (RuntimeException ignored) { }
        }
        registered = false;
    }
    private void refresh() {
        if (listener == null) return;
        if (!hasAccess() || sessions == null) { detach(); listener.changed(null, false); return; }
        try {
            if (!registered) {
                sessions.addOnActiveSessionsChangedListener(sessionsChanged, accessComponent, main);
                registered = true;
            }
            List<MediaController> all = sessions.getActiveSessions(accessComponent);
            MediaController choice = null;
            for (MediaController controller : all) {
                if (!"deezer.android.app".equals(controller.getPackageName())) continue;
                PlaybackState state = controller.getPlaybackState();
                if (state == null || !eligible(state.getState())) continue;
                if (choice == null || sameSession(controller, selected)) choice = controller;
                if (state.getState() == PlaybackState.STATE_PLAYING) { choice = controller; break; }
            }
            if (!sameSession(choice, selected)) {
                if (selected != null) selected.unregisterCallback(callback);
                selected = choice;
                sessionId++;
                if (selected != null) selected.registerCallback(callback, main);
            }
            publish();
        } catch (RuntimeException unavailable) {
            detach();
            if (listener != null) listener.changed(null, hasAccess());
        }
    }
    private void publish() {
        if (listener == null) return;
        if (selected == null) { frame = null; listener.changed(null, true); return; }
        try {
            MediaMetadata metadata = selected.getMetadata();
            PlaybackState state = selected.getPlaybackState();
            if (metadata == null || state == null || !eligible(state.getState())) {
                frame = null; listener.changed(null, true); return;
            }
            String title = first(metadata, MediaMetadata.METADATA_KEY_TITLE, MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
            String artist = first(metadata, MediaMetadata.METADATA_KEY_ARTIST, MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
                    MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            String id = LyricsLabMediaPolicy.recordingId(selected.getPackageName(),
                    text(metadata, "com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_TYPE"),
                    text(metadata, "com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_ID"));
            Bitmap image = artwork.resolve(metadata, null);
            boolean known = state.getPosition() >= 0 && state.getLastPositionUpdateTime() > 0
                    && !Float.isNaN(state.getPlaybackSpeed()) && !Float.isInfinite(state.getPlaybackSpeed());
            NowPlayingSnapshot snapshot = new NowPlayingSnapshot(sessionId, selected.getPackageName(), title, artist,
                    state.getState(), state.getActions(), state.getPosition(), metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                    state.getPlaybackSpeed(), state.getLastPositionUpdateTime(), image, "", text(metadata, MediaMetadata.METADATA_KEY_ALBUM));
            frame = new Frame(snapshot, id, known);
            listener.changed(frame, true);
        } catch (RuntimeException unavailable) { frame = null; listener.changed(null, true); }
    }
    private static boolean sameSession(MediaController a, MediaController b) {
        return a == b || a != null && b != null && a.getSessionToken().equals(b.getSessionToken());
    }
    private static boolean eligible(int state) {
        return state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED
                || state == PlaybackState.STATE_BUFFERING || state == PlaybackState.STATE_CONNECTING;
    }
    private static String text(MediaMetadata metadata, String key) {
        CharSequence value = metadata.getText(key); return value == null ? "" : value.toString().trim();
    }
    private static String first(MediaMetadata metadata, String... keys) {
        for (String key : keys) { String value = text(metadata, key); if (!value.isEmpty()) return value; }
        return "";
    }
    void previous() { transport(PlaybackState.ACTION_SKIP_TO_PREVIOUS, 0); }
    void next() { transport(PlaybackState.ACTION_SKIP_TO_NEXT, 1); }
    void toggle() {
        if (selected == null || frame == null || !frame.snapshot.canPlayPause()) return;
        try {
            if (frame.snapshot.isPlaying()) selected.getTransportControls().pause();
            else selected.getTransportControls().play();
        } catch (RuntimeException unavailable) { refresh(); }
    }
    void seek(long delta) {
        if (selected == null || frame == null || !frame.clockKnown || !frame.snapshot.canSeek()
                || frame.snapshot.durationMs() <= 0) return;
        long position = frame.snapshot.estimatedPositionMs(SystemClock.elapsedRealtime());
        long target = Math.max(0, Math.min(frame.snapshot.durationMs(), position + delta));
        try { selected.getTransportControls().seekTo(target); } catch (RuntimeException unavailable) { refresh(); }
    }
    private void transport(long capability, int kind) {
        if (selected == null || frame == null || (frame.snapshot.actions() & capability) == 0) return;
        try {
            if (kind == 0) selected.getTransportControls().skipToPrevious();
            else selected.getTransportControls().skipToNext();
        } catch (RuntimeException unavailable) { refresh(); }
    }
}
