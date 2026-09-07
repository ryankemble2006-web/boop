package com.boop.shieldoverlay;

import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.EYES;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_REST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public final class DeezerSessionObserverTest {
    @Test
    public void accessSubscriberBeforeRendererCannotDeliverStaleEyesAfterConnect() {
        assertAccessConnectDelivery(true);
    }

    @Test
    public void rendererBeforeAccessSubscriberEndsAtConnectedPlayingSnapshot() {
        assertAccessConnectDelivery(false);
    }

    private static void assertAccessConnectDelivery(boolean accessFirst) {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession session = new FakeSession("playing", "deezer.android.app", 3, log);
        platform.setSessions(session);
        MediaPuppetState state = new MediaPuppetState();
        DeezerSessionObserver observer = new DeezerSessionObserver(platform, state);
        // Match the listener service's synchronous access -> connect/disconnect boundary.
        MediaPuppetState.Listener accessListener = snapshot -> {
            if (snapshot.enabled && snapshot.granted && snapshot.connected) {
                observer.connect();
            } else {
                observer.disconnect();
            }
        };
        List<MediaPuppetState.Snapshot> rendered = new ArrayList<>();
        MediaPuppetState.Listener renderer = rendered::add;
        Runnable unsubscribeFirst = state.subscribe(accessFirst ? accessListener : renderer);
        Runnable unsubscribeSecond = state.subscribe(accessFirst ? renderer : accessListener);
        assertEquals(1, rendered.size());
        assertEquals(EYES, rendered.get(0).mode);

        state.updateAccess(true, true, true);

        assertEquals(HEADPHONES_PLAYING, state.snapshot().mode);
        assertEquals(HEADPHONES_PLAYING, rendered.get(rendered.size() - 1).mode);
        assertSame(state.snapshot(), rendered.get(rendered.size() - 1));
        boolean sawPlaying = false;
        for (MediaPuppetState.Snapshot snapshot : rendered) {
            if (sawPlaying) {
                assertEquals(HEADPHONES_PLAYING, snapshot.mode);
            }
            sawPlaying |= snapshot.mode == HEADPHONES_PLAYING;
        }
        int deliveries = rendered.size();
        session.fireState(3);
        assertEquals(deliveries, rendered.size());
        assertSame(state.snapshot(), rendered.get(rendered.size() - 1));

        unsubscribeFirst.run();
        unsubscribeSecond.run();
        observer.disconnect();
        assertEquals(deliveries, rendered.size());
    }

    @Test
    public void connectRegistersListAndDeezerCallbackBeforeInitialStateRead() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession deezer = new FakeSession("deezer", "deezer.android.app", 3, log);
        FakeSession lookalike =
                new FakeSession("lookalike", "deezer.android.app.beta", 3, log);
        platform.setSessions(lookalike, deezer);
        MediaPuppetState state = eligibleState();

        new DeezerSessionObserver(platform, state).connect();

        assertTrue(log.indexOf("platform.register") < log.indexOf("platform.sessions"));
        assertTrue(log.indexOf("deezer.register") < log.indexOf("deezer.read"));
        assertEquals(0, lookalike.registerCount);
        assertEquals(0, lookalike.readCount);
        assertEquals(HEADPHONES_PLAYING, state.snapshot().mode);
        assertTrue(state.snapshot().sessionId > 0L);
    }

    @Test
    public void equalTokenWrapperKeepsIdButReattachGetsNewId() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession first = new FakeSession(new String("same"), "deezer.android.app", 3, log);
        platform.setSessions(first);
        MediaPuppetState state = eligibleState();
        DeezerSessionObserver observer = new DeezerSessionObserver(platform, state);
        observer.connect();
        long firstId = state.snapshot().sessionId;

        FakeSession replacement =
                new FakeSession(new String("same"), "deezer.android.app", 2, log);
        platform.setSessions(replacement);
        platform.fireSessionsChanged();

        assertEquals(firstId, state.snapshot().sessionId);
        assertEquals(HEADPHONES_REST, state.snapshot().mode);
        assertEquals(0, replacement.registerCount);
        assertEquals(1, replacement.readCount);

        platform.setSessions();
        platform.fireSessionsChanged();
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(1, first.unregisterCount);

        FakeSession reattached =
                new FakeSession(new String("same"), "deezer.android.app", 3, log);
        platform.setSessions(reattached);
        platform.fireSessionsChanged();

        assertNotEquals(firstId, state.snapshot().sessionId);
        assertTrue(state.snapshot().sessionId > 0L);
        assertEquals(1, reattached.registerCount);
    }

    @Test
    public void callbacksRecomputeSelectionUsingPolicy() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession first = new FakeSession("first", "deezer.android.app", 2, log);
        FakeSession second = new FakeSession("second", "deezer.android.app", 2, log);
        platform.setSessions(first, second);
        MediaPuppetState state = eligibleState();
        new DeezerSessionObserver(platform, state).connect();
        long firstId = state.snapshot().sessionId;

        second.fireState(3);
        long secondId = state.snapshot().sessionId;
        assertNotEquals(firstId, secondId);
        assertEquals(HEADPHONES_PLAYING, state.snapshot().mode);

        first.fireState(3);
        assertEquals(secondId, state.snapshot().sessionId);

        second.fireState(2);
        assertEquals(firstId, state.snapshot().sessionId);
        assertEquals(HEADPHONES_PLAYING, state.snapshot().mode);
    }

    @Test
    public void multiControllerReconcilePublishesOnlyFinalSelection() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession resting = new FakeSession("resting", "deezer.android.app", 2, log);
        platform.setSessions(resting);
        MediaPuppetState state = eligibleState();
        DeezerSessionObserver observer = new DeezerSessionObserver(platform, state);
        observer.connect();
        List<MediaPuppetState.Snapshot> changes = new ArrayList<>();
        Runnable unsubscribe = state.subscribe(changes::add);
        changes.clear();

        FakeSession playing = new FakeSession("playing", "deezer.android.app", 3, log);
        FakeSession recreatedResting =
                new FakeSession("resting", "deezer.android.app", 2, log);
        platform.setSessions(playing, recreatedResting);
        platform.fireSessionsChanged();

        assertEquals(1, changes.size());
        assertEquals(HEADPHONES_PLAYING, changes.get(0).mode);
        assertTrue(changes.get(0).sessionId > 0L);
        unsubscribe.run();
    }

    @Test
    public void callbackFromDetachedWatcherIsIgnored() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession session = new FakeSession("gone", "deezer.android.app", 3, log);
        platform.setSessions(session);
        MediaPuppetState state = eligibleState();
        new DeezerSessionObserver(platform, state).connect();

        platform.setSessions();
        platform.fireSessionsChanged();
        session.fireRetainedState(2);
        session.destroyRetained();

        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);
    }

    @Test
    public void destroyedSelectedSessionChoosesRetainedReplacement() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession playing = new FakeSession("playing", "deezer.android.app", 3, log);
        FakeSession resting = new FakeSession("resting", "deezer.android.app", 2, log);
        platform.setSessions(playing, resting);
        MediaPuppetState state = eligibleState();
        new DeezerSessionObserver(platform, state).connect();
        long playingId = state.snapshot().sessionId;

        playing.destroy();

        assertNotEquals(playingId, state.snapshot().sessionId);
        assertTrue(state.snapshot().sessionId > 0L);
        assertEquals(HEADPHONES_REST, state.snapshot().mode);
        assertEquals(1, playing.unregisterCount);
    }

    @Test
    public void duplicateConnectAndDisconnectAreBoundaryIdempotent() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession session = new FakeSession("only", "deezer.android.app", 3, log);
        platform.setSessions(session);
        MediaPuppetState state = eligibleState();
        DeezerSessionObserver observer = new DeezerSessionObserver(platform, state);

        observer.connect();
        observer.connect();
        observer.disconnect();
        observer.disconnect();

        assertEquals(1, platform.registerCount);
        assertEquals(1, platform.unregisterCount);
        assertEquals(1, session.registerCount);
        assertEquals(1, session.unregisterCount);
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);
    }

    @Test
    public void callbackFromPreviousConnectionCannotMutateReconnectedObserver() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession old = new FakeSession("same", "deezer.android.app", 3, log);
        platform.setSessions(old);
        MediaPuppetState state = eligibleState();
        DeezerSessionObserver observer = new DeezerSessionObserver(platform, state);
        observer.connect();
        observer.disconnect();

        FakeSession current = new FakeSession("same", "deezer.android.app", 2, log);
        platform.setSessions(current);
        observer.connect();
        long currentId = state.snapshot().sessionId;
        old.fireRetainedState(3);

        assertEquals(currentId, state.snapshot().sessionId);
        assertEquals(HEADPHONES_REST, state.snapshot().mode);
    }

    @Test
    public void platformRegistrationSecurityFailureCleansUpToEyes() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        platform.throwOnRegister = true;
        MediaPuppetState state = eligibleState();

        new DeezerSessionObserver(platform, state).connect();

        assertEquals(1, platform.registerCount);
        assertEquals(1, platform.unregisterCount);
        assertNull(platform.registeredCallback);
        assertEquals(EYES, state.snapshot().mode);
        assertEquals(0L, state.snapshot().sessionId);
    }

    @Test
    public void sessionRegistrationSecurityFailureCleansAllBoundariesToEyes() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession session = new FakeSession("denied", "deezer.android.app", 3, log);
        session.throwOnRegister = true;
        platform.setSessions(session);
        MediaPuppetState state = eligibleState();

        new DeezerSessionObserver(platform, state).connect();

        assertEquals(1, session.unregisterCount);
        assertEquals(1, platform.unregisterCount);
        assertEquals(EYES, state.snapshot().mode);
        assertEquals(0L, state.snapshot().sessionId);
    }

    @Test
    public void stateReadSecurityFailureCleansAllBoundariesToEyes() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession session = new FakeSession("denied", "deezer.android.app", 3, log);
        session.throwOnRead = true;
        platform.setSessions(session);
        MediaPuppetState state = eligibleState();

        new DeezerSessionObserver(platform, state).connect();

        assertEquals(1, session.unregisterCount);
        assertEquals(1, platform.unregisterCount);
        assertEquals(EYES, state.snapshot().mode);
        assertEquals(0L, state.snapshot().sessionId);
    }

    @Test
    public void disconnectContinuesCleanupWhenOneSessionUnregisterIsDenied() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession denied = new FakeSession("denied", "deezer.android.app", 3, log);
        FakeSession other = new FakeSession("other", "deezer.android.app", 2, log);
        platform.setSessions(denied, other);
        MediaPuppetState state = eligibleState();
        DeezerSessionObserver observer = new DeezerSessionObserver(platform, state);
        observer.connect();
        denied.throwOnUnregister = true;

        observer.disconnect();

        assertEquals(1, denied.unregisterCount);
        assertEquals(1, other.unregisterCount);
        assertEquals(1, platform.unregisterCount);
        assertEquals(EYES, state.snapshot().mode);
    }

    @Test
    public void unexpectedProgrammerFailureIsNotSwallowed() {
        List<String> log = new ArrayList<>();
        FakePlatform platform = new FakePlatform(log);
        FakeSession session = new FakeSession("broken", "deezer.android.app", 3, log);
        session.readFailure = new NullPointerException("programmer error");
        platform.setSessions(session);

        try {
            new DeezerSessionObserver(platform, eligibleState()).connect();
            fail("expected programmer failure");
        } catch (NullPointerException expected) {
            assertEquals("programmer error", expected.getMessage());
        }
    }

    private static MediaPuppetState eligibleState() {
        MediaPuppetState state = new MediaPuppetState();
        state.updateAccess(true, true, true);
        return state;
    }

    private static final class FakePlatform implements DeezerSessionObserver.Platform {
        private final List<String> log;
        private List<DeezerSessionObserver.SessionPort> sessions = Collections.emptyList();
        private Runnable registeredCallback;
        private int registerCount;
        private int unregisterCount;
        private boolean throwOnRegister;

        private FakePlatform(List<String> log) {
            this.log = log;
        }

        private void setSessions(FakeSession... sessions) {
            this.sessions = new ArrayList<>(Arrays.asList(sessions));
        }

        private void fireSessionsChanged() {
            registeredCallback.run();
        }

        @Override
        public void register(Runnable sessionsChanged) {
            log.add("platform.register");
            registerCount++;
            registeredCallback = sessionsChanged;
            if (throwOnRegister) {
                throw new SecurityException("registration denied");
            }
        }

        @Override
        public void unregister(Runnable sessionsChanged) {
            log.add("platform.unregister");
            unregisterCount++;
            if (registeredCallback == sessionsChanged) {
                registeredCallback = null;
            }
        }

        @Override
        public List<DeezerSessionObserver.SessionPort> sessions() {
            log.add("platform.sessions");
            return new ArrayList<>(sessions);
        }
    }

    private static final class FakeSession implements DeezerSessionObserver.SessionPort {
        private final Object token;
        private final String packageName;
        private final String label;
        private final List<String> log;
        private Integer state;
        private DeezerSessionObserver.Callback callback;
        private DeezerSessionObserver.Callback retainedCallback;
        private int registerCount;
        private int unregisterCount;
        private int readCount;
        private boolean throwOnRegister;
        private boolean throwOnRead;
        private boolean throwOnUnregister;
        private RuntimeException readFailure;

        private FakeSession(Object token, String packageName, Integer state, List<String> log) {
            this.token = token;
            this.packageName = packageName;
            this.label = String.valueOf(token);
            this.state = state;
            this.log = log;
        }

        @Override
        public Object token() {
            log.add(label + ".token");
            return token;
        }

        @Override
        public String packageName() {
            log.add(label + ".package");
            return packageName;
        }

        @Override
        public Integer playbackState() {
            log.add(label + ".read");
            readCount++;
            if (readFailure != null) {
                throw readFailure;
            }
            if (throwOnRead) {
                throw new SecurityException("read denied");
            }
            return state;
        }

        @Override
        public void register(DeezerSessionObserver.Callback callback) {
            log.add(label + ".register");
            registerCount++;
            this.callback = callback;
            retainedCallback = callback;
            if (throwOnRegister) {
                throw new SecurityException("callback denied");
            }
        }

        @Override
        public void unregister(DeezerSessionObserver.Callback callback) {
            log.add(label + ".unregister");
            unregisterCount++;
            if (this.callback == callback) {
                this.callback = null;
            }
            if (throwOnUnregister) {
                throw new SecurityException("unregister denied");
            }
        }

        private void fireState(Integer newState) {
            state = newState;
            callback.stateChanged(newState);
        }

        private void fireRetainedState(Integer newState) {
            state = newState;
            retainedCallback.stateChanged(newState);
        }

        private void destroy() {
            callback.destroyed();
        }

        private void destroyRetained() {
            retainedCallback.destroyed();
        }
    }
}
