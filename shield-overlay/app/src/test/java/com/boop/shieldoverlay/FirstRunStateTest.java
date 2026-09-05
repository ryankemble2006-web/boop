package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class FirstRunStateTest {
    @Test
    public void freshInstallStartsAtPairing() {
        FirstRunCoordinator coordinator = new FirstRunCoordinator();
        assertEquals(FirstRunCoordinator.State.PAIRING, coordinator.start(false, false));
    }

    @Test
    public void pairedWithoutRoomShowsRoomPicker() {
        FirstRunCoordinator coordinator = new FirstRunCoordinator();
        assertEquals(FirstRunCoordinator.State.ROOM_PICKER, coordinator.start(true, false));
    }

    @Test
    public void pairedWithRoomShowsHome() {
        FirstRunCoordinator coordinator = new FirstRunCoordinator();
        assertEquals(FirstRunCoordinator.State.HOME, coordinator.start(true, true));
    }

    @Test
    public void successfulPairingPassesThroughFoundIt() {
        FirstRunCoordinator coordinator = new FirstRunCoordinator();
        assertEquals(FirstRunCoordinator.State.PAIRING_SUCCESS, coordinator.onPairingConnected());
        assertEquals(FirstRunCoordinator.State.ROOM_PICKER, coordinator.afterPairingSuccess(false));
    }
}
