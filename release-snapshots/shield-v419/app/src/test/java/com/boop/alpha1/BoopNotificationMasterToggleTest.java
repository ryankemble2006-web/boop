package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BoopNotificationMasterToggleTest {
    @Test
    public void enablingWithoutListenerAccessRequestsPermissionFirst() {
        assertEquals(
                BoopNotificationMasterToggle.Action.REQUEST_LISTENER_ACCESS,
                BoopNotificationMasterToggle.action(true, false));
    }

    @Test
    public void enablingWithListenerAccessCompletesImmediately() {
        assertEquals(
                BoopNotificationMasterToggle.Action.ENABLE_NOW,
                BoopNotificationMasterToggle.action(true, true));
    }

    @Test
    public void disablingNeverRequestsPermission() {
        assertEquals(
                BoopNotificationMasterToggle.Action.DISABLE,
                BoopNotificationMasterToggle.action(false, false));
        assertEquals(
                BoopNotificationMasterToggle.Action.DISABLE,
                BoopNotificationMasterToggle.action(false, true));
    }
}
