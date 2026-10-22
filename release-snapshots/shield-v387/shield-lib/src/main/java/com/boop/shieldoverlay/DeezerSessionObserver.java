package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DeezerSessionObserver {
    private static final String DEEZER_PACKAGE = "deezer.android.app";

    public interface SessionPort {
        Object token();
        String packageName();
        Integer playbackState();
        void register(Callback callback);
        void unregister(Callback callback);
    }

    public interface Callback {
        void stateChanged(Integer state);
        void destroyed();
    }

    public interface Platform {
        void register(Runnable sessionsChanged);
        void unregister(Runnable sessionsChanged);
        List<SessionPort> sessions();
    }

    private final Platform platform;
    private final MediaPuppetState output;
    private final LinkedHashMap<Object, Watcher> watchers = new LinkedHashMap<>();
    private boolean connected;
    private boolean reconciling;
    private boolean reconcilePending;
    private long generation;
    private long nextSessionId = 1L;
    private long selectedId;
    private SessionsChanged activeSessionsChanged;

    public DeezerSessionObserver(Platform platform, MediaPuppetState output) {
        if (platform == null) {
            throw new IllegalArgumentException("platform is required");
        }
        if (output == null) {
            throw new IllegalArgumentException("output is required");
        }
        this.platform = platform;
        this.output = output;
    }

    public void connect() {
        if (connected) {
            return;
        }
        connected = true;
        long callbackGeneration = ++generation;
        SessionsChanged listener = new SessionsChanged(callbackGeneration);
        activeSessionsChanged = listener;
        try {
            platform.register(listener);
            reconcile();
        } catch (SecurityException denied) {
            disconnectInternal();
        } catch (RuntimeException programmerFailure) {
            try {
                disconnectInternal();
            } catch (RuntimeException cleanupFailure) {
                programmerFailure.addSuppressed(cleanupFailure);
            }
            throw programmerFailure;
        }
    }

    public void disconnect() {
        if (!connected && activeSessionsChanged == null && watchers.isEmpty()) {
            return;
        }
        disconnectInternal();
    }

    private void reconcile() {
        if (!connected) {
            return;
        }
        if (reconciling) {
            reconcilePending = true;
            return;
        }

        reconciling = true;
        try {
            do {
                reconcilePending = false;
                reconcileOnce();
            } while (reconcilePending && connected);
        } finally {
            reconciling = false;
        }
        if (connected) {
            publishSelection();
        }
    }

    private void reconcileOnce() {
        List<SessionPort> sessions = platform.sessions();
        if (sessions == null) {
            sessions = Collections.emptyList();
        }
        LinkedHashMap<Object, Watcher> ordered = new LinkedHashMap<>();
        for (SessionPort session : sessions) {
            if (session == null || !DEEZER_PACKAGE.equals(session.packageName())) {
                continue;
            }
            Object token = session.token();
            if (token == null || ordered.containsKey(token)) {
                continue;
            }

            Watcher watcher = watchers.get(token);
            if (watcher == null) {
                watcher = new Watcher(token, session, allocateSessionId(), generation);
                watchers.put(token, watcher);
                watcher.registrationAttempted = true;
                session.register(watcher.callback);
                if (watchers.get(token) != watcher) {
                    continue;
                }
            }
            watcher.playbackState = session.playbackState();
            ordered.put(token, watcher);
        }

        for (Map.Entry<Object, Watcher> entry :
                new ArrayList<>(watchers.entrySet())) {
            if (!ordered.containsKey(entry.getKey())) {
                watchers.remove(entry.getKey());
                unregisterExpected(entry.getValue());
            }
        }
        watchers.clear();
        watchers.putAll(ordered);
    }

    private long allocateSessionId() {
        long allocated = nextSessionId++;
        if (allocated == 0L) {
            allocated = nextSessionId++;
        }
        return allocated;
    }

    private void stateChanged(Watcher watcher, long callbackGeneration, Integer state) {
        if (!isCurrent(watcher, callbackGeneration)) {
            return;
        }
        watcher.playbackState = state;
        if (!reconciling) {
            publishSelection();
        }
    }

    private void destroyed(Watcher watcher, long callbackGeneration) {
        if (!isCurrent(watcher, callbackGeneration)) {
            return;
        }
        watchers.remove(watcher.token);
        unregisterExpected(watcher);
        if (!reconciling) {
            publishSelection();
        }
    }

    private boolean isCurrent(Watcher watcher, long callbackGeneration) {
        return connected
                && generation == callbackGeneration
                && watchers.get(watcher.token) == watcher;
    }

    private void publishSelection() {
        List<DeezerPuppetPolicy.Session> sessions = new ArrayList<>();
        for (Watcher watcher : watchers.values()) {
            sessions.add(new DeezerPuppetPolicy.Session(
                    watcher.id, DEEZER_PACKAGE, watcher.playbackState));
        }
        long selected = DeezerPuppetPolicy.select(sessions, selectedId);
        selectedId = selected;
        if (selected == 0L) {
            output.updateSession(0L, null);
            return;
        }
        for (Watcher watcher : watchers.values()) {
            if (watcher.id == selected) {
                output.updateSession(watcher.id, watcher.playbackState);
                return;
            }
        }
        selectedId = 0L;
        output.updateSession(0L, null);
    }

    private void disconnectInternal() {
        connected = false;
        generation++;
        reconciling = false;
        reconcilePending = false;
        selectedId = 0L;

        List<Watcher> detached = new ArrayList<>(watchers.values());
        watchers.clear();
        SessionsChanged sessionsChanged = activeSessionsChanged;
        activeSessionsChanged = null;

        RuntimeException unexpected = null;
        for (Watcher watcher : detached) {
            try {
                unregisterExpected(watcher);
            } catch (RuntimeException failure) {
                if (unexpected == null) {
                    unexpected = failure;
                } else {
                    unexpected.addSuppressed(failure);
                }
            }
        }
        if (sessionsChanged != null) {
            try {
                platform.unregister(sessionsChanged);
            } catch (SecurityException | IllegalStateException expectedLifecycleFailure) {
                // Already detached or access was revoked; remaining cleanup is still valid.
            } catch (RuntimeException failure) {
                if (unexpected == null) {
                    unexpected = failure;
                } else {
                    unexpected.addSuppressed(failure);
                }
            }
        }
        output.updateSession(0L, null);
        if (unexpected != null) {
            throw unexpected;
        }
    }

    private void unregisterExpected(Watcher watcher) {
        if (!watcher.registrationAttempted) {
            return;
        }
        watcher.registrationAttempted = false;
        try {
            watcher.port.unregister(watcher.callback);
        } catch (SecurityException | IllegalStateException expectedLifecycleFailure) {
            // Session destruction or revoked access can race normal lifecycle cleanup.
        }
    }

    private final class SessionsChanged implements Runnable {
        private final long callbackGeneration;

        private SessionsChanged(long callbackGeneration) {
            this.callbackGeneration = callbackGeneration;
        }

        @Override
        public void run() {
            if (!connected
                    || generation != callbackGeneration
                    || activeSessionsChanged != this) {
                return;
            }
            try {
                reconcile();
            } catch (SecurityException denied) {
                disconnectInternal();
            } catch (RuntimeException programmerFailure) {
                try {
                    disconnectInternal();
                } catch (RuntimeException cleanupFailure) {
                    programmerFailure.addSuppressed(cleanupFailure);
                }
                throw programmerFailure;
            }
        }
    }

    private final class Watcher {
        private final Object token;
        private final SessionPort port;
        private final long id;
        private final Callback callback;
        private Integer playbackState;
        private boolean registrationAttempted;

        private Watcher(Object token, SessionPort port, long id, long callbackGeneration) {
            this.token = token;
            this.port = port;
            this.id = id;
            this.callback = new Callback() {
                @Override
                public void stateChanged(Integer state) {
                    DeezerSessionObserver.this.stateChanged(
                            Watcher.this, callbackGeneration, state);
                }

                @Override
                public void destroyed() {
                    DeezerSessionObserver.this.destroyed(
                            Watcher.this, callbackGeneration);
                }
            };
        }
    }
}
