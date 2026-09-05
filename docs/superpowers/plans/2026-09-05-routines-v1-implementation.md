# BOOP Shield Routines v1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a whole-house Shield Routines page that discovers usable Home Assistant scripts and scenes, runs them locally, and gives truthful `Running…` / `Done` / `Didn’t run` feedback without regressing the checkpointed Home dashboard.

**Architecture:** Implement Routines as a separate vertical slice: `RoutineItem` for domain data, `RoutinesRepository` for Home Assistant discovery/execution protocol, `RoutinesController` for per-row state/timers/concurrency, `TvRoutinesView` for remote-first rendering, and minimal composition in `BoopHomeActivity`. Reuse the existing authenticated `HomeAssistantWebSocket` and its state-change subscription facility; do not move routine logic into `HomeAssistantRepository`, `FocusCardView`, or the protected overlay runtime.

**Tech Stack:** Java 17, Android SDK 36 / minSdk 26, Android Views, OkHttp 4.12.0 WebSocket, `org.json`, JUnit 4.13.2, Python `unittest`, Gradle 9.6.0 in CI.

**Spec:** `docs/superpowers/specs/2026-09-05-routines-design.md`

## Global Constraints

- Base implementation branch: `boop-shield-home-implementation`.
- Preserve checkpoint branch `checkpoint-shield-home-f8e8135`; never move or modify it.
- Keep package/application ID `com.boop.shieldoverlay` and existing Shield overlay naming.
- Keep core routine execution local to Home Assistant; no OpenAI/cloud dependency.
- Routines v1 contains only `script.*` and `scene.*` entities; no automations, sensors, voice, editing, history, room filtering, paging, or favourites.
- Discovery is whole-house and sorted case-insensitively by display name.
- Use Home Assistant `config/entity_registry/list_for_display`; by API contract this endpoint already omits disabled entities. Still filter hidden entries and config/diagnostic entity categories locally.
- Scene success means Home Assistant accepted the exact `scene.turn_on` service call.
- Script success requires both service-call success and an observed exact-target `on` then `off` lifecycle after subscription was established; an isolated `off` never counts as completion.
- Script completion timeout: exactly `120_000L` ms in v1.
- Result hold time for `Done` / `Didn’t run`: exactly `2_000L` ms in v1.
- Same running row ignores duplicate Select; other ready rows remain runnable.
- No raw Home Assistant errors appear in routine cards.
- Focus visuals must never reorder children; do not add `bringToFront()`.
- Preserve existing Home dashboard behaviour, room selection, favourite discovery, physical binary control, 10-second confirmation, and rail navigation.
- Do not add Android permissions.

---

## File Structure

**Create**

- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutineItem.java` — immutable routine identity/type model.
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java` — Home Assistant discovery and execution protocol only.
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesController.java` — page state, per-row execution state, duplicate suppression, timers, offline transition, cleanup.
- `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java` — repository discovery and execution contract tests.
- `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesControllerTest.java` — controller state/timer/concurrency tests.

**Modify**

- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java` — replace placeholder with ScrollView-backed chunky routine list.
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java` — compose the Routines slice on the already-authenticated Home Assistant socket.
- `tests/test_shield_overlay_source.py` — source-level regression guards for scrolling, wiring, and no focus reordering.

**Must remain untouched unless a failing test proves otherwise**

- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java`
- protected overlay files covered by `test_protected_overlay_runtime_stays_free_of_voice_network_and_ha_code`.

---

### Task 1: Routine model and whole-house discovery

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutineItem.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`

**Interfaces:**
- Consumes: `HomeAssistantWebSocket.Callback`, `org.json.JSONObject`, `org.json.JSONArray`.
- Produces:
  - `RoutineItem(String entityId, String displayName, RoutineItem.Type type)`
  - `String entityId()`
  - `String displayName()`
  - `RoutineItem.Type type()`
  - `String domain()` returning `script` or `scene`
  - `String typeLabel()` returning `Script` or `Scene`
  - `RoutinesRepository.CommandPort.send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback)`
  - `RoutinesRepository.LoadCallback.onResult(List<RoutineItem> routines, String error)`
  - `RoutinesRepository.loadRoutines(LoadCallback callback)`

- [ ] **Step 1: Write the failing discovery tests**

Add tests that prove the repository uses the display registry, excludes non-routine/hidden/config/diagnostic entries, uses friendly-name fallback, and returns one alphabetical list.

```java
@Test
public void discoveryUsesEnabledDisplayRegistryAndReturnsVisibleScriptsAndScenesAlphabetically() throws Exception {
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
                    .put(new JSONObject().put("ei", "script.config").put("en", "Config Script").put("ec", 0))
                    .put(new JSONObject().put("ei", "scene.diagnostic").put("en", "Diagnostic Scene").put("ec", 1))
                    .put(new JSONObject().put("ei", "switch.not_a_routine").put("en", "Switch")));
    commands.command(0).callback.onResult(true, registry, null);

    assertEquals("get_states", commands.command(1).type);
    JSONArray states = new JSONArray()
            .put(state("script.bedtime", "off", "Bedtime fallback"))
            .put(state("scene.movie", "2026-09-05T12:00:00+00:00", "Movie fallback"))
            .put(state("scene.alpha", "unknown", "Alpha Scene"));
    commands.command(1).callback.onResult(true, states, null);

    assertNull(error.get());
    assertNotNull(result.get());
    assertEquals(3, result.get().size());
    assertEquals("Alpha Scene", result.get().get(0).displayName());
    assertEquals("Bedtime", result.get().get(1).displayName());
    assertEquals("Movie Night", result.get().get(2).displayName());
    assertEquals(RoutineItem.Type.SCENE, result.get().get(0).type());
    assertEquals(RoutineItem.Type.SCRIPT, result.get().get(1).type());
}

private static JSONObject state(String entityId, String value, String friendlyName) throws Exception {
    return new JSONObject()
            .put("entity_id", entityId)
            .put("state", value)
            .put("attributes", new JSONObject().put("friendly_name", friendlyName));
}
```

Also add a test where `entity_categories` is a `JSONObject` keyed by numeric strings so the parser accepts both the currently-used array form and Home Assistant's documented compact mapping form:

```java
@Test
public void discoveryDecodesObjectCategoryMapAndStillExcludesConfigEntities() throws Exception {
    RecordingCommands commands = new RecordingCommands();
    RoutinesRepository repository = new RoutinesRepository(commands);
    AtomicReference<List<RoutineItem>> result = new AtomicReference<>();

    repository.loadRoutines((items, error) -> result.set(items));
    JSONObject registry = new JSONObject()
            .put("entity_categories", new JSONObject().put("0", "config").put("1", "diagnostic"))
            .put("entities", new JSONArray()
                    .put(new JSONObject().put("ei", "script.good").put("en", "Good"))
                    .put(new JSONObject().put("ei", "scene.settings").put("en", "Settings").put("ec", 0)));
    commands.command(0).callback.onResult(true, registry, null);
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

The test fixture must use a simple command recorder:

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

private static final class RecordingCommands implements RoutinesRepository.CommandPort {
    final List<Command> commands = new ArrayList<>();

    @Override
    public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
        commands.add(new Command(type, body, callback));
    }

    Command command(int index) {
        return commands.get(index);
    }
}
```

- [ ] **Step 2: Run the unit test and verify red**

Run:

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: compilation/test failure because `RoutineItem` and `RoutinesRepository` do not exist.

- [ ] **Step 3: Implement `RoutineItem`**

Create an immutable Java model with validation and exact type/domain labels:

```java
public final class RoutineItem {
    public enum Type {
        SCRIPT,
        SCENE
    }

    private final String entityId;
    private final String displayName;
    private final Type type;

    public RoutineItem(String entityId, String displayName, Type type) {
        this.entityId = requireText(entityId, "entity ID");
        this.displayName = requireText(displayName, "display name");
        if (type == null) {
            throw new IllegalArgumentException("routine type is required");
        }
        this.type = type;
    }

    public String entityId() { return entityId; }
    public String displayName() { return displayName; }
    public Type type() { return type; }
    public String domain() { return type == Type.SCRIPT ? "script" : "scene"; }
    public String typeLabel() { return type == Type.SCRIPT ? "Script" : "Scene"; }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
```

- [ ] **Step 4: Implement whole-house discovery in `RoutinesRepository`**

Use `config/entity_registry/list_for_display` first, because Home Assistant only returns enabled entities from this endpoint. Filter `hb`, config/diagnostic category, and non-`script`/`scene` domains before the state lookup. Then call `get_states` to provide `friendly_name` fallback and to drop registry entries with no current entity state.

The core flow must be:

```java
public void loadRoutines(LoadCallback callback) {
    if (callback == null) {
        throw new IllegalArgumentException("routines callback is required");
    }
    commandPort.send("config/entity_registry/list_for_display", new JSONObject(),
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
                loadStates(candidates, callback);
            });
}
```

`collectCandidates` must:

- derive type from `ei` prefix only (`script.` / `scene.`),
- skip `hb == true`,
- decode `ec` against either `JSONArray` or `JSONObject` category maps,
- skip `config` and `diagnostic`,
- retain `en` as preferred display name when present.

`loadStates` must call `get_states`, match candidate entity IDs, use candidate name first then `attributes.friendly_name`, skip entries with neither, construct `RoutineItem`, and sort with:

```java
items.sort(Comparator.comparing(
        RoutineItem::displayName,
        String.CASE_INSENSITIVE_ORDER));
```

Return an empty list as a successful live result when no routines qualify; do not convert a legitimate empty list into an offline error.

- [ ] **Step 5: Run tests and verify green**

Run:

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: all existing Shield unit tests plus `RoutinesRepositoryTest` pass.

- [ ] **Step 6: Commit Task 1**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutineItem.java \
        shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java
git commit -m "feat: discover Shield routines"
```

---

### Task 2: Scene execution with accepted-service truthfulness

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- Modify: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`

**Interfaces:**
- Consumes: `RoutineItem` from Task 1.
- Produces:
  - `RoutinesRepository.RunCallback.onResult(boolean success, String error)`
  - `RoutinesRepository.Execution.cancel()`
  - `RoutinesRepository.run(RoutineItem routine, RunCallback callback)` returning a non-null `Execution`.

- [ ] **Step 1: Write failing scene-execution tests**

Add:

```java
@Test
public void sceneRunTargetsExactEntityAndCompletesWhenServiceIsAccepted() throws Exception {
    RecordingCommands commands = new RecordingCommands();
    RoutinesRepository repository = new RoutinesRepository(commands);
    AtomicBoolean callbackSeen = new AtomicBoolean(false);
    AtomicBoolean success = new AtomicBoolean(false);

    RoutinesRepository.Execution execution = repository.run(
            new RoutineItem("scene.movie_night", "Movie Night", RoutineItem.Type.SCENE),
            (ok, error) -> {
                callbackSeen.set(true);
                success.set(ok);
            });

    assertNotNull(execution);
    assertEquals(1, commands.commands.size());
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
public void cancelledSceneSuppressesLateServiceCallback() throws Exception {
    RecordingCommands commands = new RecordingCommands();
    RoutinesRepository repository = new RoutinesRepository(commands);
    AtomicBoolean callbackSeen = new AtomicBoolean(false);

    RoutinesRepository.Execution execution = repository.run(
            new RoutineItem("scene.movie_night", "Movie Night", RoutineItem.Type.SCENE),
            (ok, error) -> callbackSeen.set(true));
    execution.cancel();
    commands.command(0).callback.onResult(true, null, null);

    assertFalse(callbackSeen.get());
}
```

- [ ] **Step 2: Run tests and verify red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: failure because `run`, `RunCallback`, and `Execution` do not exist.

- [ ] **Step 3: Implement scene execution**

Add interfaces:

```java
public interface RunCallback {
    void onResult(boolean success, String error);
}

public interface Execution {
    void cancel();
}
```

`run` must validate non-null arguments and dispatch by `RoutineItem.Type`. Scene execution builds exactly:

```java
JSONObject body = new JSONObject()
        .put("domain", "scene")
        .put("service", "turn_on")
        .put("target", new JSONObject().put("entity_id", routine.entityId()));
```

Use a small `SceneExecution` object with synchronized/idempotent `done` state so `cancel()` suppresses late callbacks. Service rejection calls `RunCallback` with `success=false`; JSON/send exceptions are caught and reported through the callback rather than thrown into the Activity.

- [ ] **Step 4: Run tests and verify green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: all Shield unit tests pass.

- [ ] **Step 5: Commit Task 2**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java
git commit -m "feat: run Shield scenes"
```

---

### Task 3: Script execution with subscribe-first `on` → `off` confirmation

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java`
- Modify: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java`

**Interfaces:**
- Consumes: `HomeAssistantWebSocket.subscribeStateChanges` semantics already proven in the Home path.
- Produces:
  - `RoutinesRepository.StateChangePort` with nested `Listener`, `Subscription`, `Callback`.
  - overloaded constructor `RoutinesRepository(CommandPort commandPort, StateChangePort stateChangePort)`.
  - script branch of `run(...)` that subscribes before service and silently cancels subscriptions through `Execution.cancel()`.

- [ ] **Step 1: Write failing script protocol tests**

Create a combined command/state fake modeled after `BinaryActionTest.RecordingPorts` and add these cases:

```java
@Test
public void scriptSubscribesBeforeServiceAndNeedsTargetOnThenOffPlusServiceSuccess() throws Exception {
    RecordingPorts ports = new RecordingPorts();
    RoutinesRepository repository = new RoutinesRepository(ports, ports);
    AtomicBoolean callbackSeen = new AtomicBoolean(false);
    AtomicBoolean success = new AtomicBoolean(false);

    repository.run(
            new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
            (ok, error) -> {
                callbackSeen.set(true);
                success.set(ok);
            });

    assertTrue(ports.subscriptionRequested);
    assertEquals(0, ports.commands.size());

    ports.ackSubscription();
    assertEquals(1, ports.commands.size());
    Command command = ports.command(0);
    assertEquals("call_service", command.type);
    assertEquals("script", command.body.getString("domain"));
    assertEquals("turn_on", command.body.getString("service"));
    assertEquals("script.bedtime",
            command.body.getJSONObject("target").getString("entity_id"));

    ports.emit("script.other", "on");
    ports.emit("script.bedtime", "off");
    command.callback.onResult(true, null, null);
    assertFalse("off without an observed target on must not finish", callbackSeen.get());

    ports.emit("script.bedtime", "on");
    assertFalse(callbackSeen.get());
    ports.emit("script.bedtime", "off");

    assertTrue(callbackSeen.get());
    assertTrue(success.get());
    assertTrue(ports.cancelled);
}

@Test
public void scriptLifecycleCanArriveBeforeServiceReplyWithoutFalseSuccess() throws Exception {
    RecordingPorts ports = new RecordingPorts();
    RoutinesRepository repository = new RoutinesRepository(ports, ports);
    AtomicBoolean callbackSeen = new AtomicBoolean(false);

    repository.run(
            new RoutineItem("script.goodnight", "Goodnight", RoutineItem.Type.SCRIPT),
            (ok, error) -> callbackSeen.set(true));
    ports.ackSubscription();
    Command command = ports.command(0);

    ports.emit("script.goodnight", "on");
    ports.emit("script.goodnight", "off");
    assertFalse(callbackSeen.get());

    command.callback.onResult(true, null, null);
    assertTrue(callbackSeen.get());
    assertTrue(ports.cancelled);
}

@Test
public void scriptSubscriptionFailureNeverFiresService() throws Exception {
    RecordingPorts ports = new RecordingPorts();
    RoutinesRepository repository = new RoutinesRepository(ports, ports);
    AtomicBoolean success = new AtomicBoolean(true);

    repository.run(
            new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
            (ok, error) -> success.set(ok));
    ports.failSubscription("subscription rejected");

    assertFalse(success.get());
    assertEquals(0, ports.commands.size());
}
```

Also add tests that a rejected service cancels the subscription and that `Execution.cancel()` cancels the subscription without invoking the completion callback.

- [ ] **Step 2: Run tests and verify red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: failure because the repository does not yet expose a routine state-change port or script confirmation path.

- [ ] **Step 3: Implement `StateChangePort` and script execution**

Use a dedicated routines port instead of refactoring the checkpointed Home repository:

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
```

The script execution object must track these booleans independently:

```java
private boolean serviceSucceeded;
private boolean sawRunning;
private boolean sawFinishedAfterRunning;
private boolean done;
```

Rules:

```java
private void onStateChanged(String entityId, String state) {
    if (!routine.entityId().equals(clean(entityId))) {
        return;
    }
    boolean finish = false;
    synchronized (this) {
        if (done) {
            return;
        }
        if ("on".equals(clean(state))) {
            sawRunning = true;
        } else if ("off".equals(clean(state)) && sawRunning) {
            sawFinishedAfterRunning = true;
        }
        finish = serviceSucceeded && sawFinishedAfterRunning;
    }
    if (finish) {
        complete(true, null);
    }
}
```

Subscription must become active before `call_service` is sent. Service success sets `serviceSucceeded=true`; it only completes immediately if `sawFinishedAfterRunning` is already true. Service failure completes false and cancels the active subscription. `complete(...)` and `cancel()` must both be idempotent and release the subscription exactly once.

Do **not** add the 120-second timeout here; that belongs to `RoutinesController`, which owns user-facing operation state and timeout policy.

- [ ] **Step 4: Run tests and verify green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: repository tests and all pre-existing tests pass.

- [ ] **Step 5: Commit Task 3**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesRepository.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesRepositoryTest.java
git commit -m "feat: confirm Shield script completion"
```

---

### Task 4: Per-row Routines controller, timers, duplicate suppression, and offline transition

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesController.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesControllerTest.java`

**Interfaces:**
- Consumes:
  - `RoutinesRepository.LoadCallback`
  - `RoutinesRepository.RunCallback`
  - `RoutinesRepository.Execution`
- Produces:
  - `PageStatus { LOADING, LIVE, OFFLINE }`
  - `RowStatus { READY, RUNNING, DONE, FAILED }`
  - `ViewState.pageStatus()` and `ViewState.rows()`
  - `RowState.routine()`, `RowState.status()`, `RowState.enabled()`
  - `ViewState.loading()` and `ViewState.offline()` factories for Activity pre-controller states.
  - `RepositoryPort.loadRoutines(...)` and `RepositoryPort.run(...)`
  - `SchedulerPort.schedule(long delayMs, Runnable runnable)` returning `ScheduledTask`
  - `start()`, `runRoutine(String entityId)`, `markOffline()`, `close()`.

- [ ] **Step 1: Write failing controller tests**

The first test must prove per-row state and duplicate handling:

```java
@Test
public void runningRowIgnoresDuplicateSelectButOtherRowsRemainRunnable() {
    FakeRepository repository = new FakeRepository();
    ManualScheduler scheduler = new ManualScheduler();
    AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
    RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
    repository.items = Arrays.asList(
            new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT),
            new RoutineItem("scene.movie", "Movie Night", RoutineItem.Type.SCENE));

    controller.start();
    controller.runRoutine("script.bedtime");
    controller.runRoutine("script.bedtime");
    controller.runRoutine("scene.movie");

    assertEquals(2, repository.runCalls.size());
    assertEquals("script.bedtime", repository.runCalls.get(0));
    assertEquals("scene.movie", repository.runCalls.get(1));
    assertEquals(RoutinesController.RowStatus.RUNNING,
            row(rendered.get(), "script.bedtime").status());
    assertEquals(RoutinesController.RowStatus.RUNNING,
            row(rendered.get(), "scene.movie").status());
}
```

Add exact timer tests:

```java
@Test
public void successShowsDoneForTwoSecondsThenReturnsReady() throws Exception {
    FakeRepository repository = new FakeRepository();
    ManualScheduler scheduler = new ManualScheduler();
    AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
    RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
    repository.items = Collections.singletonList(
            new RoutineItem("scene.movie", "Movie Night", RoutineItem.Type.SCENE));

    controller.start();
    controller.runRoutine("scene.movie");
    repository.complete("scene.movie", true, null);

    assertEquals(RoutinesController.RowStatus.DONE,
            row(rendered.get(), "scene.movie").status());
    assertTrue(scheduler.hasDelay(2_000L));
    scheduler.runDelay(2_000L);
    assertEquals(RoutinesController.RowStatus.READY,
            row(rendered.get(), "scene.movie").status());
}

@Test
public void scriptTimeoutCancelsExecutionAndShowsFailed() throws Exception {
    FakeRepository repository = new FakeRepository();
    ManualScheduler scheduler = new ManualScheduler();
    AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
    RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
    repository.items = Collections.singletonList(
            new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT));

    controller.start();
    controller.runRoutine("script.bedtime");
    assertTrue(scheduler.hasDelay(120_000L));

    scheduler.runDelay(120_000L);
    assertTrue(repository.execution("script.bedtime").cancelled);
    assertEquals(RoutinesController.RowStatus.FAILED,
            row(rendered.get(), "script.bedtime").status());
    assertTrue(scheduler.hasDelay(2_000L));
}
```

Add tests for:

- load error -> `OFFLINE` with zero rows,
- service/repository failure -> `FAILED` for 2 seconds -> `READY`,
- `markOffline()` with no active runs -> `OFFLINE` empty immediately,
- `markOffline()` during active run -> cancel run, show affected row `FAILED` with all rows disabled for 2 seconds, then collapse to `OFFLINE` empty,
- `close()` cancels all active executions and scheduled tasks without emitting fake success.

Use a deterministic `ManualScheduler` that records `delayMs`, cancellation state, and runnable; do not use `Thread.sleep`.

- [ ] **Step 2: Run tests and verify red**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: failure because `RoutinesController` does not exist.

- [ ] **Step 3: Implement controller state model and exact constants**

Define:

```java
private static final long RESULT_HOLD_MS = 2_000L;
private static final long SCRIPT_TIMEOUT_MS = 120_000L;
```

`start()` emits `LOADING`, then calls `repository.loadRoutines`. A successful empty list is `LIVE` with zero rows; an error is `OFFLINE` with zero rows.

`runRoutine(entityId)` must only proceed when page status is `LIVE` and that row is `READY`. Before calling the repository it sets that row to `RUNNING` and emits. Store an operation object by exact entity ID so each row has independent execution and timer state.

For scripts only, schedule `SCRIPT_TIMEOUT_MS` before/around the repository call so a missed completion cannot leave `RUNNING` forever. The timeout must verify that the same operation is still active, cancel the execution handle when available, then transition to `FAILED` and schedule the 2-second reset.

Repository callbacks must:

1. ignore callbacks from operations no longer active,
2. cancel the script timeout,
3. transition success to `DONE` and failure to `FAILED`,
4. schedule `RESULT_HOLD_MS`,
5. reset only that row to `READY` if the controller is still live.

Because a fake or real repository callback can arrive synchronously before `run(...)` returns its `Execution`, handle this race explicitly: create/store the operation first; after `run(...)` returns, attach the execution only if that operation is still active, otherwise immediately cancel the returned execution.

Keep shared mutable state behind one private lock and emit immutable snapshot copies outside the lock.

- [ ] **Step 4: Implement the offline transition**

`markOffline()` must:

- set page status to `OFFLINE`,
- cancel every active execution and timeout,
- turn rows that were actively `RUNNING` into `FAILED`,
- disable every row while offline,
- if any running row failed, keep the current rows visible for exactly `2_000L` so `Didn’t run` can be seen, then clear rows to the unavailable-only state,
- if nothing was running, clear rows immediately.

This satisfies both approved behaviours: a socket loss during an operation visibly fails that row, while a normally-offline page does not retain a stale executable list.

- [ ] **Step 5: Run tests and verify green**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: all Shield unit tests pass.

- [ ] **Step 6: Commit Task 4**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoutinesController.java \
        shield-overlay/app/src/test/java/com/boop/shieldoverlay/RoutinesControllerTest.java
git commit -m "feat: manage Shield routine state"
```

---

### Task 5: Replace the Routines placeholder with a remote-first scrolling list

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java`
- Modify: `tests/test_shield_overlay_source.py`

**Interfaces:**
- Consumes: `RoutinesController.ViewState` and `RowState`.
- Produces:
  - `TvRoutinesView.Listener.onRun(String entityId)`
  - `TvRoutinesView.Listener.onContentLeft()`
  - `render(RoutinesController.ViewState state)`
  - `View firstFocusable()`.

- [ ] **Step 1: Write the source regression test first**

Add:

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

- [ ] **Step 2: Run source test and verify red**

```bash
python3 tests/test_shield_overlay_source.py -v
```

Expected: new Routines test fails because the current placeholder extends `LinearLayout` and has no run callback.

- [ ] **Step 3: Implement the scrolling view**

Change `TvRoutinesView` to extend `ScrollView`. Internally use one vertical `LinearLayout` root plus a dedicated routine-row container. Keep the same black background/padding family as the existing Home page.

Constructor shape:

```java
public interface Listener {
    void onRun(String entityId);
    void onContentLeft();
}

public TvRoutinesView(Context context, Listener listener) {
    super(context);
    if (listener == null) {
        throw new IllegalArgumentException("routines listener is required");
    }
    this.listener = listener;
    setFillViewport(true);
    setBackgroundColor(Color.BLACK);
    addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
}
```

Render copy exactly:

- `LOADING` + no rows -> focusable non-clickable card `Finding routines…`
- `OFFLINE` + no rows -> focusable non-clickable card `Routines unavailable right now`
- `LIVE` + no rows -> focusable non-clickable card `No routines found`
- normal row second line -> `Script` or `Scene`
- running row second line -> `Running…`
- done row second line -> `Done`
- failed row second line -> `Didn’t run`

Keep the routine name on the first line while status replaces the small second-line type label. Use `SpannableString` + `RelativeSizeSpan(0.72f)` and `StyleSpan(Typeface.NORMAL)` on the second line; do not modify shared `FocusCardView`.

Every routine card remains focusable. Set clickability from `RowState.enabled()`. The Left key listener must call `listener.onContentLeft()` from every routine/status card.

Maintain a `LinkedHashMap<String, FocusCardView>` and update existing card text/clickability in place when only statuses change. Rebuild the row container only when the ordered routine entity-ID list changes. This prevents execution feedback from destroying focus or reordering cards.

`ScrollView` should rely on Android descendant-focus scrolling for D-pad Up/Down; do not add paging or manual child reordering.

- [ ] **Step 4: Run source and Java tests**

```bash
python3 tests/test_shield_overlay_source.py -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
```

Expected: source regression and all Shield unit tests pass.

- [ ] **Step 5: Commit Task 5**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java \
        tests/test_shield_overlay_source.py
git commit -m "feat: render Shield routines list"
```

---

### Task 6: Wire Routines into the existing authenticated Home socket

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java`
- Modify: `tests/test_shield_overlay_source.py`

**Interfaces:**
- Consumes:
  - `HomeAssistantWebSocket.send(...)`
  - `HomeAssistantWebSocket.subscribeStateChanges(...)`
  - `RoutinesRepository`
  - `RoutinesController`
  - `TvRoutinesView`
- Produces no new public API; Activity remains a composition root only.

- [ ] **Step 1: Write the wiring regression test first**

Add:

```python
def test_home_activity_wires_routines_to_existing_ha_socket_without_touching_overlay_runtime(self):
    home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
    self.assertIn("RoutinesRepository", home)
    self.assertIn("RoutinesController", home)
    self.assertIn("subscribeStateChanges", home)
    self.assertIn("runRoutine", home)
    self.assertIn("routinesView.render", home)
```

- [ ] **Step 2: Run source test and verify red**

```bash
python3 tests/test_shield_overlay_source.py -v
```

Expected: failure because Activity still creates the old placeholder view and no Routines controller/repository exists in its composition path.

- [ ] **Step 3: Add Routines Activity fields and initial state**

Add fields beside the existing dashboard fields:

```java
private RoutinesController routinesController;
private RoutinesController.ViewState routinesState;
private TvRoutinesView routinesView;
```

When `showHomeShell()` starts, initialize:

```java
routinesController = null;
routinesState = RoutinesController.ViewState.loading();
routinesView = null;
```

Do not create a second WebSocket connection.

- [ ] **Step 4: Compose repository/controller in `connectForHomeDashboard(...).onReady()`**

After the existing Home dashboard repository/controller is created, build a separate routines state-change adapter against the **same** `socket`:

```java
RoutinesRepository.StateChangePort routinesStateChangePort =
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

Create the repository:

```java
RoutinesRepository repository =
        new RoutinesRepository(socket::send, routinesStateChangePort);
```

Adapt it to `RoutinesController.RepositoryPort`, catching `RuntimeException` from a no-longer-ready socket and converting it to a failed callback rather than throwing into the UI.

Create the scheduler from the existing main handler:

```java
RoutinesController.SchedulerPort scheduler = (delayMs, runnable) -> {
    mainHandler.postDelayed(runnable, delayMs);
    return () -> mainHandler.removeCallbacks(runnable);
};
```

Close any previous routines controller before replacing it. The listener stores `routinesState` and renders only when `routinesView != null`, always through `runOnUiThread` and with the same stale-socket guards already used by Home.

Call `routinesController.start()` once after construction.

- [ ] **Step 5: Render the real Routines page from the rail**

Replace the placeholder constructor path with:

```java
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

When rendering Home or Settings, set `routinesView = null`; when rendering Routines/Settings, preserve the existing `homeView = null` behaviour.

- [ ] **Step 6: Wire socket-loss and lifecycle cleanup**

In `onOffline` and `onReauthRequired` for the Home socket:

- if `routinesController != null`, call `routinesController.markOffline()`;
- otherwise set `routinesState = RoutinesController.ViewState.offline()` and render it if the Routines page is visible.

Before replacing a routines controller on reconnect, call `close()` on the old one.

In `clearNavigationShellState()` and `onDestroy()`, call a private helper that closes the current routines controller, nulls it, and prevents timer/subscription leaks. Do not add routine cleanup to protected overlay service code.

- [ ] **Step 7: Run all local regression tests**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle -p shield-overlay :app:assembleDebug --stacktrace
```

Expected: all Python source tests pass, all Java unit tests pass, and `shield-overlay/app/build/outputs/apk/debug/app-debug.apk` is produced.

- [ ] **Step 8: Verify checkpointed Home internals were not modified**

Run:

```bash
git diff --exit-code checkpoint-shield-home-f8e8135 -- \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java
```

Expected: no diff and exit code 0.

- [ ] **Step 9: Commit Task 6**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java \
        tests/test_shield_overlay_source.py
git commit -m "feat: wire Shield routines dashboard"
```

---

### Task 7: Fresh Shield CI and physical sofa verification

**Files:**
- No production file changes expected.
- CI workflow: `.github/workflows/build-shield-overlay-poc.yml` (read-only unless CI itself reveals a genuine workflow defect).

**Interfaces:**
- Consumes the complete implementation from Tasks 1-6.
- Produces one verified installable debug APK artifact and a physical pass/fail report.

- [ ] **Step 1: Run the exact CI test commands locally one final time**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle -p shield-overlay :app:assembleDebug --stacktrace
```

Expected: all pass.

- [ ] **Step 2: Confirm no new permission or package drift locally when Android build tools are available**

```bash
AAPT="${ANDROID_HOME}/build-tools/36.0.0/aapt"
APK=shield-overlay/app/build/outputs/apk/debug/app-debug.apk
"$AAPT" dump badging "$APK" | grep "package: name='com.boop.shieldoverlay'"
"$AAPT" dump permissions "$APK"
```

Expected: package remains `com.boop.shieldoverlay`; no `RECORD_AUDIO`, `RECEIVE_BOOT_COMPLETED`, or accessibility permission appears.

- [ ] **Step 3: Push the implementation branch and require a fresh Shield workflow**

```bash
git push origin boop-shield-home-implementation
```

The pushed `shield-overlay/**` / source-test changes trigger `Build BOOP Shield Overlay POC`. Do not accept a stale earlier run. Required green steps are:

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

- [ ] **Step 4: Install the artifact on the Shield and run the minimum physical test**

Use at least one known scene and one script whose execution is long enough to observe the running state.

Physical checklist:

1. Launch BOOP and confirm the existing Home page still loads.
2. Open `Routines` from the left rail.
3. Confirm scripts and scenes appear together in one alphabetical list.
4. Confirm each normal row shows a small `Script` or `Scene` second line.
5. Navigate far enough down the list to prove D-pad Up/Down scrolling follows focus.
6. Run a scene and observe `Running…` -> `Done` -> normal card after about 2 seconds.
7. Run the long script and observe `Running…` while it is active.
8. Confirm the script does **not** say `Done` until the exact script has visibly completed in Home Assistant.
9. Press Select twice rapidly on the same running script and confirm it does not start twice.
10. While that script is running, run a different ready routine and confirm the page remains usable.
11. Press Left and confirm focus returns to the `Routines` rail item.
12. Return to Home and toggle the existing favourite; confirm the physical device still changes and BOOP updates only after real Home Assistant confirmation.

- [ ] **Step 5: If all physical checks pass, create the functional checkpoint**

Create a branch from the exact physically verified implementation commit, using the project naming pattern:

```text
checkpoint-shield-routines-<short-sha>
```

Do not move `checkpoint-shield-home-f8e8135`.

If any sofa check fails, do **not** create the functional checkpoint. Capture the exact symptom, reproduce it, and return to systematic debugging with a failing test before changing production code.

---

## Final self-review checklist before execution

- Every approved spec requirement is mapped to Tasks 1-7.
- Disabled routine exclusion is implemented by the documented `list_for_display` contract; hidden/config/diagnostic filtering is local and tested.
- Script completion cannot succeed on an isolated `off` event.
- Service reply and script lifecycle can arrive in either order without false success.
- Timeout policy lives in the controller, not the repository.
- Result timers are testable without sleeping.
- Socket loss during a running routine shows `Didn’t run` before collapsing to the unavailable state.
- Routine UI state updates do not rebuild/reorder unchanged cards.
- No Home repository or shared FocusCardView changes are planned.
- No overlay/runtime permissions or voice/sensor/cloud work are introduced.
