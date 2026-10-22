package com.boop.shieldhome;

import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import java.util.*;
import java.util.function.LongSupplier;

/** Event-driven native queue. No polling, UI automation, catalogue replacement or account access. */
final class DeezerQueueController {
    static final String CONTEXT_TYPE = "com.deezer.METADATA_KEY_STREAM_CONTEXT_TYPE";
    static final String CONTEXT_ID = "com.deezer.METADATA_KEY_STREAM_CONTEXT_ID";
    private static final String DEEZER = "deezer.android.app";
    private static final long VERIFIED_NATIVE_VERSION = 301000101L;
    private static final long CONFIRM_MS = 4000L;
    enum Result { REQUESTED, CURRENT, BUSY, STALE, UNAVAILABLE }
    interface Listener { void changed(State state); }
    static final class Row {
        final long id;
        final String title, artist, description, mediaId;
        Row(MediaSession.QueueItem item) {
            id = item.getQueueId(); MediaDescription d = item.getDescription();
            title = clean(d.getTitle()); artist = clean(d.getSubtitle());
            description = clean(d.getDescription()); mediaId = clean(d.getMediaId());
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Row)) return false;
            Row r = (Row) other;
            return id == r.id && title.equals(r.title) && artist.equals(r.artist)
                    && description.equals(r.description) && mediaId.equals(r.mediaId);
        }
        @Override public int hashCode() { return Objects.hash(id, title, artist, description, mediaId); }
    }
    static final class State {
        final long session, generation, activeId, pendingId;
        final String context, title, message;
        final List<Row> rows;
        final boolean allowed, visible, canSelect, pending;
        State(long session, long generation, String context, String title, List<Row> rows,
                long activeId, boolean allowed, boolean visible, boolean canSelect, long pendingId, String message) {
            this.session = session; this.generation = generation; this.context = context; this.title = title;
            this.rows = Collections.unmodifiableList(new ArrayList<>(rows)); this.activeId = activeId;
            this.allowed = allowed; this.visible = visible; this.canSelect = canSelect;
            this.pendingId = pendingId; pending = pendingId >= 0; this.message = message;
        }
        int indexOf(long id) { for (int i = 0; i < rows.size(); i++) if (rows.get(i).id == id) return i; return -1; }
    }
    private final Handler main = new Handler(Looper.getMainLooper());
    private final LongSupplier providerVersion;
    private final List<Listener> listeners = new ArrayList<>();
    private MediaController player;
    private MediaSession.Token token, blockedToken;
    private long session, generation, version, pendingId = -1, pendingGeneration;
    private String blockedContext = "", message = "";
    private List<Row> rows = Collections.emptyList();
    private boolean validRows;
    private State state = empty(0);
    private final Runnable timeout = () -> {
        refresh();
        if (pendingId >= 0) { cancelPending(); message = "Deezer did not confirm the selected track."; refresh(); }
    };

    DeezerQueueController(LongSupplier providerVersion) { this.providerVersion = providerVersion; }
    State current() { return state; }
    Runnable subscribe(Listener listener) {
        listeners.add(listener); listener.changed(state);
        return () -> listeners.remove(listener);
    }
    void update(MediaController current, long selectedSession) {
        if (current == null || selectedSession <= 0) { clear(); return; }
        try {
            MediaSession.Token next = current.getSessionToken();
            if (next == null || !DEEZER.equals(current.getPackageName())) { clear(); return; }
            if (player == null || selectedSession != session || !next.equals(token)) {
                cancelPending(); player = current; token = next; session = selectedSession;
                version = readVersion(); message = ""; takeQueue(current.getQueue());
            }
            refresh();
        } catch (RuntimeException unavailable) { clear(); }
    }
    void queueChanged(MediaController owner, long ownerSession, List<MediaSession.QueueItem> queue) {
        if (!owns(owner, ownerSession)) return;
        takeQueue(queue); refresh();
    }
    private boolean owns(MediaController owner, long ownerSession) {
        return owner != null && ownerSession == session && token != null && token.equals(owner.getSessionToken());
    }
    private long readVersion() { try { return providerVersion.getAsLong(); } catch (RuntimeException unknown) { return 0; } }
    private void takeQueue(List<MediaSession.QueueItem> queue) {
        List<Row> next = new ArrayList<>(); Set<Long> ids = new HashSet<>();
        boolean valid = queue != null && !queue.isEmpty() && queue.size() <= 512;
        if (valid) for (MediaSession.QueueItem item : queue) {
            if (item == null || item.getDescription() == null || item.getQueueId() < 0 || !ids.add(item.getQueueId())) { valid = false; break; }
            Row row = new Row(item); if (row.title.isEmpty()) { valid = false; break; } next.add(row);
        }
        validRows = valid; rows = valid ? Collections.unmodifiableList(next) : Collections.emptyList();
    }
    void suppressForFlow() {
        blockedToken = token; blockedContext = state.context;
        cancelPending(); message = ""; refresh();
    }
    void clear() {
        cancelPending(); player = null; token = null; session = 0;
        blockedToken = null; blockedContext = ""; message = "";
        rows = Collections.emptyList(); validRows = false;
        if (state.session != 0 || state.allowed) generation++;
        publish(empty(generation));
    }
    private static State empty(long generation) {
        return new State(0, generation, "", "", Collections.emptyList(), -1, false, false, false, -1, "");
    }
    private void refresh() {
        if (player == null) return;
        try {
            MediaMetadata metadata = player.getMetadata(); PlaybackState playback = player.getPlaybackState();
            String type = text(metadata, CONTEXT_TYPE), id = text(metadata, CONTEXT_ID);
            String context = type + "\n" + id;
            if (blockedToken != null && (!blockedToken.equals(token)
                    || (!type.isEmpty() && !context.equals(blockedContext)))) { blockedToken = null; blockedContext = ""; }
            boolean blocked = token.equals(blockedToken) && context.equals(blockedContext);
            boolean allowed = !blocked && ("album_partner".equals(type) || "playlist_partner".equals(type))
                    && !id.isEmpty() && playback != null && NowPlayingSelectionPolicy.eligible(playback.getState());
            List<Row> shown = allowed ? rows : Collections.emptyList();
            if (state.session != session || !state.context.equals(context) || !state.rows.equals(shown)) {
                generation++; cancelPending(); message = "";
            }
            long active = playback == null ? -1 : playback.getActiveQueueItemId();
            Row activeRow = null; for (Row r : shown) if (r.id == active) { activeRow = r; break; }
            String title = text(metadata, MediaMetadata.METADATA_KEY_TITLE);
            if (title.isEmpty()) title = text(metadata, MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
            String artist = text(metadata, MediaMetadata.METADATA_KEY_ARTIST);
            if (artist.isEmpty()) artist = text(metadata, MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
            if (artist.isEmpty()) artist = text(metadata, MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            boolean coherent = activeRow != null && !title.isEmpty() && activeRow.title.equals(title) && activeRow.artist.equals(artist);
            boolean visible = allowed && validRows && coherent;
            boolean canSelect = visible && (version == VERIFIED_NATIVE_VERSION
                    || (playback.getActions() & PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM) != 0);
            if (pendingId >= 0 && (!allowed || pendingGeneration != generation)) cancelPending();
            if (pendingId == active && visible) { cancelPending(); message = ""; }
            String name = clean(player.getQueueTitle());
            if (name.isEmpty()) name = "album_partner".equals(type) ? text(metadata, MediaMetadata.METADATA_KEY_ALBUM) : "Playlist";
            publish(new State(session, generation, context, name, shown, active, allowed, visible, canSelect, pendingId, message));
        } catch (RuntimeException unavailable) { clear(); }
    }
    Result select(State displayed, Row selected) {
        if (pendingId >= 0) return Result.BUSY;
        if (player == null || displayed == null || selected == null) return Result.UNAVAILABLE;
        try {
            // Always re-read the actual queue before dispatch. Late/reordered callbacks cannot authorize a stale row.
            version = readVersion(); takeQueue(player.getQueue()); refresh();
            if (displayed.session != state.session || displayed.generation != state.generation
                    || !displayed.context.equals(state.context)) return Result.STALE;
            int index = state.indexOf(selected.id);
            if (index < 0 || !selected.equals(state.rows.get(index))) return Result.STALE;
            if (!state.visible || !state.canSelect) return Result.UNAVAILABLE;
            if (selected.id == state.activeId) return Result.CURRENT;
            pendingId = selected.id; pendingGeneration = generation; message = "";
            main.postDelayed(timeout, CONFIRM_MS); refresh();
            player.getTransportControls().skipToQueueItem(selected.id);
            return Result.REQUESTED;
        } catch (RuntimeException unavailable) {
            cancelPending(); message = "Could not select that track in Deezer."; refresh(); return Result.UNAVAILABLE;
        }
    }
    private void cancelPending() { pendingId = -1; main.removeCallbacks(timeout); }
    private void publish(State next) {
        State old = state;
        if (old.session == next.session && old.generation == next.generation && old.activeId == next.activeId
                && old.pendingId == next.pendingId && old.allowed == next.allowed && old.visible == next.visible
                && old.canSelect == next.canSelect && old.context.equals(next.context) && old.title.equals(next.title)
                && old.message.equals(next.message) && old.rows.equals(next.rows)) return;
        state = next; for (Listener listener : new ArrayList<>(listeners)) listener.changed(next);
    }
    private static String text(MediaMetadata metadata, String key) { return metadata == null ? "" : clean(metadata.getText(key)); }
    private static String clean(CharSequence value) { return value == null ? "" : value.toString().trim(); }
}
