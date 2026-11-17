package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

public class BoopRoomSetupPolicyTest {
    @Test public void unchangedAssignedRoomIsReadyWithoutNetwork() {
        assertFalse(BoopRoomSetupPolicy.needsAssignment(
                "device", "living_room", new BoopRoom("living_room", "Living Room")));
    }

    @Test public void changedRoomRequiresReassignment() {
        assertTrue(BoopRoomSetupPolicy.needsAssignment(
                "device", "living_room", new BoopRoom("bedroom", "Bedroom")));
    }

    @Test public void legacyLivingRoomIdentityMigratesWithoutNetwork() {
        assertFalse(BoopRoomSetupPolicy.needsAssignment(
                "device", null, new BoopRoom("living_room", "Living Room")));
    }

    @Test public void roomChangeDuringRequestCannotPersistSuccess() {
        assertFalse(BoopRoomSetupPolicy.canPersist(
                new BoopRoom("living_room", "Living Room"),
                new BoopRoom("bedroom", "Bedroom")));
    }

    @Test public void pendingAssignmentAlwaysRetries() {
        assertTrue(BoopRoomSetupPolicy.needsAssignment(
                "device", BoopRoomSetupPolicy.PENDING_ROOM_ID,
                new BoopRoom("living_room", "Living Room")));
    }
}
