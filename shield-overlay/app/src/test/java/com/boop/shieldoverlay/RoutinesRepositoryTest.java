package com.boop.shieldoverlay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class RoutinesRepositoryTest {
    @Test
    public void discoveryUsesDisplayRegistryAndReturnsOnlyUsableRoutinesAlphabetically() throws Exception {
        RecordingCommands commands = new RecordingCommands();
        RoutinesRepository repository = new RoutinesRepository(commands);
        AtomicReference<List<RoutineItem>> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        repository.loadRoutines((items, message) -> {
            result.set(items);
            error.set(message);
        });

        assertEquals("config/entity_registry/list_for_display", commands.command(0).type);

        JSONObject registry = new JSONObject()
                .put("entity_categories", new JSONArray().put("config").put("diagnostic"))
                .put("entities", new JSONArray()
                        .put(new JSONObject().put("ei", "script.bedtime").put("en", "Bedtime"))
                        .put(new JSONObject().put("ei", "scene.movie").put("en", "Movie Night"))
                        .put(new JSONObject().put("ei", "scene.alpha"))
                        .put(new JSONObject().put("ei", "script.hidden").put("en", "Hidden").put("hb", true))
                        .put(new JSONObject().put("ei", "script.config").put("en", "Config").put("ec", 0))
                        .put(new JSONObject().put("ei", "scene.diagnostic").put("en", "Diagnostic").put("ec", 1))
                        .put(new JSONObject().put("ei", "switch.not_a_routine").put("en", "Switch")));
        commands.command(0).callback.onResult(true, registry, null);

        assertEquals("get_states", commands.command(1).type);
        commands.command(1).callback.onResult(
                true,
                new JSONArray()
                        .put(state("script.bedtime", "off", "Bedtime fallback"))
                        .put(state("scene.movie", "2026-09-05T12:00:00+00:00", "Movie fallback"))
                        .put(state("scene.alpha", "unknown", "Alpha Scene")),
                null);

        assertNull(error.get());
        assertEquals(3, result.get().size());
        assertEquals("Alpha Scene", result.get().get(0).displayName());
        assertEquals("Bedtime", result.get().get(1).displayName());
        assertEquals("Movie Night", result.get().get(2).displayName());
        assertEquals(RoutineItem.Type.SCENE, result.get().get(0).type());
        assertEquals(RoutineItem.Type.SCRIPT, result.get().get(1).type());
    }

    @Test
    public void discoveryAcceptsObjectCategoryMapAndExcludesConfigEntity() throws Exception {
        RecordingCommands commands = new RecordingCommands();
        RoutinesRepository repository = new RoutinesRepository(commands);
        AtomicReference<List<RoutineItem>> result = new AtomicReference<>();

        repository.loadRoutines((items, message) -> result.set(items));
        commands.command(0).callback.onResult(
                true,
                new JSONObject()
                        .put("entity_categories", new JSONObject().put("0", "config").put("1", "diagnostic"))
                        .put("entities", new JSONArray()
                                .put(new JSONObject().put("ei", "script.good").put("en", "Good"))
                                .put(new JSONObject().put("ei", "scene.settings").put("en", "Settings").put("ec", 0))),
                null);
        commands.command(1).callback.onResult(
                true,
                new JSONArray()
                        .put(state("script.good", "off", "Good"))
                        .put(state("scene.settings", "unknown", "Settings")),
                null);

        assertEquals(1, result.get().size());
        assertEquals("script.good", result.get().get(0).entityId());
    }

    @Test
    public void sceneTargetsExactEntityAndFinishesWhenServiceAccepted() throws Exception {
        RecordingCommands commands = new RecordingCommands();
        RoutinesRepository repository = new RoutinesRepository(commands);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(false);

        RoutinesRepository.Execution execution = repository.run(
                new RoutineItem("scene.movie_night", "Movie Night", RoutineItem.Type.SCENE),
                (ok, message) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                });

        assertNotNull(execution);
        Command command = commands.command(0);
        assertEquals("call_service", command.type);
        assertEquals("scene", command.body.getString("domain"));
        assertEquals("turn_on", command.body.getString("service"));
        assertEquals("scene.movie_night",
                command.body.getJSONObject("target").getString("entity_id"));
        assertFalse(callbackSeen.get());

        command.callback.onResult(true, null, null);
        assertTrue(callbackSeen.get());
        assertTrue(success.get());
    }

    @Test
    public void cancelledSceneIgnoresLateServiceReply() throws Exception {
        RecordingCommands commands = new RecordingCommands();
        RoutinesRepository repository = new RoutinesRepository(commands);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);

        RoutinesRepository.Execution execution = repository.run(
                new RoutineItem("scene.movie_night", "Movie Night", RoutineItem.Type.SCENE),
                (ok, message) -> callbackSeen.set(true));
        execution.cancel();
        commands.command(0).callback.onResult(true, null, null);

        assertFalse(callbackSeen.get());
    }

    @Test
    public void scriptSubscribesBeforeServiceAndNeedsTargetOnThenOffPlusServiceSuccess() throws Exception {
        RecordingPorts ports = new RecordingPorts();
        RoutinesRepository repository = new RoutinesRepository(ports, ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(false);

        repository.run(
                new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
                (ok, message) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                });

        assertTrue(ports.subscriptionRequested);
        assertEquals(0, ports.commands.size());
        ports.ackSubscription();

        Command command = ports.command(0);
        assertEquals("call_service", command.type);
        assertEquals("script", command.body.getString("domain"));
        assertEquals("turn_on", command.body.getString("service"));
        assertEquals("script.bedtime",
                command.body.getJSONObject("target").getString("entity_id"));

        ports.emit("script.other", "on");
        ports.emit("script.bedtime", "off");
        command.callback.onResult(true, null, null);
        assertFalse(callbackSeen.get());

        ports.emit("script.bedtime", "on");
        assertFalse(callbackSeen.get());
        ports.emit("script.bedtime", "off");

        assertTrue(callbackSeen.get());
        assertTrue(success.get());
        assertTrue(ports.cancelled);
    }

    @Test
    public void scriptLifecycleMayArriveBeforeServiceReplyButCannotFinishEarly() throws Exception {
        RecordingPorts ports = new RecordingPorts();
        RoutinesRepository repository = new RoutinesRepository(ports, ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);

        repository.run(
                new RoutineItem("script.goodnight", "Goodnight", RoutineItem.Type.SCRIPT),
                (ok, message) -> callbackSeen.set(true));
        ports.ackSubscription();
        Command command = ports.command(0);

        ports.emit("script.goodnight", "on");
        ports.emit("script.goodnight", "off");
        assertFalse(callbackSeen.get());

        command.callback.onResult(true, null, null);
        assertTrue(callbackSeen.get());
    }

    @Test
    public void scriptSubscriptionFailureDoesNotCallService() {
        RecordingPorts ports = new RecordingPorts();
        RoutinesRepository repository = new RoutinesRepository(ports, ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(true);

        repository.run(
                new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
                (ok, message) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                });
        ports.failSubscription("No subscription");

        assertTrue(callbackSeen.get());
        assertFalse(success.get());
        assertEquals(0, ports.commands.size());
    }

    @Test
    public void scriptServiceRejectionCancelsSubscriptionAndFails() {
        RecordingPorts ports = new RecordingPorts();
        RoutinesRepository repository = new RoutinesRepository(ports, ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);
        AtomicBoolean success = new AtomicBoolean(true);

        repository.run(
                new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
                (ok, message) -> {
                    callbackSeen.set(true);
                    success.set(ok);
                });
        ports.ackSubscription();
        ports.command(0).callback.onResult(false, null, "Rejected");

        assertTrue(callbackSeen.get());
        assertFalse(success.get());
        assertTrue(ports.cancelled);
    }

    @Test
    public void cancelledScriptReleasesSubscriptionAndIgnoresLateCompletion() {
        RecordingPorts ports = new RecordingPorts();
        RoutinesRepository repository = new RoutinesRepository(ports, ports);
        AtomicBoolean callbackSeen = new AtomicBoolean(false);

        RoutinesRepository.Execution execution = repository.run(
                new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
                (ok, message) -> callbackSeen.set(true));
        ports.ackSubscription();
        Command command = ports.command(0);

        execution.cancel();
        ports.emit("script.bedtime", "on");
        ports.emit("script.bedtime", "off");
        command.callback.onResult(true, null, null);

        assertTrue(ports.cancelled);
        assertFalse(callbackSeen.get());
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

    private static class RecordingCommands implements RoutinesRepository.CommandPort {
        final List<Command> commands = new ArrayList<>();

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            commands.add(new Command(type, body, callback));
        }

        Command command(int index) {
            return commands.get(index);
        }
    }

    private static final class RecordingPorts extends RecordingCommands
            implements RoutinesRepository.StateChangePort {
        boolean subscriptionRequested;
        boolean cancelled;
        Listener listener;
        Callback subscriptionCallback;

        @Override
        public void subscribe(Listener listener, Callback callback) {
            subscriptionRequested = true;
            this.listener = listener;
            this.subscriptionCallback = callback;
        }

        void ackSubscription() {
            assertNotNull(subscriptionCallback);
            subscriptionCallback.onResult(() -> cancelled = true, null);
        }

        void failSubscription(String message) {
            assertNotNull(subscriptionCallback);
            subscriptionCallback.onResult(null, message);
        }

        void emit(String entityId, String state) {
            assertNotNull(listener);
            listener.onStateChanged(entityId, state);
        }
    }
}
