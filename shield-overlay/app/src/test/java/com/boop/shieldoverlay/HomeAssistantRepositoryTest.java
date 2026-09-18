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
                .put(stateWithFeatures("fan.lounge_fan", "on", "Fan", 48L))
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
    public void dashboardFallsBackToPowerSwitchWhenFanLacksPowerFeatures() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AtomicReference<DashboardSnapshot> result = new AtomicReference<>();
        AreaInfo lounge = new AreaInfo("living_room", "Living Room");
        repository.loadDashboard(lounge, (snapshot, message) -> result.set(snapshot));

        commands.reply(0, true, new JSONObject().put("referenced_entities", new JSONArray()
                .put("fan.govee_fan")
                .put("switch.govee_fan_power_switch")
                .put("switch.govee_fan_oscillation")), null);
        commands.reply(1, true, new JSONArray()
                .put(new JSONObject().put("id", "dev-govee").put("area_id", "living_room").put("name", "Govee Fan")), null);
        commands.reply(2, true, new JSONObject()
                .put("entity_categories", new JSONObject())
                .put("entities", new JSONArray()
                        .put(new JSONObject().put("ei", "fan.govee_fan").put("di", "dev-govee").put("en", "Fan"))
                        .put(new JSONObject().put("ei", "switch.govee_fan_power_switch").put("di", "dev-govee").put("en", "Power Switch"))
                        .put(new JSONObject().put("ei", "switch.govee_fan_oscillation").put("di", "dev-govee").put("en", "Oscillation"))), null);
        commands.reply(3, true, new JSONArray()
                .put(stateWithFeatures("fan.govee_fan", "on", "Fan", 0L))
                .put(state("switch.govee_fan_power_switch", "on", "Power Switch"))
                .put(state("switch.govee_fan_oscillation", "off", "Oscillation")), null);

        assertNotNull(result.get());
        assertEquals(1, result.get().cards().size());
        assertEquals("switch.govee_fan_power_switch", result.get().cards().get(0).entityId());
        assertEquals("Govee Fan", result.get().cards().get(0).displayName());
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

    @Test
    public void binaryToggleSendsCallServiceImmediatelyAndLeavesStateToLiveStream() {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        EntityCard fan = new EntityCard(
                "fan.lounge_fan", "living_room", "Fan", "off",
                false, null, "dev-fan", "Living Room Fan");
        AtomicReference<Boolean> success = new AtomicReference<>();
        AtomicReference<EntityCard> accepted = new AtomicReference<>();
        AtomicReference<EntityCard> returned = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        repository.toggleBinary(fan, new HomeAssistantRepository.BinaryActionCallback() {
            @Override public void onAccepted(EntityCard requestedState) { accepted.set(requestedState); }
            @Override public void onResult(boolean ok, EntityCard card, String message) {
                success.set(ok); returned.set(card); error.set(message);
            }
        });

        assertEquals(1, commands.size());
        assertEquals("call_service", commands.type(0));
        assertNull(success.get());
        commands.reply(0, true, new JSONObject(), null);

        assertEquals(Boolean.TRUE, success.get());
        assertNotNull(accepted.get());
        assertEquals("on", accepted.get().state());
        assertNull(returned.get());
        assertNull(error.get());
    }

    @Test
    public void semanticFanUsesRoomScopedConversationRouteFirst() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AreaInfo lounge = new AreaInfo("living_room", "Living Room");
        EntityCard fanPowerSwitch = new EntityCard(
                "switch.govee_fan_power_switch", "living_room", "Govee Fan", "on",
                false, null, "dev-govee", "Govee Fan");
        AtomicReference<Boolean> success = new AtomicReference<>();

        repository.toggleBinary(lounge, fanPowerSwitch,
                (ok, card, message) -> success.set(ok));

        assertEquals("conversation/process", commands.type(0));
        assertEquals("turn off Govee Fan in Living Room", commands.body(0).getString("text"));
        commands.reply(0, true, new JSONObject()
                .put("response", new JSONObject()
                        .put("response_type", "action_done")
                        .put("data", new JSONObject().put("failed", new JSONArray()))), null);
        assertEquals(Boolean.TRUE, success.get());
        assertEquals(1, commands.size());
    }

    @Test
    public void semanticFanFallsBackToDirectServiceWhenConversationCannotAct() {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        AreaInfo lounge = new AreaInfo("living_room", "Living Room");
        EntityCard fanPowerSwitch = new EntityCard(
                "switch.govee_fan_power_switch", "living_room", "Fan", "off",
                false, null, "dev-govee", "Fan");
        AtomicReference<Boolean> success = new AtomicReference<>();

        repository.toggleBinary(lounge, fanPowerSwitch,
                (ok, card, message) -> success.set(ok));
        assertEquals("conversation/process", commands.type(0));
        commands.reply(0, true, new JSONObject()
                .put("response", new JSONObject().put("response_type", "error")), null);
        assertEquals("call_service", commands.type(1));
        commands.reply(1, true, new JSONObject(), null);
        assertEquals(Boolean.TRUE, success.get());
    }

    @Test
    public void binaryToggleReportsImmediateServiceFailure() {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);
        EntityCard light = new EntityCard(
                "light.lamp", "living_room", "Lamp", "on",
                false, null, "dev-light", "Lamp");
        AtomicReference<Boolean> success = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        repository.toggleBinary(light, (ok, card, message) -> {
            success.set(ok); error.set(message);
        });
        assertEquals("call_service", commands.type(0));
        commands.reply(0, false, null, "rejected");

        assertEquals(Boolean.FALSE, success.get());
        assertEquals("rejected", error.get());
    }

    private static JSONObject state(String entityId, String value, String name) throws Exception {
        return new JSONObject()
                .put("entity_id", entityId)
                .put("state", value)
                .put("attributes", new JSONObject().put("friendly_name", name));
    }

    private static JSONObject stateWithFeatures(
            String entityId, String value, String name, long supportedFeatures) throws Exception {
        return new JSONObject()
                .put("entity_id", entityId)
                .put("state", value)
                .put("attributes", new JSONObject()
                        .put("friendly_name", name)
                        .put("supported_features", supportedFeatures));
    }

    private static final class FakeCommandPort implements HomeAssistantRepository.CommandPort {
        private final List<String> types = new ArrayList<>();
        private final List<HomeAssistantWebSocket.Callback> callbacks = new ArrayList<>();
        private final List<JSONObject> bodies = new ArrayList<>();

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            types.add(type);
            bodies.add(body);
            callbacks.add(callback);
        }

        int size() { return types.size(); }
        String type(int index) { return types.get(index); }
        JSONObject body(int index) { return bodies.get(index); }
        void reply(int index, boolean success, Object result, String error) {
            callbacks.get(index).onResult(success, result, error);
        }
    }
}
