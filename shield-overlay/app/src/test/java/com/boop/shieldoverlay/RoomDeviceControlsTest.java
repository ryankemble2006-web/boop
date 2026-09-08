package com.boop.shieldoverlay;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
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

    @Test public void collapsesOnePhysicalDeviceToItsPrimaryControl() {
        List<EntityCard> result = RoomDeviceControls.collapseToDevices(Arrays.asList(
                deviceCard("switch.lounge_fan_power_switch", "Power Switch", "on", "dev-fan", "Living Room Fan"),
                deviceCard("switch.lounge_fan_oscillation_toggle", "Oscillation Toggle", "off", "dev-fan", "Living Room Fan"),
                deviceCard("fan.lounge_fan", "Fan", "on", "dev-fan", "Living Room Fan")));

        assertEquals(1, result.size());
        assertEquals("fan.lounge_fan", result.get(0).entityId());
        assertEquals("Living Room Fan", result.get(0).displayName());
    }

    @Test public void keepsOneStandaloneSwitchWhenItReallyIsTheDevice() {
        List<EntityCard> result = RoomDeviceControls.collapseToDevices(Arrays.asList(
                deviceCard("switch.floor_lamp", "Power", "off", "dev-plug", "Floor Lamp")));

        assertEquals(1, result.size());
        assertEquals("switch.floor_lamp", result.get(0).entityId());
        assertEquals("Floor Lamp", result.get(0).displayName());
    }

    @Test public void rejectsLooseEntitiesBecauseHomeShowsDevicesNotEntityPlumbing() {
        List<EntityCard> result = RoomDeviceControls.collapseToDevices(Arrays.asList(
                card("switch.orphan_power", "on", false, null),
                card("light.logical_group", "off", false, null)));
        assertTrue(result.isEmpty());
    }

    private EntityCard card(String id, String state, boolean hidden, String category) {
        return new EntityCard(id, "living_room", "Fixture", state, hidden, category);
    }

    private EntityCard deviceCard(String id, String entityName, String state, String deviceId, String deviceName) {
        return new EntityCard(id, "living_room", entityName, state, false, null, deviceId, deviceName);
    }
}
