package com.boop.shieldoverlay;

import org.junit.Test;
import static org.junit.Assert.*;

public final class RoomDeviceControlsTest {
    @Test public void admitsOnlySupportedControllableDeviceDomains() {
        assertTrue(RoomDeviceControls.isActionable(card("light.lamp", "off", false, null)));
        assertTrue(RoomDeviceControls.isActionable(card("fan.chair", "on", false, null)));
        assertTrue(RoomDeviceControls.isActionable(card("switch.speaker", "on", false, null)));
        assertFalse(RoomDeviceControls.isActionable(card("input_boolean.helper", "on", false, null)));
        assertFalse(RoomDeviceControls.isActionable(card("sensor.temperature", "20", false, null)));
        assertFalse(RoomDeviceControls.isActionable(card("button.identify", "unknown", false, null)));
    }
    @Test public void hidesDiagnosticsConfigurationUnavailableAndHiddenControls() {
        assertFalse(RoomDeviceControls.isActionable(card("switch.hex_logging", "on", false, "diagnostic")));
        assertFalse(RoomDeviceControls.isActionable(card("switch.setting", "off", false, "config")));
        assertFalse(RoomDeviceControls.isActionable(card("switch.future", "on", false, "unknown")));
        assertFalse(RoomDeviceControls.isActionable(card("light.lamp", "unavailable", false, null)));
        assertFalse(RoomDeviceControls.isActionable(card("light.hidden", "off", true, null)));
        assertFalse(RoomDeviceControls.isActionable(null));
    }
    private EntityCard card(String id, String state, boolean hidden, String category) {
        return new EntityCard(id, "living_room", "Fixture", state, hidden, category);
    }
}
