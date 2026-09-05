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
    public void subscribesBeforeServiceAndConfirmsOnlyRequestedTargetState() throws Exception {
        RecordingPorts ports = new RecordingPorts();
        HomeAssistantRepository repository = new HomeAssistantRepository(ports, ports);
        EntityCard original = card("switch.sync_box", "living_room", "AI Sync Box strip", "on");
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<EntityCard> confirmed = new AtomicReference<>();

        repository.toggleBinary(original, (ok, card, error) -> {
            callbackSeen.set(true);
            success.set(ok);
            confirmed.set(card);
        });

        assertTrue("BOOP must listen before changing the house", ports.subscriptionRequested);
        assertEquals(0, ports.commands.size());

        ports.ackSubscription();
        assertEquals(1, ports.commands.size());
        Command service = ports.commands.get(0);
        assertEquals("call_service", service.type);
        assertEquals("switch", service.body.getString("domain"));
        assertEquals("turn_off", service.body.getString("service"));
        assertEquals("switch.sync_box", service.body.getJSONObject("target").getString("entity_id"));

        service.callback.onResult(true, null, null);
        ports.emit("light.other", "off");
        ports.emit("switch.sync_box", "on");
        assertFalse(callbackSeen.get());

        ports.emit("switch.sync_box", "off");
        assertTrue(callbackSeen.get());
        assertTrue(success.get());
        assertNotNull(confirmed.get());
        assertEquals("off", confirmed.get().state());
        assertTrue("confirmation listener must be released", ports.cancelled);
        assertEquals("event confirmation must not poll get_states", 1, ports.commands.size());
    }

    @Test
    public void expectedEventBeforeServiceReplyWaitsForServiceSuccess() throws Exception {
        RecordingPorts ports = new RecordingPorts();
        HomeAssistantRepository repository = new HomeAssistantRepository(ports, ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(false);

        repository.toggleBinary(
                card("light.sofa", "living_room", "Sofa lamp", "off"),
                (ok, card, error) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                });

        ports.ackSubscription();
        Command service = ports.commands.get(0);
        ports.emit("light.sofa", "on");
        assertFalse("event alone is not proof the service call succeeded", callbackSeen.get());

        service.callback.onResult(true, null, null);
        assertTrue(callbackSeen.get());
        assertTrue(success.get());
        assertTrue(ports.cancelled);
    }

    @Test
    public void failedServiceCancelsListenerAndNeverReportsSuccess() throws Exception {
        RecordingPorts ports = new RecordingPorts();
        HomeAssistantRepository repository = new HomeAssistantRepository(ports, ports);
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

        ports.ackSubscription();
        ports.commands.get(0).callback.onResult(false, null, "Service rejected");

        assertTrue(callbackSeen.get());
        assertFalse(success.get());
        assertNull(result.get());
        assertEquals("Service rejected", error.get());
        assertTrue(ports.cancelled);
    }

    @Test
    public void missingStateListenerNeverChangesDevice() {
        RecordingPorts ports = new RecordingPorts();
        HomeAssistantRepository repository = new HomeAssistantRepository(ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(true);

        repository.toggleBinary(
                card("switch.corner", "living_room", "Corner plug", "on"),
                (ok, card, error) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                });

        assertTrue(callbackSeen.get());
        assertFalse(success.get());
        assertEquals(0, ports.commands.size());
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

    private static final class RecordingPorts
            implements HomeAssistantRepository.CommandPort, HomeAssistantRepository.StateChangePort {
        final List<Command> commands = Collections.synchronizedList(new ArrayList<>());
        boolean subscriptionRequested;
        boolean cancelled;
        HomeAssistantRepository.StateChangePort.Listener listener;
        HomeAssistantRepository.StateChangePort.Callback subscriptionCallback;

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            commands.add(new Command(type, body, callback));
        }

        @Override
        public void subscribe(
                HomeAssistantRepository.StateChangePort.Listener listener,
                HomeAssistantRepository.StateChangePort.Callback callback) {
            subscriptionRequested = true;
            this.listener = listener;
            this.subscriptionCallback = callback;
        }

        void ackSubscription() {
            assertNotNull(subscriptionCallback);
            subscriptionCallback.onResult(() -> cancelled = true, null);
        }

        void emit(String entityId, String state) {
            assertNotNull(listener);
            listener.onStateChanged(entityId, state);
        }
    }
}
