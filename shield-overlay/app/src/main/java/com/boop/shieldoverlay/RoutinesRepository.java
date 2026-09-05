package com.boop.shieldoverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RoutinesRepository {
    public interface CommandPort {
        void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback);
    }

    public interface StateChangePort {
        interface Listener {
            void onStateChanged(String entityId, String state);
        }

        interface Subscription {
            void cancel();
        }

        interface Callback {
            void onResult(Subscription subscription, String error);
        }

        void subscribe(Listener listener, Callback callback);
    }

    public interface LoadCallback {
        void onResult(List<RoutineItem> routines, String error);
    }

    public interface RunCallback {
        void onResult(boolean success, String error);
    }

    public interface Execution {
        void cancel();
    }

    private final CommandPort commandPort;
    private final StateChangePort stateChangePort;

    public RoutinesRepository(CommandPort commandPort) {
        this(commandPort, null);
    }

    public RoutinesRepository(CommandPort commandPort, StateChangePort stateChangePort) {
        if (commandPort == null) {
            throw new IllegalArgumentException("Home Assistant command port is required");
        }
        this.commandPort = commandPort;
        this.stateChangePort = stateChangePort;
    }

    public void loadRoutines(LoadCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("routines callback is required");
        }

        try {
            commandPort.send(
                    "config/entity_registry/list_for_display",
                    new JSONObject(),
                    (success, result, error) -> {
                        if (!success || !(result instanceof JSONObject)) {
                            callback.onResult(null, "I couldn't load routines from Home Assistant.");
                            return;
                        }

                        JSONObject registry = (JSONObject) result;
                        JSONArray entities = registry.optJSONArray("entities");
                        if (entities == null) {
                            callback.onResult(null, "I couldn't load routines from Home Assistant.");
                            return;
                        }

                        Map<String, Candidate> candidates = collectCandidates(
                                entities,
                                registry.opt("entity_categories"));
                        if (candidates.isEmpty()) {
                            callback.onResult(Collections.emptyList(), null);
                            return;
                        }
                        loadStates(candidates, callback);
                    });
        } catch (RuntimeException unavailable) {
            callback.onResult(null, "I couldn't load routines from Home Assistant.");
        }
    }

    public Execution run(RoutineItem routine, RunCallback callback) {
        if (routine == null) {
            throw new IllegalArgumentException("routine is required");
        }
        if (callback == null) {
            throw new IllegalArgumentException("run callback is required");
        }

        if (routine.type() == RoutineItem.Type.SCRIPT) {
            if (stateChangePort == null) {
                callback.onResult(false, "I couldn't watch that script finish.");
                return () -> { };
            }
            ScriptExecution execution = new ScriptExecution(routine, callback);
            execution.start();
            return execution;
        }

        SceneExecution execution = new SceneExecution(callback);
        execution.start(routine);
        return execution;
    }

    private Map<String, Candidate> collectCandidates(JSONArray entities, Object categories) {
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        for (int index = 0; index < entities.length(); index++) {
            Object raw = entities.opt(index);
            if (!(raw instanceof JSONObject)) {
                continue;
            }

            JSONObject object = (JSONObject) raw;
            String entityId = clean(object.optString("ei", null));
            RoutineItem.Type type = typeForEntityId(entityId);
            if (entityId == null || type == null || object.optBoolean("hb", false)) {
                continue;
            }

            String category = entityCategory(object.opt("ec"), categories);
            if ("config".equalsIgnoreCase(category)
                    || "diagnostic".equalsIgnoreCase(category)) {
                continue;
            }

            candidates.put(
                    entityId,
                    new Candidate(
                            entityId,
                            clean(object.optString("en", null)),
                            type));
        }
        return candidates;
    }

    private void loadStates(Map<String, Candidate> candidates, LoadCallback callback) {
        try {
            commandPort.send("get_states", new JSONObject(), (success, result, error) -> {
                if (!success || !(result instanceof JSONArray)) {
                    callback.onResult(null, "I couldn't load routines from Home Assistant.");
                    return;
                }

                JSONArray states = (JSONArray) result;
                Map<String, RoutineItem> found = new LinkedHashMap<>();
                for (int index = 0; index < states.length(); index++) {
                    Object raw = states.opt(index);
                    if (!(raw instanceof JSONObject)) {
                        continue;
                    }

                    JSONObject object = (JSONObject) raw;
                    String entityId = clean(object.optString("entity_id", null));
                    Candidate candidate = entityId == null ? null : candidates.get(entityId);
                    if (candidate == null) {
                        continue;
                    }

                    JSONObject attributes = object.optJSONObject("attributes");
                    String friendlyName = attributes == null
                            ? null
                            : clean(attributes.optString("friendly_name", null));
                    String displayName = candidate.displayName != null
                            ? candidate.displayName
                            : friendlyName;
                    if (displayName == null) {
                        continue;
                    }

                    found.put(
                            entityId,
                            new RoutineItem(entityId, displayName, candidate.type));
                }

                List<RoutineItem> items = new ArrayList<>(found.values());
                items.sort(Comparator.comparing(
                        RoutineItem::displayName,
                        String.CASE_INSENSITIVE_ORDER));
                callback.onResult(items, null);
            });
        } catch (RuntimeException unavailable) {
            callback.onResult(null, "I couldn't load routines from Home Assistant.");
        }
    }

    private static String entityCategory(Object categoryRef, Object categories) {
        if (categoryRef == null || categoryRef == JSONObject.NULL) {
            return null;
        }

        if (categoryRef instanceof Number) {
            int index = ((Number) categoryRef).intValue();
            if (categories instanceof JSONArray) {
                JSONArray array = (JSONArray) categories;
                return index >= 0 && index < array.length()
                        ? clean(array.optString(index, null))
                        : null;
            }
            if (categories instanceof JSONObject) {
                return clean(((JSONObject) categories).optString(String.valueOf(index), null));
            }
            return null;
        }

        if (categoryRef instanceof String) {
            String value = clean((String) categoryRef);
            if (value == null) {
                return null;
            }
            if (categories instanceof JSONObject) {
                String mapped = clean(((JSONObject) categories).optString(value, null));
                return mapped != null ? mapped : value;
            }
            return value;
        }

        return null;
    }

    private static RoutineItem.Type typeForEntityId(String entityId) {
        String value = clean(entityId);
        if (value == null) {
            return null;
        }
        if (value.startsWith("script.")) {
            return RoutineItem.Type.SCRIPT;
        }
        if (value.startsWith("scene.")) {
            return RoutineItem.Type.SCENE;
        }
        return null;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private final class SceneExecution implements Execution {
        private final RunCallback callback;
        private boolean done;

        SceneExecution(RunCallback callback) {
            this.callback = callback;
        }

        void start(RoutineItem routine) {
            final JSONObject body;
            try {
                body = serviceBody("scene", routine.entityId());
            } catch (JSONException couldNotEncode) {
                complete(false, "I couldn't prepare that routine.");
                return;
            }

            try {
                commandPort.send("call_service", body, this::onServiceResult);
            } catch (RuntimeException unavailable) {
                complete(false, "Home Assistant is offline.");
            }
        }

        void onServiceResult(boolean success, Object result, String error) {
            complete(success, success ? null : "Home Assistant didn't run that.");
        }

        private void complete(boolean success, String error) {
            synchronized (this) {
                if (done) {
                    return;
                }
                done = true;
            }
            callback.onResult(success, error);
        }

        @Override
        public synchronized void cancel() {
            done = true;
        }
    }

    private final class ScriptExecution implements Execution {
        private final RoutineItem routine;
        private final RunCallback callback;

        private StateChangePort.Subscription subscription;
        private boolean serviceSucceeded;
        private boolean seenOn;
        private boolean seenOffAfterOn;
        private boolean done;

        ScriptExecution(RoutineItem routine, RunCallback callback) {
            this.routine = routine;
            this.callback = callback;
        }

        void start() {
            try {
                stateChangePort.subscribe(this::onStateChanged, this::onSubscribed);
            } catch (RuntimeException unavailable) {
                complete(false, "I couldn't watch that script finish.");
            }
        }

        private void onSubscribed(StateChangePort.Subscription active, String error) {
            if (active == null || error != null) {
                if (active != null) {
                    active.cancel();
                }
                complete(false, "I couldn't watch that script finish.");
                return;
            }

            synchronized (this) {
                if (done) {
                    active.cancel();
                    return;
                }
                subscription = active;
            }

            final JSONObject body;
            try {
                body = serviceBody("script", routine.entityId());
            } catch (JSONException couldNotEncode) {
                complete(false, "I couldn't prepare that routine.");
                return;
            }

            try {
                commandPort.send("call_service", body, this::onServiceResult);
            } catch (RuntimeException unavailable) {
                complete(false, "Home Assistant is offline.");
            }
        }

        private void onStateChanged(String entityId, String state) {
            if (!routine.entityId().equals(clean(entityId))) {
                return;
            }

            boolean finish;
            synchronized (this) {
                if (done) {
                    return;
                }
                String value = clean(state);
                if ("on".equals(value)) {
                    seenOn = true;
                } else if ("off".equals(value) && seenOn) {
                    seenOffAfterOn = true;
                }
                finish = serviceSucceeded && seenOffAfterOn;
            }
            if (finish) {
                complete(true, null);
            }
        }

        private void onServiceResult(boolean success, Object result, String error) {
            if (!success) {
                complete(false, "Home Assistant didn't run that.");
                return;
            }

            boolean finish;
            synchronized (this) {
                if (done) {
                    return;
                }
                serviceSucceeded = true;
                finish = seenOffAfterOn;
            }
            if (finish) {
                complete(true, null);
            }
        }

        private void complete(boolean success, String error) {
            final StateChangePort.Subscription toCancel;
            synchronized (this) {
                if (done) {
                    return;
                }
                done = true;
                toCancel = subscription;
                subscription = null;
            }
            if (toCancel != null) {
                toCancel.cancel();
            }
            callback.onResult(success, error);
        }

        @Override
        public void cancel() {
            final StateChangePort.Subscription toCancel;
            synchronized (this) {
                if (done) {
                    return;
                }
                done = true;
                toCancel = subscription;
                subscription = null;
            }
            if (toCancel != null) {
                toCancel.cancel();
            }
        }
    }

    private static JSONObject serviceBody(String domain, String entityId) throws JSONException {
        return new JSONObject()
                .put("domain", domain)
                .put("service", "turn_on")
                .put("target", new JSONObject().put("entity_id", entityId));
    }

    private static final class Candidate {
        final String entityId;
        final String displayName;
        final RoutineItem.Type type;

        Candidate(String entityId, String displayName, RoutineItem.Type type) {
            this.entityId = entityId;
            this.displayName = displayName;
            this.type = type;
        }
    }
}
