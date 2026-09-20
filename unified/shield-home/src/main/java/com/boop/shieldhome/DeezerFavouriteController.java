package com.boop.shieldhome;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/** Shared, lifecycle-bound favourite control over BOOP's existing selected media session. */
final class DeezerFavouriteController {
    interface Listener { void changed(State state); }
    static final class State {
        final long session;
        final String track, mediaIdentity;
        final int saved;
        final boolean canAdd, canRemove, pending;
        State(long session, String track, String identity, int saved,
                boolean canAdd, boolean canRemove, boolean pending) {
            this.session = session; this.track = track; mediaIdentity = identity;
            this.saved = saved; this.canAdd = canAdd; this.canRemove = canRemove;
            this.pending = pending;
        }
        boolean matches(NowPlayingSnapshot snapshot) {
            return snapshot != null && DeezerFavouritePolicy.sameTrack(
                    session, track, snapshot.sessionId(), trackIdentity(snapshot));
        }
    }
    private static DeezerFavouriteController instance;
    private final Context context;
    private final ShieldNowPlayingManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Listener> listeners = new ArrayList<>();
    private final DeezerFavouriteRequest request = new DeezerFavouriteRequest();
    private Runnable unsubscribe;
    private MediaController player;
    private MediaSession.Token token;
    private MediaController.Callback callback;
    private PlaybackState.CustomAction addAction, removeAction;
    private boolean heartRating;
    private long bindingGeneration;
    private State state = empty();
    private final Runnable timeout = () -> {
        refresh();
        if (request.expired(SystemClock.elapsedRealtime())) {
            request.cancel();
            message("Deezer didn't confirm the favourite change.");
            refresh();
        }
        stopIfIdle();
    };

    static DeezerFavouriteController get(Context context) {
        requireMain();
        if (instance == null) instance = new DeezerFavouriteController(context.getApplicationContext());
        return instance;
    }
    private DeezerFavouriteController(Context context) {
        this.context = context;
        manager = ShieldNowPlayingManager.get(context);
    }
    Runnable subscribe(Listener listener) {
        requireMain();
        listeners.add(listener);
        if (unsubscribe == null) unsubscribe = manager.state().subscribe(ignored -> refresh());
        else refresh();
        return () -> { requireMain(); listeners.remove(listener); stopIfIdle(); };
    }
    private void stopIfIdle() {
        if (!listeners.isEmpty() || request.pending()) return;
        if (unsubscribe != null) { Runnable end = unsubscribe; unsubscribe = null; end.run(); }
        unbind();
        state = empty();
    }
    private void unbind() {
        bindingGeneration++;
        if (player != null && callback != null) {
            try { player.unregisterCallback(callback); } catch (RuntimeException ignored) { }
        }
        player = null; token = null; callback = null;
    }
    private void bind(MediaSession.Token next) {
        if (next != null && next.equals(token) && player != null) return;
        unbind();
        if (next == null) return;
        player = new MediaController(context, next);
        token = next;
        final long generation = bindingGeneration;
        callback = new MediaController.Callback() {
            @Override public void onMetadataChanged(MediaMetadata metadata) {
                if (generation == bindingGeneration) refresh();
            }
            @Override public void onPlaybackStateChanged(PlaybackState playback) {
                if (generation == bindingGeneration) refresh();
            }
            @Override public void onSessionDestroyed() {
                if (generation != bindingGeneration) return;
                unbind(); request.cancel(); handler.removeCallbacks(timeout);
                state = empty(); publish(); stopIfIdle();
            }
        };
        player.registerCallback(callback, handler);
    }
    private void refresh() {
        requireMain();
        addAction = null; removeAction = null; heartRating = false;
        State next = empty();
        try {
            NowPlayingSnapshot owner = manager.state().current();
            if (owner != null && "deezer.android.app".equals(owner.packageName())
                    && !owner.title().isEmpty() && NowPlayingSelectionPolicy.eligible(owner.playbackState())) {
                bind(manager.sessionToken(owner.sessionId()));
                MediaMetadata metadata = player == null ? null : player.getMetadata();
                PlaybackState playback = player == null ? null : player.getPlaybackState();
                if (metadata != null && playback != null && metadataMatches(owner, metadata)) {
                    Boolean heart = null;
                    Rating rating = metadata.getRating(MediaMetadata.METADATA_KEY_USER_RATING);
                    if (rating != null && rating.getRatingStyle() == Rating.RATING_HEART && rating.isRated())
                        heart = rating.hasHeart();
                    heartRating = player.getRatingType() == Rating.RATING_HEART
                            && (playback.getActions() & PlaybackState.ACTION_SET_RATING) != 0;
                    boolean multipleAdd = false, multipleRemove = false;
                    for (PlaybackState.CustomAction action : playback.getCustomActions()) {
                        if (action == null || action.getAction() == null || action.getAction().isEmpty()) continue;
                        int kind = DeezerFavouritePolicy.action(action.getName());
                        if (kind == 1) {
                            if (addAction != null && !addAction.getAction().equals(action.getAction())) multipleAdd = true;
                            addAction = action;
                        } else if (kind == -1) {
                            if (removeAction != null && !removeAction.getAction().equals(action.getAction())) multipleRemove = true;
                            removeAction = action;
                        }
                    }
                    if (multipleAdd) addAction = null;
                    if (multipleRemove) removeAction = null;
                    String track = trackIdentity(owner);
                    String identity = track + "\n" + text(metadata, MediaMetadata.METADATA_KEY_MEDIA_ID);
                    int saved = DeezerFavouritePolicy.state(heart, addAction != null, removeAction != null);
                    next = new State(owner.sessionId(), track, identity, saved,
                            heartRating || addAction != null, heartRating || removeAction != null, false);
                }
            } else unbind();
        } catch (RuntimeException unavailable) {
            unbind(); addAction = null; removeAction = null; heartRating = false;
        }
        if (request.pending()) {
            if (!request.owns(next.session, next.mediaIdentity)) {
                request.cancel(); handler.removeCallbacks(timeout);
            } else {
                boolean target = request.target();
                if (request.confirm(next.session, next.mediaIdentity, next.saved)) {
                    handler.removeCallbacks(timeout);
                    message(target ? "Added to Deezer favourites." : "Removed from Deezer favourites.");
                }
            }
        }
        state = new State(next.session, next.track, next.mediaIdentity, next.saved,
                next.canAdd, next.canRemove, request.pending());
        publish();
        stopIfIdle();
    }
    void change(NowPlayingSnapshot displayed, State displayedState, Boolean explicitTarget) {
        requireMain();
        refresh();
        if (displayedState == null || !state.matches(displayed)
                || !DeezerFavouritePolicy.sameTrack(displayedState.session, displayedState.mediaIdentity,
                        state.session, state.mediaIdentity) || player == null) {
            message("Track changed or favourites are unavailable here."); return;
        }
        if (request.pending()) { message("Waiting for Deezer to confirm…"); return; }
        if (explicitTarget == null && state.saved == DeezerFavouritePolicy.UNKNOWN) {
            message("Deezer isn't sharing this track's favourite state."); return;
        }
        boolean target = explicitTarget != null ? explicitTarget : state.saved == DeezerFavouritePolicy.NOT_SAVED;
        if (state.saved == (target ? DeezerFavouritePolicy.SAVED : DeezerFavouritePolicy.NOT_SAVED)) {
            message(target ? "Already in Deezer favourites." : "Not in Deezer favourites."); return;
        }
        if (!(target ? state.canAdd : state.canRemove)) {
            message("Deezer isn't exposing this favourite control."); return;
        }
        if (!request.begin(state.session, state.mediaIdentity, target, SystemClock.elapsedRealtime())) return;
        handler.postDelayed(timeout, DeezerFavouriteRequest.TIMEOUT_MS);
        try {
            // Only advertised heart ratings or the actual published custom action are sent.
            // Never reinterpret thumbs-down/dislike as removal from favourites.
            if (heartRating) player.getTransportControls().setRating(Rating.newHeartRating(target));
            else player.getTransportControls().sendCustomAction(target ? addAction : removeAction, null);
        } catch (RuntimeException unavailable) {
            request.cancel(); handler.removeCallbacks(timeout);
            message("Couldn't change Deezer favourites just now.");
        }
        refresh();
    }
    private void publish() {
        for (Listener listener : new ArrayList<>(listeners)) listener.changed(state);
    }
    private void message(String value) {
        if (!listeners.isEmpty()) Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
    }
    static String trackIdentity(NowPlayingSnapshot snapshot) {
        return snapshot.trackKey() + "\n" + snapshot.album() + "\n" + snapshot.durationMs();
    }
    private static boolean metadataMatches(NowPlayingSnapshot owner, MediaMetadata metadata) {
        String title = text(metadata, MediaMetadata.METADATA_KEY_TITLE);
        if (title.isEmpty()) title = text(metadata, MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        String artist = text(metadata, MediaMetadata.METADATA_KEY_ARTIST);
        if (artist.isEmpty()) artist = text(metadata, MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        if (artist.isEmpty()) artist = text(metadata, MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        return owner.title().equals(title) && owner.subtitle().equals(artist)
                && owner.album().equals(text(metadata, MediaMetadata.METADATA_KEY_ALBUM))
                && owner.durationMs() == Math.max(0L, metadata.getLong(MediaMetadata.METADATA_KEY_DURATION));
    }
    private static String text(MediaMetadata metadata, String key) {
        CharSequence text = metadata.getText(key);
        return text == null ? "" : text.toString().trim();
    }
    private static State empty() { return new State(0, "", "", -1, false, false, false); }
    private static void requireMain() {
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Main thread required");
    }
}
