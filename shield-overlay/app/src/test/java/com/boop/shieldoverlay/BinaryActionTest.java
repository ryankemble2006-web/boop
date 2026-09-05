package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class BinaryActionTest {
    @Test
    public void repositoryExposesStateChangeConfirmationPort() throws Exception {
        Class<?> stateChangePort = null;
        for (Class<?> nested : HomeAssistantRepository.class.getDeclaredClasses()) {
            if ("StateChangePort".equals(nested.getSimpleName())) {
                stateChangePort = nested;
                break;
            }
        }

        assertNotNull("binary confirmation needs a state-change port", stateChangePort);
        assertNotNull(HomeAssistantRepository.class.getConstructor(
                HomeAssistantRepository.CommandPort.class,
                stateChangePort));
    }

    @Test
    public void offEntityCallsTurnOnForExactTargetThenRefreshesState() throws Exception {
        RecordingPort port = new RecordingPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(port);
        EntityCard original = card("light.sofa", "living_room", "Sofa lamp", "off");
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<EntityCard> refreshed = new AtomicReference<>();

        repository.toggleBinary(original, (ok, card, error) -> {
            success.set(ok);
            refreshed.set(card);
        });

        assertEquals(1, port.commands.size());
        Command service = port.commands.get(0);
        assertEquals("call_service", service.type);
        assertEquals("light", service.body.getString("domain"));
        assertEquals("turn_on", service.body.getString("service"));
        assertEquals("light.sofa", service.body.getJSONObject("target").getString("entity_id"));
        assertFalse(success.get());
        assertNull(refreshed.get());

        service.callback.onResult(true, null, null);

        assertEquals(2, port.commands.size());
        assertEquals("get_states", port.commands.get(1).type);
        assertFalse(success.get());

        JSONArray states = new JSONArray()
                .put(new JSONObject()
                        .put("entity_id", "light.sofa")
                        .put("state", "on")
                        .put("attributes", new JSONObject().put("friendly_name", "Sofa lamp")));
        port.commands.get(1).callback.onResult(true, states, null);

        assertTrue(success.get());
        assertEquals("light.sofa", refreshed.get().entityId());
        assertEquals("on", refreshed.get().state());
    }

    @Test
    public void staleFirstReadWaitsForRequestedStateBeforeReportingSuccess() throws Exception {
        RecordingPort port = new RecordingPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(port);
        EntityCard original = card("switch.sync_box", "living_room", "AI Sync Box strip", "on");
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<EntityCard> refreshed = new AtomicReference<>();

        repository.toggleBinary(original, (ok, card, error) -> {
            callbackSeen.set(true);
            success.set(ok);
            refreshed.set(card);
        });

        port.commands.get(0).callback.onResult(true, null, null);
        assertEquals("get_states", port.commands.get(1).type);

        JSONArray staleStates = new JSONArray()
                .put(new JSONObject()
                        .put("entity_id", "switch.sync_box")
                        .put("state", "on")
                        .put("attributes", new JSONObject().put("friendly_name", "AI Sync Box strip")));
        port.commands.get(1).callback.onResult(true, staleStates, null);

        assertFalse("old state must not be accepted as confirmation", callbackSeen.get());
        waitForCommandCount(port, 3, 2000L);
        assertEquals("get_states", port.commands.get(2).type);

        JSONArray settledStates = new JSONArray()
                .put(new JSONObject()
                        .put("entity_id", "switch.sync_box")
                        .put("state", "off")
                        .put("attributes", new JSONObject().put("friendly_name", "AI Sync Box strip")));
        port.commands.get(2).callback.onResult(true, settledStates, null);

        assertTrue(callbackSeen.get());
        assertTrue(success.get());
        assertEquals("off", refreshed.get().state());
    }

    @Test
    public void onEntityCallsTurnOffUsingItsActualDomain() throws Exception {
        RecordingPort port = new RecordingPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(port);

        repository.toggleBinary(
                card("switch.corner", "living_room", "Corner plug", "on"),
                (ok, card, error) -> { });

        Command service = port.commands.get(0);
        assertEquals("switch", service.body.getString("domain"));
        assertEquals("turn_off", service.body.getString("service"));
        assertEquals("switch.corner", service.body.getJSONObject("target").getString("entity_id"));
    }

    @Test
    public void failedServiceIsNeverReportedAsSuccessAndDoesNotRefresh() {
        RecordingPort port = new RecordingPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(port);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<EntityCard> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        repository.toggleBinary(
                card("fan.room", "living_room", "Room fan", "off"),
                (ok, card, message) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                    result.set(card);
                    error.set(message);
                });

        port.commands.get(0).callback.onResult(false, null, "Service rejected");

        assertTrue(callbackSeen.get());
        assertFalse(success.get());
        assertNull(result.get());
        assertEquals("Service rejected", error.get());
        assertEquals(1, port.commands.size());
    }

    private static void waitForCommandCount(RecordingPort port, int expected, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (port.commands.size() < expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
        assertEquals(expected, port.commands.size());
    }

    private static EntityCard card(String id, String area, String name, String state) {
        return new EntityCard(id, area, name, state, false, null);
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
        final List<Command> commands = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            commands.add(new Command(type, body, callback));
        }
    }
}
