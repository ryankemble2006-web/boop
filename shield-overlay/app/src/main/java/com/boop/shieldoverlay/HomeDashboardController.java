package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class HomeDashboardController {
    interface SettlementScheduler { Runnable schedule(Runnable task); }
    private static final ScheduledExecutorService SETTLEMENTS = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "boop-control-settlement"); thread.setDaemon(true); return thread;
    });
    private final SettlementScheduler settlementScheduler;
    private final Map<String, Runnable> settlementCancels = new HashMap<>();
    public enum Status { LIVE, STALE }
    public interface RepositoryPort {
        void loadDashboard(AreaInfo room, HomeAssistantRepository.DashboardCallback callback);
        void toggleBinary(EntityCard card, HomeAssistantRepository.BinaryActionCallback callback);
    }
    /** Legacy cache contract retained for source compatibility; Home no longer uses favourites. */
    public interface CachePort { EntityCard load(AreaInfo room); void save(AreaInfo room, EntityCard card); void clear(AreaInfo room); }
    public interface Listener { void onViewState(ViewState state); }
    private interface CardAction { void toggle(EntityCard card); }

    public static final class ViewState {
        private final Status status; private final List<EntityCard> cards; private final boolean actionsEnabled; private final String message; private final CardAction cardAction;
        private ViewState(Status status, List<EntityCard> cards, boolean actionsEnabled, String message, CardAction cardAction) {
            this.status = status;
            this.cards = Collections.unmodifiableList(new ArrayList<>(cards == null ? Collections.emptyList() : cards));
            this.actionsEnabled = actionsEnabled; this.message = clean(message); this.cardAction = cardAction;
        }
        public Status status() { return status; }
        /** Deprecated compatibility accessor. Favourites were removed from Shield Home. */
        public EntityCard favourite() { return null; }
        public List<EntityCard> cards() { return cards; }
        public boolean stale() { return status == Status.STALE; }
        public boolean actionsEnabled() { return actionsEnabled; }
        public String message() { return message; }
        public void toggle(EntityCard card) { if (actionsEnabled && card != null && cardAction != null) cardAction.toggle(card); }
    }

    private final AreaInfo room; private final RepositoryPort repository; private final CachePort cache; private final Listener listener;
    private List<EntityCard> cards = Collections.emptyList(); private Status status = Status.STALE; private boolean toggleInFlight; private String message;
    private long actionGeneration;
    private final Map<String, Long> latestActions = new HashMap<>();
    private final Map<String, String> requestedStates = new HashMap<>();

    public HomeDashboardController(AreaInfo room, RepositoryPort repository, CachePort cache, Listener listener) {
        this(room, repository, cache, listener, task -> {
            java.util.concurrent.ScheduledFuture<?> future = SETTLEMENTS.schedule(task, 10, TimeUnit.SECONDS);
            return () -> future.cancel(false);
        });
    }

    HomeDashboardController(AreaInfo room, RepositoryPort repository, CachePort cache, Listener listener,
            SettlementScheduler settlementScheduler) {
        this.settlementScheduler = settlementScheduler;
        if (room == null) throw new IllegalArgumentException("room is required");
        if (repository == null) throw new IllegalArgumentException("repository is required");
        if (cache == null) throw new IllegalArgumentException("cache is required");
        if (listener == null) throw new IllegalArgumentException("listener is required");
        this.room = room; this.repository = repository; this.cache = cache; this.listener = listener;
    }

    public synchronized void start() {
        actionGeneration++; latestActions.clear(); requestedStates.clear(); cancelSettlements();
        final long loadGeneration = actionGeneration;
        repository.loadDashboard(room, (snapshot, error) -> {
            synchronized (HomeDashboardController.this) {
            if (loadGeneration != actionGeneration) return;
            if (error != null || snapshot == null) {
                cards = Collections.emptyList(); status = Status.STALE; toggleInFlight = false;
                message = plainError(error, "I couldn't reach Home Assistant right now."); cache.clear(room); emit(); return;
            }
            if (snapshot.room() == null || !room.id().equals(snapshot.room().id())) {
                cards = Collections.emptyList(); status = Status.LIVE; toggleInFlight = false;
                message = "I couldn't confirm this room, so I hid the controls."; cache.clear(room); emit(); return;
            }
            List<EntityCard> scoped = RoomScopedEntities.keep(room, snapshot.cards());
            boolean rejectedUnscoped = scoped.size() != snapshot.cards().size();
            List<EntityCard> actionable = keepActionable(scoped);
            cards = Collections.unmodifiableList(new ArrayList<>(actionable)); status = Status.LIVE; toggleInFlight = false;
            message = rejectedUnscoped ? "I hid controls that aren't confirmed in " + room.name() + "." : null;
            cache.clear(room); emit();
            }
        });
    }

    /** Legacy call retained for old callers; there is no favourite to toggle now. */
    public void toggleFavourite() { }
    public synchronized void markOffline(String reason) { actionGeneration++; latestActions.clear(); requestedStates.clear(); cancelSettlements(); status = Status.STALE; toggleInFlight = false; message = plainError(reason, "Home Assistant is offline."); emit(); }

    private synchronized void toggleCard(EntityCard requested) {
        if (status != Status.LIVE || requested == null || toggleInFlight) return;
        EntityCard current = findCard(requested.entityId());
        if (current == null || !RoomScopedEntities.belongsTo(room, current)) return;
        final long generation = ++actionGeneration;
        Runnable oldSettlement = settlementCancels.remove(current.entityId());
        if (oldSettlement != null) oldSettlement.run();
        latestActions.put(current.entityId(), generation);
        String priorRequest = requestedStates.get(current.entityId());
        EntityCard commandBase = priorRequest == null ? current : current.withState(priorRequest);
        final String targetState = "off".equals(commandBase.state()) ? "on" : "off";
        requestedStates.put(current.entityId(), targetState);
        toggleInFlight = true; emit();
        repository.toggleBinary(commandBase, new HomeAssistantRepository.BinaryActionCallback() {
            private boolean observed;
            private boolean completed;
            private boolean resultSucceeded;
            private long recheckGeneration;
            private boolean accepted;
            private boolean latest() { return Long.valueOf(generation).equals(latestActions.get(current.entityId())); }
            private void unlock() { if (generation == actionGeneration) toggleInFlight = false; }
            @Override public void onAccepted(EntityCard requestedState) {
                synchronized (HomeDashboardController.this) {
                    if (!latest() || (completed && !resultSucceeded) || !valid(requestedState) || !targetState.equals(requestedState.state())) return;
                    if (!accepted) {
                        settlementCancels.put(current.entityId(), settlementScheduler.schedule(() -> {
                            synchronized (HomeDashboardController.this) {
                                if (!latest()) return;
                                settlementCancels.remove(current.entityId());
                                recheck();
                            }
                        }));
                    }
                    accepted = true; unlock(); emit();
                }
            }
            @Override public void onObservedState(EntityCard confirmed) {
                synchronized (HomeDashboardController.this) {
                    if (!latest() || completed || !valid(confirmed)) return;
                    observed = true;
                    replaceCard(confirmed); unlock(); message = null; emit();
                }
            }
            @Override public void onResult(boolean success, EntityCard confirmed, String error) {
                synchronized (HomeDashboardController.this) {
                    if (!latest()) return;
                    if (completed) return;
                    completed = true;
                    resultSucceeded = success;
                    unlock();
                    if (accepted && !success && !observed) {
                        // Returning to the original state may produce no state_changed event.
                        // Re-read HA rather than inventing success or freezing usable controls.
                        Runnable cancel = settlementCancels.remove(current.entityId());
                        if (cancel != null) cancel.run();
                        recheck();
                        emit(); return;
                    }
                    requestedStates.remove(current.entityId());
                    if (observed) {
                        // The actual state is known even if the separate command reply failed.
                        message = success ? null : plainError(error, "Home Assistant didn't acknowledge that command.");
                        emit(); return;
                    }
                    if (!success || !valid(confirmed)) {
                        status = Status.STALE; message = plainError(error, "Home Assistant didn't confirm that room control."); emit(); return;
                    }
                    replaceCard(confirmed); status = Status.LIVE; message = null; emit();
                }
            }
            private void recheck() {
                final long check = ++recheckGeneration;
                repository.loadDashboard(room, (snapshot, loadError) -> {
                    synchronized (HomeDashboardController.this) {
                        if (!latest() || check != recheckGeneration) return;
                        completed = true;
                        EntityCard actual = null;
                        if (loadError == null && snapshot != null && snapshot.room() != null
                                && room.id().equals(snapshot.room().id())) {
                            for (EntityCard card : snapshot.cards())
                                if (valid(card)) { actual = card; break; }
                        }
                        requestedStates.remove(current.entityId());
                        if (actual != null) { replaceCard(actual); message = null; }
                        else { status = Status.STALE; message = plainError(loadError, "I couldn't confirm that room control."); }
                        emit();
                    }
                });
            }
            private boolean valid(EntityCard confirmed) {
                return confirmed != null && current.entityId().equals(confirmed.entityId())
                        && RoomScopedEntities.belongsTo(room, confirmed)
                        && ("on".equals(confirmed.state()) || "off".equals(confirmed.state()));
            }
        });
    }

    public synchronized void stop() { actionGeneration++; latestActions.clear(); requestedStates.clear(); cancelSettlements(); }

    private void cancelSettlements() {
        for (Runnable cancel : settlementCancels.values()) cancel.run();
        settlementCancels.clear();
    }

    private EntityCard findCard(String entityId) {
        if (entityId == null) return null;
        for (EntityCard card : cards) if (card != null && entityId.equals(card.entityId())) return card;
        return null;
    }
    private void replaceCard(EntityCard confirmed) {
        List<EntityCard> updated = new ArrayList<>(cards.size()); boolean replaced = false;
        for (EntityCard card : cards) {
            if (card != null && card.entityId().equals(confirmed.entityId())) { updated.add(confirmed); replaced = true; } else updated.add(card);
        }
        if (!replaced) updated.add(confirmed); cards = Collections.unmodifiableList(updated);
    }

    private static List<EntityCard> keepActionable(List<EntityCard> source) {
        if (source == null || source.isEmpty()) return Collections.emptyList();
        List<EntityCard> result = new ArrayList<>();
        for (EntityCard card : source) {
            if (isActionable(card)) result.add(card);
        }
        return Collections.unmodifiableList(result);
    }

    private static boolean isActionable(EntityCard card) {
        if (card == null || card.hidden()) return false;
        String category = clean(card.entityCategory());
        if ("config".equalsIgnoreCase(category) || "diagnostic".equalsIgnoreCase(category)) return false;
        String domain = card.domain();
        boolean supportedDomain = "light".equals(domain)
                || "switch".equals(domain)
                || "fan".equals(domain)
                || "input_boolean".equals(domain);
        return supportedDomain && ("on".equals(card.state()) || "off".equals(card.state()));
    }

    private void emit() { listener.onViewState(new ViewState(status, cards, status == Status.LIVE && !cards.isEmpty() && !toggleInFlight, message, this::toggleCard)); }
    private static String plainError(String value, String fallback) { String clean = clean(value); return clean == null ? fallback : clean; }
    private static String clean(String value) { if (value == null) return null; String trimmed = value.trim(); return trimmed.isEmpty() ? null : trimmed; }
}
