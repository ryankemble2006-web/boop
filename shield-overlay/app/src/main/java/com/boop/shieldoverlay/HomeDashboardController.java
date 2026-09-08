package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HomeDashboardController {
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

    public HomeDashboardController(AreaInfo room, RepositoryPort repository, CachePort cache, Listener listener) {
        if (room == null) throw new IllegalArgumentException("room is required");
        if (repository == null) throw new IllegalArgumentException("repository is required");
        if (cache == null) throw new IllegalArgumentException("cache is required");
        if (listener == null) throw new IllegalArgumentException("listener is required");
        this.room = room; this.repository = repository; this.cache = cache; this.listener = listener;
    }

    public void start() {
        repository.loadDashboard(room, (snapshot, error) -> {
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
            cards = Collections.unmodifiableList(new ArrayList<>(scoped)); status = Status.LIVE; toggleInFlight = false;
            message = rejectedUnscoped ? "I hid controls that aren't confirmed in " + room.name() + "." : null;
            cache.clear(room); emit();
        });
    }

    /** Legacy call retained for old callers; there is no favourite to toggle now. */
    public void toggleFavourite() { }
    public void markOffline(String reason) { status = Status.STALE; toggleInFlight = false; message = plainError(reason, "Home Assistant is offline."); emit(); }

    private void toggleCard(EntityCard requested) {
        if (status != Status.LIVE || requested == null || toggleInFlight) return;
        EntityCard current = findCard(requested.entityId());
        if (current == null || !RoomScopedEntities.belongsTo(room, current)) return;
        toggleInFlight = true; emit();
        repository.toggleBinary(current, (success, confirmed, error) -> {
            toggleInFlight = false;
            if (!success || confirmed == null || !RoomScopedEntities.belongsTo(room, confirmed)) {
                status = Status.STALE; message = plainError(error, "Home Assistant didn't confirm that room control."); emit(); return;
            }
            replaceCard(confirmed); status = Status.LIVE; message = null; emit();
        });
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
    private void emit() { listener.onViewState(new ViewState(status, cards, status == Status.LIVE && !cards.isEmpty() && !toggleInFlight, message, this::toggleCard)); }
    private static String plainError(String value, String fallback) { String clean = clean(value); return clean == null ? fallback : clean; }
    private static String clean(String value) { if (value == null) return null; String trimmed = value.trim(); return trimmed.isEmpty() ? null : trimmed; }
}
