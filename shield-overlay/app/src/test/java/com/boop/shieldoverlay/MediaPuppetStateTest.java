package com.boop.shieldoverlay;

import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.EYES;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING;
import static com.boop.shieldoverlay.DeezerPuppetPolicy.Mode.HEADPHONES_REST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class MediaPuppetStateTest {
    @Test
    public void statusGivesOffPrecedenceAndReportsRealAccessLifecycle() {
        MediaPuppetState state = new MediaPuppetState();

        assertEquals("Off", state.snapshot().status());
        assertEquals(EYES, state.snapshot().mode);

        state.updateAccess(false, true, true);
        assertEquals("Off", state.snapshot().status());

        state.updateAccess(true, false, true);
        assertEquals("Access needed", state.snapshot().status());

        state.updateAccess(true, true, false);
        assertEquals("Connecting", state.snapshot().status());

        state.updateAccess(true, true, true);
        assertEquals("On", state.snapshot().status());
    }

    @Test
    public void optingOutClearsSessionAndOptingBackInCannotReviveIt() {
        MediaPuppetState state = playingState();

        state.updateAccess(false, true, true);
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);

        state.updateAccess(true, true, true);
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);
    }

    @Test
    public void losingGrantClearsSessionAndRestoringGrantRequiresFreshSession() {
        MediaPuppetState state = playingState();

        state.updateAccess(true, false, true);
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);

        state.updateAccess(true, true, true);
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);

        state.updateSession(8L, 2);
        assertEquals(8L, state.snapshot().sessionId);
        assertEquals(HEADPHONES_REST, state.snapshot().mode);
    }

    @Test
    public void lateSessionUpdateWhileIneligibleCannotBeCachedForRevival() {
        MediaPuppetState state = playingState();
        state.updateAccess(false, true, true);

        state.updateSession(99L, 3);

        assertEquals(0L, state.snapshot().sessionId);
        state.updateAccess(true, true, true);
        assertEquals(0L, state.snapshot().sessionId);
        assertEquals(EYES, state.snapshot().mode);
    }

    @Test
    public void identicalExternallyVisibleSnapshotsDoNotNotifySubscribers() {
        MediaPuppetState state = new MediaPuppetState();
        List<MediaPuppetState.Snapshot> delivered = new ArrayList<>();
        state.subscribe(delivered::add);

        state.updateAccess(false, false, false);
        state.updateAccess(true, true, true);
        state.updateSession(7L, 2);
        state.updateSession(7L, 4);
        state.updateSession(7L, 3);

        assertEquals(4, delivered.size());
        assertEquals(EYES, delivered.get(0).mode);
        assertEquals(EYES, delivered.get(1).mode);
        assertEquals(HEADPHONES_REST, delivered.get(2).mode);
        assertEquals(HEADPHONES_PLAYING, delivered.get(3).mode);
    }

    @Test
    public void unsubscribeDuringDeliveryIsSafeAndUnsubscribeIsIdempotent() {
        MediaPuppetState state = new MediaPuppetState();
        int[] firstCalls = {0};
        int[] secondCalls = {0};
        boolean[] armed = {false};
        Runnable[] firstUnsubscribe = new Runnable[1];

        firstUnsubscribe[0] = state.subscribe(snapshot -> {
            firstCalls[0]++;
            if (armed[0]) {
                firstUnsubscribe[0].run();
            }
        });
        state.subscribe(snapshot -> secondCalls[0]++);
        armed[0] = true;

        state.updateAccess(true, false, false);
        firstUnsubscribe[0].run();
        state.updateAccess(true, true, false);

        assertEquals(2, firstCalls[0]);
        assertEquals(3, secondCalls[0]);
    }

    @Test
    public void snapshotsAreValuesAndDoNotMutateAfterDelivery() {
        MediaPuppetState state = new MediaPuppetState();
        MediaPuppetState.Snapshot before = state.snapshot();

        state.updateAccess(true, true, true);
        state.updateSession(42L, 3);

        assertFalse(before.enabled);
        assertFalse(before.granted);
        assertFalse(before.connected);
        assertEquals(0L, before.sessionId);
        assertEquals(EYES, before.mode);
        assertTrue(state.snapshot().enabled);
        assertEquals(42L, state.snapshot().sessionId);
    }

    private static MediaPuppetState playingState() {
        MediaPuppetState state = new MediaPuppetState();
        state.updateAccess(true, true, true);
        state.updateSession(7L, 3);
        assertEquals(HEADPHONES_PLAYING, state.snapshot().mode);
        return state;
    }
}
