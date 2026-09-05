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

public final class HomeAssistantDashboardTest {
    @Test
    public void selectedAreaUsesHaTargetMembershipIncludingDeviceInheritedEntities() throws Exception {
        RecordingPort port = new RecordingPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(port);
        AreaInfo room = new AreaInfo("living_room", "Living Room");
        AtomicReference<DashboardSnapshot> snapshot = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        repository.loadDashboard(room, (value, message) -> {
            snapshot.set(value);
            error.set(message);
        });

        assertEquals(1, port.commands.size());
        Command target = port.commands.get(0);
        assertEquals("extract_from_target", target.type);
        assertEquals("living_room", target.body.getJSONObject("target").getString("area_id"));
        assertTrue(!target.body.getBoolean("expand_group"));
        assertTrue(!target.body.getBoolean("primary_entities_only"));

        target.callback.onResult(
                true,
                new JSONObject().put(
                        "referenced_entities",
                        new JSONArray()
                                .put("light.sofa")
                                .put("switch.tv_plug")
                                .put("sensor.temperature")
                                .put("light.hidden")
                                .put("switch.config")
                                .put("fan.diagnostic")),
                null);

        assertEquals(2, port.commands.size());
        assertEquals("config/entity_registry/list_for_display", port.commands.get(1).type);

        JSONObject registry = new JSONObject()
                .put("entity_categories", new JSONArray().put("config").put("diagnostic"))
                .put("entities", new JSONArray()
                        .put(registry("light.sofa", "living_room", "Sofa lamp", false, null))
                        .put(registry("switch.tv_plug", null, "TV plug", false, null))
                        .put(registry("sensor.temperature", "living_room", "Temperature", false, null))
                        .put(registry("light.hidden", "living_room", "Hidden light", true, null))
                        .put(registry("switch.config", "living_room", "Config switch", false, 0))
                        .put(registry("fan.diagnostic", "living_room", "Diagnostic fan", false, 1))
                        .put(registry("light.bedroom", "bedroom", "Bedroom lamp", false, null)));
        port.commands.get(1).callback.onResult(true, registry, null);

        assertEquals(3, port.commands.size());
        assertEquals("get_states", port.commands.get(2).type);

        JSONArray states = new JSONArray()
                .put(state("light.sofa", "off", "Sofa lamp"))
                .put(state("switch.tv_plug", "on", "TV plug"))
                .put(state("sensor.temperature", "21.4", "Temperature"))
                .put(state("light.hidden", "off", "Hidden light"))
                .put(state("switch.config", "off", "Config switch"))
                .put(state("fan.diagnostic", "off", "Diagnostic fan"))
                .put(state("light.bedroom", "off", "Bedroom lamp"));
        port.commands.get(2).callback.onResult(true, states, null);

        assertNull(error.get());
        assertNotNull(snapshot.get());
        assertEquals("living_room", snapshot.get().room().id());
        assertEquals(2, snapshot.get().cards().size());
        assertEquals("light.sofa", snapshot.get().cards().get(0).entityId());
        assertEquals("switch.tv_plug", snapshot.get().cards().get(1).entityId());
        assertNull(snapshot.get().cards().get(1).areaId());
    }

    @Test
    public void dashboardFailsPlainlyWhenHaCannotResolveRoomMembership() throws Exception {
        RecordingPort port = new RecordingPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(port);
        AtomicReference<DashboardSnapshot> snapshot = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        repository.loadDashboard(
                new AreaInfo("living_room", "Living Room"),
                (value, message) -> {
                    snapshot.set(value);
                    error.set(message);
                });

        port.commands.get(0).callback.onResult(false, null, "Target lookup failed");

        assertNull(snapshot.get());
        assertEquals("Target lookup failed", error.get());
        assertEquals(1, port.commands.size());
    }

    private static JSONObject registry(
            String entityId,
            String areaId,
            String name,
            boolean hidden,
            Integer categoryIndex) throws Exception {
        JSONObject object = new JSONObject()
                .put("ei", entityId)
                .put("en", name)
                .put("hb", hidden);
        if (areaId != null) {
            object.put("ai", areaId);
        }
        if (categoryIndex != null) {
            object.put("ec", categoryIndex);
        }
        return object;
    }

    private static JSONObject state(String entityId, String value, String friendlyName) throws Exception {
        return new JSONObject()
                .put("entity_id", entityId)
                .put("state", value)
                .put("attributes", new JSONObject().put("friendly_name", friendlyName));
    }

    private static final class Command {
        final String type;
        final JSONObject body;
        final HomeAssistantWebSocket.Callback callback;

        Command(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            this.type = type;
            this.body = body;
            this.callback = callback;
        }
    }

    private static final class RecordingPort implements HomeAssistantRepository.CommandPort {
        final List<Command> commands = new ArrayList<>();

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            commands.add(new Command(type, body, callback));
        }
    }
}
