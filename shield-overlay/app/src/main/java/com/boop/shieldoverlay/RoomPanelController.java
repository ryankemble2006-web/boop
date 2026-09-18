package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Single-threaded launcher state machine. Transport callbacks must return on its owner thread. */
public final class RoomPanelController {
    public enum Phase { HIDDEN, NO_ROOM, CONNECTING, LIVE, OFFLINE, AUTH_REQUIRED }
    public interface RoomSource { AreaInfo selectedRoom(); }
    public interface Scheduler { Runnable later(long delayMs, Runnable task); }
    public interface Listener { void onState(State state); }
    public interface LoadCallback { void onResult(DashboardSnapshot snapshot, String error); }
    public interface ActionCallback {
        void onObserved(EntityCard card);
        void onResult(boolean success, EntityCard card, String error);
    }
    public interface Events {
        void onReady();
        void onState(String entityId, String state);
        void onOffline(boolean authenticationRequired);
    }
    public interface Connection {
        void load(LoadCallback callback);
        void toggle(EntityCard card, ActionCallback callback);
        void close();
    }
    /** open must return before it invokes any Events callback. */
    public interface Transport { Connection open(AreaInfo room, Events events); }

    public static final class State {
        public final long generation;
        public final AreaInfo room;
        public final Phase phase;
        public final List<EntityCard> cards;
        public final String message;
        private final String pendingId;
        State(long generation, AreaInfo room, Phase phase, List<EntityCard> cards,
                String pendingId, String message) {
            this.generation = generation; this.room = room; this.phase = phase;
            this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
            this.pendingId = pendingId; this.message = message;
        }
        public boolean pending(String id) { return id != null && id.equals(pendingId); }
        public boolean actionable() { return phase == Phase.LIVE && pendingId == null; }
    }

    private final RoomSource source;
    private final Transport transport;
    private final Scheduler scheduler;
    private final Listener listener;
    private final Map<String, String> observations = new HashMap<>();
    private final Map<String, Long> eventVersions = new HashMap<>();
    private long eventVersion;
    private List<EntityCard> cards = Collections.emptyList();
    private AreaInfo room;
    private Connection connection;
    private Phase phase = Phase.HIDDEN;
    private boolean active, loading;
    private long generation, loadSequence, actionSequence;
    private String pendingId, message;
    private int failures;
    private Runnable cancelTimer;

    public RoomPanelController(RoomSource source, Transport transport, Scheduler scheduler, Listener listener) {
        if (source == null || transport == null || scheduler == null || listener == null)
            throw new IllegalArgumentException("Room panel dependencies are required");
        this.source = source; this.transport = transport; this.scheduler = scheduler; this.listener = listener;
    }

    public void start() {
        AreaInfo selected = source.selectedRoom();
        if (active && sameRoom(selected, room) && (phase == Phase.LIVE || phase == Phase.CONNECTING)) return;
        active = true;
        begin(selected);
    }

    public void stop() {
        active = false;
        invalidate();
        room = null; cards = Collections.emptyList(); message = null; phase = Phase.HIDDEN;
        emit();
    }

    private void begin(AreaInfo selected) {
        invalidate();
        room = selected; cards = Collections.emptyList(); message = null;
        phase = room == null ? Phase.NO_ROOM : Phase.CONNECTING;
        emit();
        if (room == null) return;
        final long token = generation;
        arm(20000, () -> offline(token, false));
        try {
            connection = transport.open(room, new Events() {
                public void onReady() { if (valid(token)) load(null); }
                public void onState(String id, String actual) {
                    if (!valid(token) || id == null) return;
                    String value = actual == null ? "unavailable" : actual;
                    if (loading) observations.put(id, value);
                    EntityCard current = find(id);
                    if (current != null && !value.equals(current.state())) {
                        eventVersions.put(id, ++eventVersion);
                        replace(current.withState(value)); emit();
                    }
                }
                public void onOffline(boolean reauth) { offline(token, reauth); }
            });
        } catch (RuntimeException unavailable) { offline(token, false); }
    }

    /** The UI passes the generation it rendered, so a stale tile can never operate a new room. */
    public void toggle(long renderedGeneration, String entityId) {
        if (!active || renderedGeneration != generation) return;
        if (!sameRoom(source.selectedRoom(), room)) { begin(source.selectedRoom()); return; }
        if (phase != Phase.LIVE || pendingId != null) return;
        EntityCard requested = find(entityId);
        if (!RoomDeviceControls.isActionable(requested)
                || !RoomScopedEntities.belongsTo(room, requested)) return;
        pendingId = entityId; message = null;
        emit();
        // The live card set is already room-scoped and refreshed in the background.
        // Do not block a button press on registry and whole-state round trips.
        send(generation, requested);
    }

    private void load(EntityCard requested) {
        if (connection == null || !active) return;
        final long token = generation, sequence = ++loadSequence;
        loading = true; observations.clear();
        arm(15000, () -> offline(token, false));
        try {
            connection.load((snapshot, error) -> {
                if (!valid(token) || sequence != loadSequence) return;
                loading = false;
                if (error != null || snapshot == null) { offline(token, false); return; }
                if (!sameRoom(room, snapshot.room())) {
                    cards = Collections.emptyList(); pendingId = null;
                    phase = Phase.LIVE; message = "This room could not be confirmed. Controls are hidden.";
                    observations.clear(); emit(); scheduleRefresh(); return;
                }
                List<EntityCard> next = new ArrayList<>();
                for (EntityCard card : RoomScopedEntities.keep(room, snapshot.cards())) {
                    if (!RoomDeviceControls.isActionable(card)) continue;
                    String actual = observations.get(card.entityId());
                    next.add(actual == null ? card : card.withState(actual));
                }
                cards = next; observations.clear(); phase = Phase.LIVE; failures = 0;
                if (requested == null) { message = null; emit(); scheduleRefresh(); return; }
                EntityCard fresh = find(requested.entityId());
                if (!RoomDeviceControls.isActionable(fresh)) {
                    pendingId = null; message = "That device is no longer available in this room.";
                    emit(); scheduleRefresh(); return;
                }
                String target = "on".equals(requested.state()) ? "off" : "on";
                // Someone else already reached the requested state; do not invert it again.
                if (target.equals(fresh.state())) {
                    pendingId = null; emit(); scheduleRefresh(); return;
                }
                send(token, fresh);
            });
        } catch (RuntimeException unavailable) { offline(token, false); }
    }

    private void send(long token, EntityCard target) {
        final long action = ++actionSequence;
        final long observedBefore = eventVersions.getOrDefault(target.entityId(), 0L);
        arm(15000, () -> offline(token, false));
        try {
            connection.toggle(target, new ActionCallback() {
                private boolean done;
                private boolean current() { return !done && valid(token) && action == actionSequence; }
                private boolean confirmed(EntityCard card) {
                    return card != null && target.entityId().equals(card.entityId())
                            && RoomScopedEntities.belongsTo(room, card) && RoomDeviceControls.isActionable(card);
                }
                private boolean noNewerEvent() {
                    return eventVersions.getOrDefault(target.entityId(), 0L) == observedBefore;
                }
                public void onObserved(EntityCard card) {
                    if (current() && confirmed(card) && noNewerEvent()) { replace(card); emit(); }
                }
                public void onResult(boolean success, EntityCard card, String error) {
                    if (!current()) return;
                    done = true; pendingId = null;
                    if (success) {
                        if (confirmed(card) && noNewerEvent()) replace(card);
                        message = null;
                    } else {
                        message = "Home Assistant did not accept that change. Please try again.";
                    }
                    emit(); scheduleRefresh();
                }
            });
        } catch (RuntimeException unavailable) { offline(token, false); }
    }

    private void scheduleRefresh() {
        arm(30000, () -> {
            if (!active) return;
            if (!sameRoom(source.selectedRoom(), room)) begin(source.selectedRoom());
            else if (pendingId == null) load(null);
        });
    }

    private void offline(long token, boolean reauth) {
        if (!valid(token)) return;
        invalidate();
        phase = reauth ? Phase.AUTH_REQUIRED : Phase.OFFLINE;
        message = reauth ? "Reconnect Home Assistant in BOOP settings." : "Home Assistant unavailable. Reconnecting…";
        emit();
        if (!reauth) arm(Math.min(30000L, 3000L * ++failures), () -> {
            if (active) begin(source.selectedRoom());
        });
    }

    private boolean valid(long token) {
        if (!active || token != generation) return false;
        if (!sameRoom(source.selectedRoom(), room)) { begin(source.selectedRoom()); return false; }
        return true;
    }

    private void invalidate() {
        ++generation; ++loadSequence; ++actionSequence;
        pendingId = null; loading = false; observations.clear(); eventVersions.clear();
        if (cancelTimer != null) { cancelTimer.run(); cancelTimer = null; }
        Connection old = connection; connection = null;
        if (old != null) old.close();
    }
    private void arm(long delay, Runnable task) {
        if (cancelTimer != null) cancelTimer.run();
        cancelTimer = scheduler.later(delay, task);
    }
    private EntityCard find(String id) {
        for (EntityCard card : cards) if (card.entityId().equals(id)) return card;
        return null;
    }
    private void replace(EntityCard card) {
        ArrayList<EntityCard> copy = new ArrayList<>(cards);
        for (int i = 0; i < copy.size(); i++) if (copy.get(i).entityId().equals(card.entityId())) { copy.set(i, card); break; }
        cards = copy;
    }
    private void emit() { listener.onState(new State(generation, room, phase, cards, pendingId, message)); }
    private static boolean sameRoom(AreaInfo a, AreaInfo b) {
        return a == null ? b == null : b != null && a.id().equals(b.id());
    }
}
