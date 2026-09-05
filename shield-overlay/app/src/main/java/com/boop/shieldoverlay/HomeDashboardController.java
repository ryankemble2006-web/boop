package com.boop.shieldoverlay;

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

    public static final class ViewState {
        private final Status status;
        private final EntityCard favourite;
        private final boolean actionsEnabled;
        private final String message;

        private ViewState(
                Status status,
                EntityCard favourite,
                boolean actionsEnabled,
                String message) {
            this.status = status;
            this.favourite = favourite;
            this.actionsEnabled = actionsEnabled;
            this.message = clean(message);
        }

        public Status status() {
            return status;
        }

        public EntityCard favourite() {
            return favourite;
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
    }

    private final AreaInfo room;
    private final RepositoryPort repository;
    private final CachePort cache;
    private final Listener listener;
    private final FavouriteSelector favouriteSelector = new FavouriteSelector();

    private EntityCard favourite;
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
                favourite = cached;
                status = Status.STALE;
                toggleInFlight = false;
                message = plainError(error, "I couldn't reach Home Assistant right now.");
                emit();
                return;
            }

            favourite = favouriteSelector.select(room.id(), snapshot.cards());
            status = Status.LIVE;
            toggleInFlight = false;
            message = null;
            if (favourite == null) {
                cache.clear(room);
            } else {
                cache.save(room, favourite);
            }
            emit();
        });
    }

    public void toggleFavourite() {
        if (status != Status.LIVE || favourite == null || toggleInFlight) {
            return;
        }

        toggleInFlight = true;
        EntityCard requested = favourite;
        emit();
        repository.toggleBinary(requested, (success, confirmed, error) -> {
            toggleInFlight = false;
            if (!success || confirmed == null) {
                status = Status.STALE;
                message = plainError(error, "Home Assistant didn't do that.");
                emit();
                return;
            }

            favourite = confirmed;
            status = Status.LIVE;
            message = null;
            cache.save(room, confirmed);
            emit();
        });
    }

    public void markOffline(String reason) {
        status = Status.STALE;
        toggleInFlight = false;
        message = plainError(reason, "Home Assistant is offline.");
        emit();
    }

    private void emit() {
        listener.onViewState(new ViewState(
                status,
                favourite,
                status == Status.LIVE && favourite != null && !toggleInFlight,
                message));
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
