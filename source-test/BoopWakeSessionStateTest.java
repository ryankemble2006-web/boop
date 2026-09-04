package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class BoopWakeSessionStateTest {
    private BoopWakeSessionState readyState() {
        BoopWakeSessionState state = new BoopWakeSessionState();
        state.beginForegroundSession();
        state.setMicrophonePermission(true);
        state.setRecognitionSupported(true);
        return state;
    }

    @Test public void armsOnlyWhenForegroundPermissionAndSupportAreReady() {
        BoopWakeSessionState state = new BoopWakeSessionState();
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.beginForegroundSession();
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setMicrophonePermission(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setRecognitionSupported(true);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
    }

    @Test public void wakeCaptureHasThreeSecondDeadlineAndProcessingLifecycle() {
        BoopWakeSessionState state = readyState();
        assertTrue(state.acceptWake(5_000L));
        assertEquals(BoopWakeSessionState.State.WAKE_CAPTURE, state.state());
        assertEquals(8_000L, state.commandDeadlineMs());
        state.markProcessing();
        assertEquals(BoopWakeSessionState.State.PROCESSING, state.state());
        state.finishProcessing();
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
    }

    @Test public void cancelWakeCaptureQuietlyRearms() {
        BoopWakeSessionState state = readyState();
        assertTrue(state.acceptWake(2_000L));
        state.cancelWakeCapture();
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
        assertEquals(-1L, state.commandDeadlineMs());
    }

    @Test public void ttsTapAndSettingsBlockWake() {
        BoopWakeSessionState state = readyState();
        state.setTtsSpeaking(true);
        assertEquals(BoopWakeSessionState.State.SPEAKING, state.state());
        state.setTapListening(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setTtsSpeaking(false);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setTapListening(false);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
        state.setVoiceSettingsOpen(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setVoiceSettingsOpen(false);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
    }

    @Test public void failureLatchesForForegroundSessionAndResetsOnNextSession() {
        BoopWakeSessionState state = readyState();
        state.failWakeSession();
        assertTrue(state.wakeFailed());
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.endForegroundSession();
        state.beginForegroundSession();
        assertFalse(state.wakeFailed());
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setRecognitionSupported(true);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
    }
}
