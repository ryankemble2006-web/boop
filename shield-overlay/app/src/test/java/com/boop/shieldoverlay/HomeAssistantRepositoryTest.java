package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class HomeAssistantRepositoryTest {
    @Test
    public void areaRegistryBecomesCleanAlphabeticalRoomCards() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AtomicReference<List<AreaInfo>> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        repository.loadAreas((areas, message) -> { result.set(areas); error.set(message); });
        assertEquals("config/area_registry/list", commands.type(0));
        JSONArray payload = new JSONArray()
                .put(new JSONObject().put("area_id", "bedroom").put("name", "Bedroom"))
                .put(new JSONObject().put("area_id", "living_room").put("name", "Living Room"))
                .put(new JSONObject().put("area_id", "kitchen").put("name", "Kitchen"))
                .put(new JSONObject().put("area_id", "bad").put("name", "  "));
        commands.reply(0, true, payload, null);
        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(3, result.get().size());
        assertEquals("Bedroom", result.get().get(0).name());
        assertEquals("Kitchen", result.get().get(1).name());
        assertEquals("Living Room", result.get().get(2).name());
        assertEquals("living_room", result.get().get(2).id());
    }

    @Test
    public void failedOrMalformedAreaRequestIsPlainFailureNotEmptySuccess() {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AtomicReference<List<AreaInfo>> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        repository.loadAreas((areas, message) -> { result.set(areas); error.set(message); });
        commands.reply(0, false, null, "not allowed");
        assertNull(result.get());
        assertNotNull(error.get());
    }

    @Test
    public void dashboardUsesDeviceInheritedAreaAndReturnsOnePrimaryCardPerDevice() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AtomicReference<DashboardSnapshot> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        AreaInfo lounge = new AreaInfo("living_room", "Living Room");
        repository.loadDashboard(lounge, (snapshot, message) -> { result.set(snapshot); error.set(message); });

        assertEquals("extract_from_target", commands.type(0));
        commands.reply(0, true, new JSONObject().put("referenced_entities", new JSONArray()
                .put("fan.lounge_fan")
                .put("switch.lounge_fan_power_switch")
                .put("switch.lounge_fan_oscillation_toggle")
                .put("switch.bedroom_plug")
                .put("switch.loose_helper")), null);

        assertEquals("config/device_registry/list", commands.type(1));
        commands.reply(1, true, new JSONArray()
                .put(new JSONObject().put("id", "dev-fan").put("area_id", "living_room").put("name", "Living Room Fan"))
                .put(new JSONObject().put("id", "dev-bedroom").put("area_id", "bedroom").put("name", "Bedroom Plug")), null);

        assertEquals("config/entity_registry/list_for_display", commands.type(2));
        JSONObject registry = new JSONObject()
                .put("entity_categories", new JSONObject().put("0", "config").put("1", "diagnostic"))
                .put("entities", new JSONArray()
                        .put(new JSONObject().put("ei", "fan.lounge_fan").put("di", "dev-fan").put("en", "Fan"))
                        .put(new JSONObject().put("ei", "switch.lounge_fan_power_switch").put("di", "dev-fan").put("en", "Power Switch"))
                        .put(new JSONObject().put("ei", "switch.lounge_fan_oscillation_toggle").put("di", "dev-fan").put("en", "Oscillation Toggle"))
                        .put(new JSONObject().put("ei", "switch.bedroom_plug").put("di", "dev-bedroom").put("en", "Power"))
                        .put(new JSONObject().put("ei", "switch.loose_helper").put("en", "Loose helper")));
        commands.reply(2, true, registry, null);

        assertEquals("get_states", commands.type(3));
        JSONArray states = new JSONArray()
                .put(state("fan.lounge_fan", "on", "Fan"))
                .put(state("switch.lounge_fan_power_switch", "on", "Power Switch"))
                .put(state("switch.lounge_fan_oscillation_toggle", "off", "Oscillation Toggle"))
                .put(state("switch.bedroom_plug", "on", "Power"))
                .put(state("switch.loose_helper", "on", "Loose helper"));
        commands.reply(3, true, states, null);

        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(1, result.get().cards().size());
        assertEquals("fan.lounge_fan", result.get().cards().get(0).entityId());
        assertEquals("Living Room Fan", result.get().cards().get(0).displayName());
        assertEquals("living_room", result.get().cards().get(0).areaId());
    }

    @Test
    public void dashboardFailsClosedWhenPhysicalDeviceAreaCannotBeConfirmed() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AtomicReference<DashboardSnapshot> result = new AtomicReference<>();
        AreaInfo lounge = new AreaInfo("living_room", "Living Room");
        repository.loadDashboard(lounge, (snapshot, message) -> result.set(snapshot));
        commands.reply(0, true, new JSONObject().put("referenced_entities", new JSONArray().put("switch.mystery")), null);
        commands.reply(1, true, new JSONArray(), null);
        commands.reply(2, true, new JSONObject()
                .put("entity_categories", new JSONObject())
                .put("entities", new JSONArray().put(new JSONObject().put("ei", "switch.mystery").put("en", "Mystery"))), null);
        commands.reply(3, true, new JSONArray().put(state("switch.mystery", "on", "Mystery")), null);
        assertNotNull(result.get());
        assertTrue(result.get().cards().isEmpty());
    }

    private static JSONObject state(String entityId, String value, String name) throws Exception {
        return new JSONObject()
                .put("entity_id", entityId)
                .put("state", value)
                .put("attributes", new JSONObject().put("friendly_name", name));
    }

    private static final class FakeCommandPort implements HomeAssistantRepository.CommandPort {
        private final List<String> types = new ArrayList<>();
        private final List<HomeAssistantWebSocket.Callback> callbacks = new ArrayList<>();

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            types.add(type);
            callbacks.add(callback);
        }

        String type(int index) { return types.get(index); }
        void reply(int index, boolean success, Object result, String error) {
            callbacks.get(index).onResult(success, result, error);
        }
    }
}
