package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopWakeSessionCoordinatorTest {
    private static final class FakeEngine implements BoopWakeSessionCoordinator.Engine {
        int armCalls;
        int suspendCalls;
        int shutdownCalls;
        boolean armResult = true;

        @Override public boolean arm() {
            armCalls++;
            return armResult;
        }

        @Override public void suspendAll() {
            suspendCalls++;
        }

        @Override public void shutdown() {
            shutdownCalls++;
        }
    }

    private static BoopWakeSessionCoordinator ready(FakeEngine engine) {
        BoopWakeSessionCoordinator coordinator = new BoopWakeSessionCoordinator(engine);
        coordinator.beginForegroundSession();
        coordinator.setMicrophonePermission(true);
        coordinator.setRecognitionSupported(true);
        return coordinator;
    }

    @Test public void readyStateArmsOnceAndRepeatedSyncIsHarmless() {
        FakeEngine engine = new FakeEngine();
        BoopWakeSessionCoordinator coordinator = ready(engine);
        assertEquals(BoopWakeSessionState.State.ARMED, coordinator.state());
        assertEquals(1, engine.armCalls);
        coordinator.setRecognitionSupported(true);
        coordinator.setMicrophonePermission(true);
        assertEquals(1, engine.armCalls);
    }

    @Test public void foregroundTapSettingsAndTtsSuspendWake() {
        FakeEngine engine = new FakeEngine();
        BoopWakeSessionCoordinator coordinator = ready(engine);

        coordinator.endForegroundSession();
        assertEquals(1, engine.suspendCalls);

        coordinator.beginForegroundSession();
        coordinator.setMicrophonePermission(true);
        coordinator.setRecognitionSupported(true);
        coordinator.onTapStarted();
        assertEquals(2, engine.suspendCalls);
        coordinator.onTapFinished();

        coordinator.setVoiceSettingsOpen(true);
        assertEquals(3, engine.suspendCalls);
        coordinator.setVoiceSettingsOpen(false);

        coordinator.onTtsStarting();
        assertEquals(4, engine.suspendCalls);
    }

    @Test public void ttsStopWhileTapActiveStaysDisarmed() {
        FakeEngine engine = new FakeEngine();
        BoopWakeSessionCoordinator coordinator = ready(engine);
        coordinator.onTtsStarting();
        coordinator.onTapStarted();
        coordinator.onTtsFinished();
        assertEquals(BoopWakeSessionState.State.DISARMED, coordinator.state());
        coordinator.onTapFinished();
        assertEquals(BoopWakeSessionState.State.ARMED, coordinator.state());
    }

    @Test public void wakeCaptureKeepsEngineAliveButProcessingSuspends() {
        FakeEngine engine = new FakeEngine();
        BoopWakeSessionCoordinator coordinator = ready(engine);
        int before = engine.suspendCalls;
        assertTrue(coordinator.onWakeDetected(1_000L));
        assertEquals(BoopWakeSessionState.State.WAKE_CAPTURE, coordinator.state());
        assertEquals(before, engine.suspendCalls);
        coordinator.markWakeProcessing();
        assertEquals(BoopWakeSessionState.State.PROCESSING, coordinator.state());
        assertEquals(before + 1, engine.suspendCalls);
    }

    @Test public void failedArmLatchesUntilFreshForegroundSupportResult() {
        FakeEngine engine = new FakeEngine();
        engine.armResult = false;
        BoopWakeSessionCoordinator coordinator = new BoopWakeSessionCoordinator(engine);
        coordinator.beginForegroundSession();
        coordinator.setMicrophonePermission(true);
        coordinator.setRecognitionSupported(true);
        assertTrue(coordinator.wakeFailed());
        assertEquals(1, engine.armCalls);
        coordinator.setRecognitionSupported(true);
        assertEquals(1, engine.armCalls);

        coordinator.endForegroundSession();
        engine.armResult = true;
        coordinator.beginForegroundSession();
        coordinator.setMicrophonePermission(true);
        assertFalse(coordinator.wakeFailed());
        assertEquals(BoopWakeSessionState.State.DISARMED, coordinator.state());
        assertEquals(1, engine.armCalls);
        coordinator.setRecognitionSupported(true);
        assertEquals(2, engine.armCalls);
        assertEquals(BoopWakeSessionState.State.ARMED, coordinator.state());
    }

    @Test public void shutdownOwnsEngineTeardown() {
        FakeEngine engine = new FakeEngine();
        BoopWakeSessionCoordinator coordinator = ready(engine);
        coordinator.shutdown();
        assertEquals(1, engine.shutdownCalls);
    }
}
