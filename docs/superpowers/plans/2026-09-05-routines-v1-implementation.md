# BOOP Shield Routines v1 Implementation Plan

> **2026-09-05 compatibility amendment:** Home Assistant `automation.*`, `script.*` and `scene.*` entities are all BOOP **Routines**. The correction extends discovery with `automation.*`, invokes exact targets with `automation.trigger`, and uses the visible type label `Routine` for all three.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a whole-house Shield Routines page that discovers usable Home Assistant automations, scripts and scenes, runs them locally, and gives truthful `Running…` / `Done` / `Didn’t run` feedback without regressing the checkpointed Home dashboard.

**Architecture:** Keep Routines as a separate vertical slice: `RoutineItem` holds routine identity/type, `RoutinesRepository` owns Home Assistant discovery and execution protocol, `RoutinesController` owns per-row state/timers/concurrency, `TvRoutinesView` owns remote-first rendering, and `BoopHomeActivity` only composes those pieces on the already-authenticated Home Assistant WebSocket. Reuse `HomeAssistantWebSocket.subscribeStateChanges(...)`; do not move routine logic into `HomeAssistantRepository`, `FocusCardView`, or the protected overlay runtime.

**Tech Stack:** Java 17, Android SDK 36 / minSdk 26, Android Views, OkHttp 4.12.0 WebSocket, `org.json`, JUnit 4.13.2, Python `unittest`, Gradle 9.6.0 in CI.

**Spec:** `docs/superpowers/specs/2026-09-05-routines-design.md`

## Global Constraints

- Implementation branch: `boop-shield-home-implementation`.
- Never move or modify `checkpoint-shield-home-f8e8135`.
- Keep package/application ID `com.boop.shieldoverlay` and existing `shieldoverlay` naming.
- Core routine execution remains local to Home Assistant; no OpenAI/cloud dependency.
- Routines v1 contains `automation.*`, `script.*` and `scene.*`; no sensors, voice, editing, history, room filtering, paging, routine favourites, or automation enable/disable controls.
- All three Home Assistant entity types use the BOOP-facing label `Routine`.
- Automation success means the exact `automation.trigger` service request was accepted.
- Discovery is whole-house and sorted case-insensitively by display name.
- Use `config/entity_registry/list_for_display`; Home Assistant's API contract excludes disabled entities from that response. Still filter hidden entries and `config`/`diagnostic` categories locally.
- Scene success means the exact `scene.turn_on` service request was accepted.
- Script success requires service-call success plus an observed exact-target `on` then `off` lifecycle after subscription activation. An isolated `off` event never counts as completion.
- Script completion timeout is exactly `120_000L` ms.
- `Done` / `Didn’t run` hold time is exactly `2_000L` ms.
- Duplicate Select on the same running row is ignored; other ready rows remain runnable.
- Routine cards never expose raw Home Assistant errors.
- Do not add `bringToFront()` or any focus behaviour that reorders vertical children.
- Preserve Home dashboard, selected-room flow, favourite selection, physical binary control, the 10-second Home confirmation window, and rail navigation.
- Do not add Android permissions.

## File Map

**Create**
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutineItem.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesController.java`
- `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`
- `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesControllerTest.java`

**Modify**
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java`
- `tests/test_shield_overlay_source.py`

**Expected untouched**
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java`
- protected overlay service/view/geometry/window-spec files.

---

### Task 1: Model and whole-house discovery

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutineItem.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`

**Interfaces:**

```java
public final class RoutineItem {
    public enum Type { AUTOMATION, SCRIPT, SCENE }
    public RoutineItem(String entityId, String displayName, Type type);
    public String entityId();
    public String displayName();
    public Type type();
    public String domain();      // "automation", "script" or "scene"
    public String typeLabel();   // "Routine"
}
```

```java
public final class RoutinesRepository {
    public interface CommandPort {
        void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback);
    }
    public interface LoadCallback {
        void onResult(List<RoutineItem> routines, String error);
    }
    public RoutinesRepository(CommandPort commandPort);
    public void loadRoutines(LoadCallback callback);
}
```

Private discovery helper types/methods introduced in this task:

```java
private static final class Candidate {
    final String entityId;
    final String displayName;
    final RoutineItem.Type type;
}

private Map<String, Candidate> collectCandidates(JSONArray entities, Object categories);
private void loadStates(Map<String, Candidate> candidates, LoadCallback callback);
private static String entityCategory(Object categoryRef, Object categories);
private static RoutineItem.Type typeForEntityId(String entityId);
private static String clean(String value);
```

- [ ] **Step 1: Write the red discovery tests**

Create `RoutinesRepositoryTest.java` with a command recorder and two discovery tests.

```java
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

private static JSONObject state(String entityId, String value, String friendlyName) throws Exception {
    return new JSONObject()
            .put("entity_id", entityId)
            .put("state", value)
            .put("attributes", new JSONObject().put("friendly_name", friendlyName));
}
```

Primary test:

```java
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
                    .put(new JSONObject().put("ei", "automation.keep_awake").put("en", "Keep Awake"))
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
                    .put(state("automation.keep_awake", "on", "Keep Awake fallback"))
                    .put(state("scene.alpha", "unknown", "Alpha Scene")),
            null);

    assertNull(error.get());
    assertEquals(4, result.get().size());
    assertEquals("Alpha Scene", result.get().get(0).displayName());
    assertEquals("Bedtime", result.get().get(1).displayName());
    assertEquals("Keep Awake", result.get().get(2).displayName());
    assertEquals("Movie Night", result.get().get(3).displayName());
    assertEquals(RoutineItem.Type.SCENE, result.get().get(0).type());
    assertEquals(RoutineItem.Type.SCRIPT, result.get().get(1).type());
    assertEquals(RoutineItem.Type.AUTOMATION, result.get().get(2).type());
    assertEquals("Routine", result.get().get(2).typeLabel());
}
```

Category-map compatibility test:

```java
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
```

- [ ] **Step 2: Run red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: compilation failure because `RoutineItem` / `RoutinesRepository` do not exist.

- [ ] **Step 3: Implement `RoutineItem`**

Use exact domain/label mapping and reject blank constructor values. No Android dependencies.

```java
public String domain() {
    if (type == Type.AUTOMATION) {
        return "automation";
    }
    return type == Type.SCRIPT ? "script" : "scene";
}

public String typeLabel() {
    return "Routine";
}
```

- [ ] **Step 4: Implement `RoutinesRepository.loadRoutines`**

Required command order:

```java
commandPort.send("config/entity_registry/list_for_display", new JSONObject(), registryCallback);
```

After a successful display-registry result, call:

```java
commandPort.send("get_states", new JSONObject(), statesCallback);
```

`collectCandidates(...)` rules:
- read entity ID from compact key `ei`,
- `typeForEntityId` accepts only prefixes `automation.`, `script.` and `scene.`,
- skip `hb == true`,
- decode `ec` against either `JSONArray` or `JSONObject` category maps,
- skip decoded `config` and `diagnostic`,
- retain compact display name `en` when present.

`loadStates(...)` rules:
- only use states whose `entity_id` exists in the candidate map,
- preferred name = candidate `en`; fallback = state `attributes.friendly_name`,
- skip a candidate with no usable name or no current state entry,
- do **not** reject scene state `unknown`; a never-activated scene must still be discoverable,
- sort exactly with:

```java
items.sort(Comparator.comparing(
        RoutineItem::displayName,
        String.CASE_INSENSITIVE_ORDER));
```

A successful empty list is `callback.onResult(Collections.emptyList(), null)`, not an offline error.

- [ ] **Step 5: Run green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: all Shield unit tests pass.

- [ ] **Step 6: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutineItem.java \
        shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java
git commit -m "feat: discover Shield routines"
```

---

### Task 2: Scene execution

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- Modify: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`

**Interfaces added:**

```java
public interface RunCallback {
    void onResult(boolean success, String error);
}

public interface Execution {
    void cancel();
}

public Execution run(RoutineItem routine, RunCallback callback);
```

Private type added:

```java
private final class ImmediateExecution implements Execution {
    private final RunCallback callback;
    private boolean done;
    void start(RoutineItem routine);
    void onServiceResult(boolean success, Object result, String error);
    @Override public void cancel();
}
```

- [ ] **Step 1: Write automation and scene tests**

Add one automation test that asserts `call_service`, domain `automation`, service `trigger`, and the exact `target.entity_id`. Its success callback follows the same accepted-service-result contract as a scene.

```java
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
```

- [ ] **Step 2: Run red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: missing run/Execution API.

- [ ] **Step 3: Implement immediate automation and scene execution**

Build exactly:

```java
String service = routine.type() == RoutineItem.Type.AUTOMATION ? "trigger" : "turn_on";
JSONObject body = new JSONObject()
        .put("domain", routine.domain())
        .put("service", service)
        .put("target", new JSONObject().put("entity_id", routine.entityId()));
```

`ImmediateExecution` serves both automations and scenes and must synchronize its `done` flag. `cancel()` sets `done=true`. A service callback after cancellation does nothing. Service rejection returns `success=false`; JSON encoding or socket-send runtime exceptions are converted into one failed callback rather than escaping into Activity.

- [ ] **Step 4: Run green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java
git commit -m "feat: run Shield scenes"
```

---

### Task 3: Script subscribe-first completion confirmation

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- Modify: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`

**Interfaces added:**

```java
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

public RoutinesRepository(CommandPort commandPort, StateChangePort stateChangePort);
```

Private type added:

```java
private final class ScriptExecution implements Execution {
    private final RoutineItem routine;
    private final RunCallback callback;
    private StateChangePort.Subscription subscription;
    private boolean serviceSucceeded;
    private boolean sawRunning;
    private boolean sawFinishedAfterRunning;
    private boolean done;

    void start();
    void onSubscribed(StateChangePort.Subscription active, String error);
    void onStateChanged(String entityId, String state);
    void onServiceResult(boolean success, Object result, String error);
    void complete(boolean success, String error);
    @Override public void cancel();
}
```

- [ ] **Step 1: Add script protocol fake and red tests**

Extend the test recorder:

```java
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
        subscriptionCallback.onResult(() -> cancelled = true, null);
    }

    void failSubscription(String message) {
        subscriptionCallback.onResult(null, message);
    }

    void emit(String entityId, String state) {
        listener.onStateChanged(entityId, state);
    }
}
```

Strict lifecycle test:

```java
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
```

Race test:

```java
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
```

Also add exact tests for:
- subscription failure -> no service command and failed callback,
- service rejection -> subscription cancelled and failed callback,
- `Execution.cancel()` -> subscription cancelled and no completion callback.

- [ ] **Step 2: Run red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 3: Implement script execution**

`run(...)` dispatches `AUTOMATION` and `SCENE` to `ImmediateExecution`, and `SCRIPT` to `ScriptExecution`. A script with no `StateChangePort` fails without sending the service command.

`ScriptExecution.start()` calls `stateChangePort.subscribe(...)` first. Only `onSubscribed(active, null)` may send `call_service`.

State logic must be exactly:

```java
private void onStateChanged(String entityId, String state) {
    if (!routine.entityId().equals(clean(entityId))) {
        return;
    }
    boolean finish;
    synchronized (this) {
        if (done) {
            return;
        }
        String cleanState = clean(state);
        if ("on".equals(cleanState)) {
            sawRunning = true;
        } else if ("off".equals(cleanState) && sawRunning) {
            sawFinishedAfterRunning = true;
        }
        finish = serviceSucceeded && sawFinishedAfterRunning;
    }
    if (finish) {
        complete(true, null);
    }
}
```

Service success sets `serviceSucceeded=true` and only completes if `sawFinishedAfterRunning` is already true. `complete(...)` and `cancel()` are idempotent and release the subscription exactly once.

Do not add the 120-second timeout in the repository; timeout policy belongs to the controller.

- [ ] **Step 4: Run green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java
git commit -m "feat: confirm Shield script completion"
```

---

### Task 4: Controller state, timers, duplicate suppression, and socket-loss behaviour

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesController.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesControllerTest.java`

**Interfaces:**

```java
public final class RoutinesController implements AutoCloseable {
    public enum PageStatus { LOADING, LIVE, OFFLINE }
    public enum RowStatus { READY, RUNNING, DONE, FAILED }

    public interface RepositoryPort {
        void loadRoutines(RoutinesRepository.LoadCallback callback);
        RoutinesRepository.Execution run(RoutineItem routine, RoutinesRepository.RunCallback callback);
    }

    public interface ScheduledTask {
        void cancel();
    }

    public interface SchedulerPort {
        ScheduledTask schedule(long delayMs, Runnable runnable);
    }

    public interface Listener {
        void onViewState(ViewState state);
    }

    public static final class RowState {
        public RoutineItem routine();
        public RowStatus status();
        public boolean enabled();
    }

    public static final class ViewState {
        public PageStatus pageStatus();
        public List<RowState> rows();
        public static ViewState loading();
        public static ViewState offline();
    }

    public RoutinesController(RepositoryPort repository, SchedulerPort scheduler, Listener listener);
    public void start();
    public void runRoutine(String entityId);
    public void markOffline();
    @Override public void close();
}
```

Private state types/constants:

```java
private static final long RESULT_HOLD_MS = 2_000L;
private static final long SCRIPT_TIMEOUT_MS = 120_000L;

private static final class Operation {
    final RoutineItem routine;
    RoutinesRepository.Execution execution;
    ScheduledTask timeout;
}
```

- [ ] **Step 1: Create deterministic test fakes and write red tests**

Use this scheduler rather than sleeping:

```java
private static final class ManualScheduler implements RoutinesController.SchedulerPort {
    private static final class Entry implements RoutinesController.ScheduledTask {
        final long delayMs;
        final Runnable runnable;
        boolean cancelled;

        Entry(long delayMs, Runnable runnable) {
            this.delayMs = delayMs;
            this.runnable = runnable;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    final List<Entry> entries = new ArrayList<>();

    @Override
    public RoutinesController.ScheduledTask schedule(long delayMs, Runnable runnable) {
        Entry entry = new Entry(delayMs, runnable);
        entries.add(entry);
        return entry;
    }

    boolean hasDelay(long delayMs) {
        for (Entry entry : entries) {
            if (!entry.cancelled && entry.delayMs == delayMs) {
                return true;
            }
        }
        return false;
    }

    void runDelay(long delayMs) {
        for (Entry entry : new ArrayList<>(entries)) {
            if (!entry.cancelled && entry.delayMs == delayMs) {
                entry.cancelled = true;
                entry.runnable.run();
                return;
            }
        }
        throw new AssertionError("No active task for delay " + delayMs);
    }
}
```

Repository fake:

```java
private static final class FakeExecution implements RoutinesRepository.Execution {
    boolean cancelled;
    @Override public void cancel() { cancelled = true; }
}

private static final class PendingRun {
    final RoutinesRepository.RunCallback callback;
    final FakeExecution execution;
    PendingRun(RoutinesRepository.RunCallback callback, FakeExecution execution) {
        this.callback = callback;
        this.execution = execution;
    }
}

private static final class FakeRepository implements RoutinesController.RepositoryPort {
    List<RoutineItem> items = Collections.emptyList();
    String loadError;
    final List<String> runCalls = new ArrayList<>();
    final Map<String, PendingRun> pending = new HashMap<>();

    @Override
    public void loadRoutines(RoutinesRepository.LoadCallback callback) {
        callback.onResult(items, loadError);
    }

    @Override
    public RoutinesRepository.Execution run(
            RoutineItem routine,
            RoutinesRepository.RunCallback callback) {
        runCalls.add(routine.entityId());
        FakeExecution execution = new FakeExecution();
        pending.put(routine.entityId(), new PendingRun(callback, execution));
        return execution;
    }

    void complete(String entityId, boolean success, String error) {
        PendingRun run = pending.get(entityId);
        run.callback.onResult(success, error);
    }

    FakeExecution execution(String entityId) {
        return pending.get(entityId).execution;
    }
}
```

Row helper:

```java
private static RoutinesController.RowState row(
        RoutinesController.ViewState state,
        String entityId) {
    for (RoutinesController.RowState row : state.rows()) {
        if (entityId.equals(row.routine().entityId())) {
            return row;
        }
    }
    throw new AssertionError("Missing row " + entityId);
}
```

Write tests proving:

1. `start()` emits `LOADING` then `LIVE`, including a legitimate live empty list.
2. load error emits `OFFLINE` with zero rows.
3. same running row is not submitted twice while another ready row can still run.
4. scene/repository success -> `DONE`, scheduled `2_000L`, then `READY`.
5. repository failure -> `FAILED`, scheduled `2_000L`, then `READY`.
6. script run schedules `120_000L`; timeout cancels its `Execution`, shows `FAILED`, then schedules `2_000L`.
7. `markOffline()` with no active run clears rows immediately.
8. `markOffline()` during a run cancels it, shows that row `FAILED` with every row disabled for `2_000L`, then collapses to `OFFLINE` with zero rows.
9. `close()` cancels active executions and scheduled tasks without emitting success.

Duplicate/concurrency test core:

```java
controller.start();
controller.runRoutine("script.bedtime");
controller.runRoutine("script.bedtime");
controller.runRoutine("scene.movie");
assertEquals(Arrays.asList("script.bedtime", "scene.movie"), repository.runCalls);
```

Timeout test core:

```java
controller.runRoutine("script.bedtime");
assertTrue(scheduler.hasDelay(120_000L));
scheduler.runDelay(120_000L);
assertTrue(repository.execution("script.bedtime").cancelled);
assertEquals(RoutinesController.RowStatus.FAILED,
        row(rendered.get(), "script.bedtime").status());
assertTrue(scheduler.hasDelay(2_000L));
```

- [ ] **Step 2: Run red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 3: Implement controller state machine**

`start()` first emits `ViewState.loading()`, then calls `repository.loadRoutines`. Success stores the returned ordered list, gives every row `READY`, sets page `LIVE`, and emits. Error clears rows, sets `OFFLINE`, and emits.

`runRoutine(entityId)` only proceeds when page is `LIVE` and that exact row is `READY`. Create/store an `Operation` **before** invoking the repository, set row `RUNNING`, and emit. For scripts, schedule `SCRIPT_TIMEOUT_MS` for that operation.

Handle synchronous-callback race explicitly:

```java
RoutinesRepository.Execution execution = repository.run(routine, callback);
synchronized (lock) {
    Operation current = operations.get(routine.entityId());
    if (current == operation) {
        operation.execution = execution;
    } else if (execution != null) {
        execution.cancel();
    }
}
```

Repository completion:
- ignore if operation is no longer current,
- remove the operation,
- cancel its timeout,
- state = `DONE` on success, `FAILED` on failure,
- schedule `RESULT_HOLD_MS`,
- reset only that row to `READY` if page is still `LIVE`.

Timeout:
- verify the operation is still current,
- remove it,
- cancel its execution when present,
- mark row `FAILED`,
- schedule `RESULT_HOLD_MS`.

All shared maps/lists/statuses live behind one private lock. Build immutable snapshot copies under the lock, then call `listener.onViewState(snapshot)` outside the lock.

- [ ] **Step 4: Implement `markOffline()` and `close()`**

`markOffline()`:
- set page `OFFLINE`,
- cancel all active operation timeouts/executions,
- convert only rows that were `RUNNING` to `FAILED`,
- disable all rows because page is offline,
- if any row failed, retain rows for `2_000L`, then clear them and emit offline-empty,
- otherwise clear rows immediately.

`close()` silently cancels all active operation handles, script timeouts, result-reset tasks, and any offline-collapse task. It must not emit `Done` or `Didn’t run` during destruction.

- [ ] **Step 5: Run green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 6: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesController.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesControllerTest.java
git commit -m "feat: manage Shield routine state"
```

---

### Task 5: Remote-first scrolling Routines view

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java`
- Modify: `tests/test_shield_overlay_source.py`

**Interfaces:**

```java
public final class TvRoutinesView extends ScrollView {
    public interface Listener {
        void onRun(String entityId);
        void onContentLeft();
    }

    public TvRoutinesView(Context context, Listener listener);
    public void render(RoutinesController.ViewState state);
    public View firstFocusable();
}
```

Private view helpers/state:

```java
private final LinearLayout content;
private final LinearLayout rows;
private final LinkedHashMap<String, FocusCardView> cards = new LinkedHashMap<>();
private FocusCardView statusCard;

private void rebuildRows(List<RoutinesController.RowState> states);
private void updateRows(List<RoutinesController.RowState> states);
private boolean sameEntityOrder(List<RoutinesController.RowState> states);
private CharSequence cardText(RoutinesController.RowState row);
private FocusCardView statusCard(String text);
```

- [ ] **Step 1: Add red source regression**

```python
def test_routines_page_is_scrollable_remote_first_and_does_not_reorder_cards(self):
    source = self.read("java/com/boop/shieldoverlay/TvRoutinesView.java")
    self.assertIn("extends ScrollView", source)
    self.assertIn("setFillViewport(true)", source)
    self.assertIn("FocusCardView", source)
    self.assertIn("KEYCODE_DPAD_LEFT", source)
    self.assertIn("onRun", source)
    self.assertNotIn("bringToFront()", source)
```

- [ ] **Step 2: Run red**

```bash
python3 tests/test_shield_overlay_source.py -v
```

Expected: the new test fails against the current placeholder `LinearLayout`.

- [ ] **Step 3: Implement `ScrollView` layout and exact copy**

Use one internal vertical `LinearLayout` and `setFillViewport(true)`. Keep black background and the Home page's large/chunky spacing family.

Empty/status copy:
- `LOADING`, no rows -> `Finding routines…`
- `OFFLINE`, no rows -> `Routines unavailable right now`
- `LIVE`, no rows -> `No routines found`

Routine card first line always remains the routine name. The second line is:
- ready -> `Routine`
- running -> `Running…`
- done -> `Done`
- failed -> `Didn’t run`

Use `SpannableString`, `RelativeSizeSpan(0.72f)`, and `StyleSpan(Typeface.NORMAL)` on the second line. Do not change shared `FocusCardView`.

Each row:
- remains focusable,
- calls `listener.onRun(entityId)` only when `RowState.enabled()` is true,
- handles D-pad Left by calling `listener.onContentLeft()` and returning `true`.

Preserve focus during status updates: if the ordered entity-ID list is unchanged, update the existing `FocusCardView` objects in place. Rebuild only when entity order/list changes. This prevents `Running…` / `Done` refreshes from resetting or moving focus.

Android `ScrollView` handles descendant focus scrolling for Up/Down; do not add paging and do not reorder children.

- [ ] **Step 4: Run green source + Java tests**

```bash
python3 tests/test_shield_overlay_source.py -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java \
        tests/test_shield_overlay_source.py
git commit -m "feat: render Shield routines list"
```

---

### Task 6: Activity composition on the existing Home Assistant socket

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java`
- Modify: `tests/test_shield_overlay_source.py`

**Consumes:**
- `HomeAssistantWebSocket.send(...)`
- `HomeAssistantWebSocket.subscribeStateChanges(...)`
- all Routines interfaces from Tasks 1-5.

**Activity fields added:**

```java
private RoutinesController routinesController;
private RoutinesController.ViewState routinesState;
private TvRoutinesView routinesView;
```

**Private Activity helper added:**

```java
private void closeRoutinesController();
```

- [ ] **Step 1: Add red wiring regression**

```python
def test_home_activity_wires_routines_to_existing_ha_socket(self):
    home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
    self.assertIn("RoutinesRepository", home)
    self.assertIn("RoutinesController", home)
    self.assertIn("subscribeStateChanges", home)
    self.assertIn("runRoutine", home)
    self.assertIn("routinesView.render", home)
```

- [ ] **Step 2: Run red**

```bash
python3 tests/test_shield_overlay_source.py -v
```

- [ ] **Step 3: Initialize Routines shell state**

In `showHomeShell()` initialize:

```java
closeRoutinesController();
routinesState = RoutinesController.ViewState.loading();
routinesView = null;
```

Do not create a second socket.

- [ ] **Step 4: Compose Routines after the existing socket reaches `onReady()`**

Map the same socket's state subscription into the separate routines port:

```java
RoutinesRepository.StateChangePort stateChangePort =
        new RoutinesRepository.StateChangePort() {
            @Override
            public void subscribe(
                    RoutinesRepository.StateChangePort.Listener listener,
                    RoutinesRepository.StateChangePort.Callback callback) {
                socket.subscribeStateChanges(
                        listener::onStateChanged,
                        (subscription, error) -> {
                            RoutinesRepository.StateChangePort.Subscription mapped =
                                    subscription == null ? null : subscription::cancel;
                            callback.onResult(mapped, error);
                        });
            }
        };
```

Create repository:

```java
RoutinesRepository repository = new RoutinesRepository(socket::send, stateChangePort);
```

Create exact controller adapter:

```java
RoutinesController.RepositoryPort repositoryPort = new RoutinesController.RepositoryPort() {
    @Override
    public void loadRoutines(RoutinesRepository.LoadCallback callback) {
        try {
            repository.loadRoutines(callback);
        } catch (RuntimeException unavailable) {
            callback.onResult(null, "Home Assistant is offline.");
        }
    }

    @Override
    public RoutinesRepository.Execution run(
            RoutineItem routine,
            RoutinesRepository.RunCallback callback) {
        try {
            return repository.run(routine, callback);
        } catch (RuntimeException unavailable) {
            callback.onResult(false, "Home Assistant is offline.");
            return () -> { };
        }
    }
};
```

Create scheduler from existing `mainHandler`:

```java
RoutinesController.SchedulerPort scheduler = (delayMs, runnable) -> {
    mainHandler.postDelayed(runnable, delayMs);
    return () -> mainHandler.removeCallbacks(runnable);
};
```

Before assigning a new controller, call `closeRoutinesController()`. Controller listener must use `runOnUiThread`, reject stale socket/activity states exactly as the Home listener does, assign `routinesState`, and call `routinesView.render(state)` when the view is visible. Then call `routinesController.start()`.

- [ ] **Step 5: Replace placeholder page construction**

Routines branch in `renderNavigationPage(...)`:

```java
homeView = null;
routinesView = new TvRoutinesView(this, new TvRoutinesView.Listener() {
    @Override
    public void onRun(String entityId) {
        if (routinesController != null) {
            routinesController.runRoutine(entityId);
        }
    }

    @Override
    public void onContentLeft() {
        returnContentFocusToRail();
    }
});
if (routinesState != null) {
    routinesView.render(routinesState);
}
pageView = routinesView;
currentPageFirstFocusable = routinesView.firstFocusable();
```

When rendering Home or Settings, set `routinesView = null` so an off-screen page is never rendered into a detached view.

- [ ] **Step 6: Wire disconnect and cleanup**

In the Home socket's `onOffline` and `onReauthRequired` callbacks:

```java
if (routinesController != null) {
    routinesController.markOffline();
} else {
    routinesState = RoutinesController.ViewState.offline();
    if (routinesView != null) {
        routinesView.render(routinesState);
    }
}
```

`closeRoutinesController()`:

```java
private void closeRoutinesController() {
    RoutinesController controller = routinesController;
    routinesController = null;
    if (controller != null) {
        controller.close();
    }
}
```

Call it from `clearNavigationShellState()`, `onDestroy()`, and before replacing the controller after reconnect. Do not add cleanup or Home Assistant code to `BoopOverlayService`.

- [ ] **Step 7: Run full local regression**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle -p shield-overlay :app:assembleDebug --stacktrace
```

Expected: all source tests green, all Java tests green, APK built at `shield-overlay/app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 8: Verify checkpointed shared Home internals were not modified**

```bash
git diff --exit-code checkpoint-shield-home-f8e8135 -- \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java
```

Expected: exit code 0, no diff.

- [ ] **Step 9: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java \
        tests/test_shield_overlay_source.py
git commit -m "feat: wire Shield routines dashboard"
```

---

### Task 7: Fresh CI, APK, and sofa verification

**Files:**
- No code changes expected.
- Read-only CI contract: `.github/workflows/build-shield-overlay-poc.yml`.

- [ ] **Step 1: Re-run exact local CI-equivalent gates**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle -p shield-overlay :app:assembleDebug --stacktrace
```

Expected: all green.

- [ ] **Step 2: Push and require a fresh workflow run**

```bash
git push origin boop-shield-home-implementation
```

Required green workflow steps:
1. Run BOOP source regression suite
2. Copy approved BOOP eye artwork
3. Set up Java 17
4. Set up Android SDK / Android 36
5. Prepare stable BOOP development signing
6. Run Shield unit tests
7. Build Shield overlay APK
8. Inspect package and permissions
9. Verify stable BOOP signer
10. Upload `BOOP-Shield-Overlay-POC-debug`

Do not accept an older workflow run as evidence for the new implementation commit.

- [ ] **Step 3: Install the fresh artifact and perform the sofa checklist**

Use at least one known automation. If available, also use one scene and one script that stays active long enough to see `Running…`.

1. Launch BOOP; confirm Home still loads.
2. Open Routines from the rail.
3. Confirm automations + scripts + scenes are one alphabetical list.
4. Confirm every normal row shows the small `Routine` second line.
5. Navigate far enough to prove D-pad Up/Down scrolling follows focus.
6. Run an automation; observe `Running…` -> `Done` -> normal row after about two seconds.
7. If available, run a scene and observe the same accepted-call feedback.
8. If available, run the long script; observe `Running…` while active.
9. Confirm the script says `Done` only after that exact script has finished.
10. Double-press Select on the running script; confirm it does not start twice.
11. While that script runs, trigger another ready routine; confirm the rest of the page remains usable.
12. Press Left; confirm focus returns to the Routines rail item.
13. Return Home; toggle the existing favourite and confirm physical control + real state confirmation still work.

- [ ] **Step 4: Create functional checkpoint only after physical green**

From the exact physically verified implementation commit create:

```text
checkpoint-shield-routines-<short-sha>
```

Do not move `checkpoint-shield-home-f8e8135`.

If any physical check fails, do not checkpoint the feature. Capture the exact symptom, reproduce it, add a failing test, then make the smallest evidence-backed fix.

---

## Plan Self-Review

- Spec coverage: discovery, filtering, alphabetical list, scenes, strict script lifecycle, races, duplicate suppression, other-row concurrency, 120-second timeout, 2-second results, offline failure feedback, no stale routine list, D-pad Left, scrolling, and Home preservation all map to Tasks 1-7.
- Placeholder scan: no `TODO`, `TBD`, unnamed helper type, or deferred implementation step remains.
- Type consistency: `RoutineItem`, `RoutinesRepository` ports/callbacks/execution handle, `RoutinesController` ports/states/scheduler, `TvRoutinesView.Listener`, and Activity adapters use the same signatures throughout.
- Isolation: no task requires modifying `HomeAssistantRepository`, `FocusCardView`, or protected overlay runtime.
