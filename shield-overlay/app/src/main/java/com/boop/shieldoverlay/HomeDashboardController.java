package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HomeDashboardController {
    public enum Status {
        LIVE,
        STALE
    }

    public interface RepositoryPort {
        void loadDashboard(AreaInfo room, HomeAssistantRepository.DashboardCallback callback);
        void toggleBinary(EntityCard card, HomeAssistantRepository.BinaryActionCallback callback);
    }

    public interface CachePort {
        EntityCard load(AreaInfo room);
        void save(AreaInfo room, EntityCard card);
        void clear(AreaInfo room);
    }

    public interface Listener {
        void onViewState(ViewState state);
    }

    private interface CardAction {
        void toggle(EntityCard card);
    }

    public static final class ViewState {
        private final Status status;
        private final EntityCard favourite;
        private final List<EntityCard> cards;
        private final boolean actionsEnabled;
        private final String message;
        private final CardAction cardAction;

        private ViewState(
                Status status,
                EntityCard favourite,
                List<EntityCard> cards,
                boolean actionsEnabled,
                String message,
                CardAction cardAction) {
            this.status = status;
            this.favourite = favourite;
            this.cards = Collections.unmodifiableList(new ArrayList<>(
                    cards == null ? Collections.emptyList() : cards));
            this.actionsEnabled = actionsEnabled;
            this.message = clean(message);
            this.cardAction = cardAction;
        }

        public Status status() {
            return status;
        }

        public EntityCard favourite() {
            return favourite;
        }

        public List<EntityCard> cards() {
            return cards;
        }

        public boolean stale() {
            return status == Status.STALE;
        }

        public boolean actionsEnabled() {
            return actionsEnabled;
        }

        public String message() {
            return message;
        }

        public void toggle(EntityCard card) {
            if (actionsEnabled && card != null && cardAction != null) {
                cardAction.toggle(card);
            }
        }
    }

    private final AreaInfo room;
    private final RepositoryPort repository;
    private final CachePort cache;
    private final Listener listener;
    private final FavouriteSelector favouriteSelector = new FavouriteSelector();

    private EntityCard favourite;
    private List<EntityCard> cards = Collections.emptyList();
    private Status status = Status.STALE;
    private boolean toggleInFlight;
    private String message;

    public HomeDashboardController(
            AreaInfo room,
            RepositoryPort repository,
            CachePort cache,
            Listener listener) {
        if (room == null) {
            throw new IllegalArgumentException("room is required");
        }
        if (repository == null) {
            throw new IllegalArgumentException("repository is required");
        }
        if (cache == null) {
            throw new IllegalArgumentException("cache is required");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }
        this.room = room;
        this.repository = repository;
        this.cache = cache;
        this.listener = listener;
    }

    public void start() {
        final EntityCard cached = cache.load(room);
        repository.loadDashboard(room, (snapshot, error) -> {
            if (error != null || snapshot == null) {
                favourite = RoomScopedEntities.belongsTo(room, cached) ? cached : null;
                cards = favourite == null
                        ? Collections.emptyList()
                        : Collections.singletonList(favourite);
                status = Status.STALE;
                toggleInFlight = false;
                message = plainError(error, "I couldn't reach Home Assistant right now.");
                emit();
                return;
            }

            if (snapshot.room() == null || !room.id().equals(snapshot.room().id())) {
                favourite = null;
                cards = Collections.emptyList();
                status = Status.LIVE;
                toggleInFlight = false;
                message = "I couldn't confirm this room, so I hid the controls.";
                cache.clear(room);
                emit();
                return;
            }

            List<EntityCard> scoped = RoomScopedEntities.keep(room, snapshot.cards());
            boolean rejectedUnscoped = scoped.size() != snapshot.cards().size();
            favourite = favouriteSelector.select(room.id(), scoped);
            cards = favouriteFirst(favourite, scoped);
            status = Status.LIVE;
            toggleInFlight = false;
            message = rejectedUnscoped
                    ? "I hid controls that aren't confirmed in " + room.name() + "."
                    : null;
            if (favourite == null) {
                cache.clear(room);
            } else {
                cache.save(room, favourite);
            }
            emit();
        });
    }

    public void toggleFavourite() {
        toggleCard(favourite);
    }

    public void markOffline(String reason) {
        status = Status.STALE;
        toggleInFlight = false;
        message = plainError(reason, "Home Assistant is offline.");
        emit();
    }

    private void toggleCard(EntityCard requested) {
        if (status != Status.LIVE || requested == null || toggleInFlight) {
            return;
        }

        EntityCard current = findCard(requested.entityId());
        if (current == null || !RoomScopedEntities.belongsTo(room, current)) {
            return;
        }

        toggleInFlight = true;
        emit();
        repository.toggleBinary(current, (success, confirmed, error) -> {
            toggleInFlight = false;
            if (!success || confirmed == null || !RoomScopedEntities.belongsTo(room, confirmed)) {
                status = Status.STALE;
                message = plainError(error, "Home Assistant didn't confirm that room control.");
                emit();
                return;
            }

            replaceCard(confirmed);
            if (favourite != null && favourite.entityId().equals(confirmed.entityId())) {
                favourite = confirmed;
                cache.save(room, confirmed);
            }
            status = Status.LIVE;
            message = null;
            emit();
        });
    }

    private EntityCard findCard(String entityId) {
        if (entityId == null) {
            return null;
        }
        for (EntityCard card : cards) {
            if (card != null && entityId.equals(card.entityId())) {
                return card;
            }
        }
        return null;
    }

    private void replaceCard(EntityCard confirmed) {
        List<EntityCard> updated = new ArrayList<>(cards.size());
        boolean replaced = false;
        for (EntityCard card : cards) {
            if (card != null && card.entityId().equals(confirmed.entityId())) {
                updated.add(confirmed);
                replaced = true;
            } else {
                updated.add(card);
            }
        }
        if (!replaced) {
            updated.add(confirmed);
        }
        cards = Collections.unmodifiableList(updated);
    }

    private void emit() {
        listener.onViewState(new ViewState(
                status,
                favourite,
                cards,
                status == Status.LIVE && !cards.isEmpty() && !toggleInFlight,
                message,
                this::toggleCard));
    }

    private List<EntityCard> favouriteFirst(
            EntityCard favourite,
            List<EntityCard> source) {
        List<EntityCard> ordered = new ArrayList<>();
        if (favourite != null) {
            ordered.add(favourite);
        }
        if (source != null) {
            for (EntityCard card : source) {
                if (!favouriteSelector.isCandidate(room.id(), card)) {
                    continue;
                }
                if (favourite != null && favourite.entityId().equals(card.entityId())) {
                    continue;
                }
                ordered.add(card);
            }
        }
        return Collections.unmodifiableList(ordered);
    }

    private static String plainError(String value, String fallback) {
        String clean = clean(value);
        return clean == null ? fallback : clean;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
