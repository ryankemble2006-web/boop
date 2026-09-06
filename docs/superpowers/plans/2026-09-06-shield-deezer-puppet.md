# Shield Deezer Puppet Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (selected by Ryan) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Deezer-only H1 puppet APK candidate, ready for separately approved signed installation and a physical Shield checkpoint.

**Architecture:** A callback-only Android observer feeds a pure state/policy layer. A separately testable clock and frame loop drive the existing H1 motion sampler; Settings owns opt-in, never pairing. The overlay only consumes snapshots and draws; it owns no media access or HA connection.

**Tech Stack:** Java 17, Android API 26 minimum / compile and target 36, AGP 9.4.0, Gradle 9.6.0, JUnit 4.13.2, existing Python source regressions. No new runtime dependency.

**Spec:** `docs/superpowers/specs/2026-09-06-shield-deezer-puppet-design.md` (approved 2026-09-06).

## Global Constraints

- Deezer is selected by the observed exact package `deezer.android.app`.
- Keep the 1.2x preview pace: one cycle every three seconds, not beat synchronisation.
- Default the new feature preference to OFF, including existing installations.
- No microphone, network client or HA dependency enters the overlay classes.
- Notification callbacks are no-ops; no notification enumeration, cancellation or payload parsing. No playback control calls.
- No title, artist, token or notification contents in diagnostics or persistent storage.
- Keep `HomeAssistantRepository`, `FocusCardView`, `OverlayGeometry`'s ordinary-eye calculation and `OverlayWindowSpec` unchanged.
- Only Settings composition in `BoopHomeActivity` may change; pairing, authentication, favourite control and Routines logic are out of scope.
- No new animation timer or polling loop is added to `BoopOverlayService`.
- Use `com.boop.shieldoverlay` and the existing stable BOOP signer for any installable update.
- No commits, index changes, pushes, workflow dispatch, installation, grants, data clearing or existing checkpoint changes during these implementation tasks. Root owns those later approval boundaries.
- Preserve all pre-existing uncommitted H1/P1 preview work. Do not rewrite the sampler or preview assets. Do not edit another task's report or shared memory files; root maintains them.
- Follow TDD: record an expected failing assertion before production behaviour, then covering green output. Existing source guards remain; new tests exercise behaviour or parsed build contracts, not text-presence tests.
- No child agents. Root dispatches every implementer and reviewer. One implementer at a time.

## File and interface map

All Java paths below are relative to `shield-overlay/app/src/main/java/com/boop/shieldoverlay/` (MAIN); test paths use `shield-overlay/app/src/test/java/com/boop/shieldoverlay/` (TEST). The prefixes are directory aliases, not literal folders to create.

| File | Responsibility / producer |
| --- | --- |
| MAIN/DeezerPuppetPolicy.java | Pure mode and stable session selection, Task 1 |
| MAIN/MediaPuppetClock.java | Visible-playing-only accumulated motion phase, Task 1 |
| MAIN/DeezerSessionObserver.java | Pure lifecycle/controller bridge with small Android boundary ports, Task 2 |
| MAIN/MediaPuppetState.java | Pure deduplicated snapshots/subscriptions, Task 2 |
| MAIN/DeezerMediaListenerService.java | Notification listener + Android adapters, Task 2 |
| MAIN/DeezerPuppetAccess.java | Application-context preferences/access/rebind wrapper, Task 2 |
| MAIN/MediaPuppetFrameLoop.java | Pure one-pending-frame scheduling and draw callbacks, Task 3 |
| MAIN/HeadphoneGeometry.java | H1-only envelope/scaling, Task 3 |
| MAIN/HeadphoneRenderer.java | Decode once and draw H1 with sampler, Task 3 |
| MAIN/BoopOverlayView.java, MAIN/BoopOverlayService.java | Narrow render/subscription/display integration, Task 3 |
| MAIN/TvSettingsView.java, MAIN/BoopHomeActivity.java | Remote-first opt-in/status/access action, Task 3 |
| tools/deezer-puppet/test-core.ps1 | Reusable pure Java test runner, Task 1 |
| tools/deezer-puppet/README.md | Build/testing boundaries, Task 4 |

No new mandatory Application startup work: `DeezerPuppetAccess.get(Context)` obtains a process-local singleton using only application context, lazily. The listener and overlay/Settings receive the same `MediaPuppetState` from this wrapper. No Activity/View retained in static fields.

## Local tool setup and baseline

Root will provide a task-local tool-path note with verified paths, not private credentials. Use per-command Git safe.directory, never global configuration. The known Python child launcher needs both PYTHONHOME and its DLL directories on the process PATH; do not edit the signing test to work around this environment issue. Root's fresh source baseline is 42 tests, zero failures after that environment setup, with no source changes.

Pure Java focused command (runner introduced in Task 1):

```powershell
./tools/deezer-puppet/test-core.ps1 -JavacPath $boopJavac -JavaPath $boopJava -JunitPath $boopJunit -HamcrestPath $boopHamcrest -TestClasses @('com.boop.shieldoverlay.MediaPuppetClockTest','com.boop.shieldoverlay.DeezerPuppetPolicyTest')
```

Android validation uses task-local GRADLE_USER_HOME and ANDROID_USER_HOME, JAVA_HOME set to the available Java 17, ANDROID_HOME to the existing SDK, and the existing Gradle binary:

```powershell
& $boopGradle -p shield-overlay :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --offline --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Shield build or validation failed' }
```

If uncached dependencies or SDK ACLs block this, report the exact failure; request tool permission for the build rather than changing SDK versions or dropping a test. No local debug-signed output is an installable deliverable.

---

### Task 1: Pure playback policy and motion clock

**Files:** Create MAIN/DeezerPuppetPolicy.java, MAIN/MediaPuppetClock.java, TEST/DeezerPuppetPolicyTest.java, TEST/MediaPuppetClockTest.java, tools/deezer-puppet/test-core.ps1 and tools/deezer-puppet/.gitignore (build/). Existing MAIN/MediaPuppetMotion.java is read-only.

**Interfaces:**

```java
public final class DeezerPuppetPolicy {
    public enum Mode { EYES, HEADPHONES_REST, HEADPHONES_PLAYING }
    public static final class Session {
        public final long id;
        public final String packageName;
        public final Integer playbackState;
        public Session(long id, String packageName, Integer playbackState);
    }
    public static Mode mode(boolean enabled, boolean granted, boolean connected, Integer playbackState);
    public static long select(java.util.List<Session> sessions, long currentId); // 0 = none
}
public final class MediaPuppetClock {
    public void update(DeezerPuppetPolicy.Mode mode, boolean visible, boolean animationsEnabled, long nowMs);
    public long sampleTimeMs(long nowMs); // normalized 0..3599, scaled at 1.2x
    public boolean isRunning();
    public void reset(long nowMs);
}
```

- [ ] Write the first failing pause/resume regression with hand-derived expectations. Minimal compile-only API stubs may throw UnsupportedOperationException; do not implement behaviour until the assertion/run demonstrates it is missing.

```java
@Test public void pausedTimeAndDuplicatePlayingDoNotRestartPhase() {
    MediaPuppetClock clock = new MediaPuppetClock();
    clock.update(HEADPHONES_PLAYING, true, true, 1000L);
    assertEquals(1200L, clock.sampleTimeMs(2000L));
    clock.update(HEADPHONES_REST, true, true, 2000L);
    assertEquals(1200L, clock.sampleTimeMs(9000L));
    clock.update(HEADPHONES_PLAYING, true, true, 9000L);
    clock.update(HEADPHONES_PLAYING, true, true, 9500L);
    assertEquals(2400L, clock.sampleTimeMs(10000L));
}
```

- [ ] Add and run literal state-table tests. Integer Android playback values: 3 -> PLAYING; 2,4,5,6,8,9,10,11 -> REST; null,0,1,7 and unknown values -> EYES. Any false enable/grant/connection input -> EYES. Selection ignores non-Deezer; prefers playing, then current eligible, then first eligible, otherwise 0; duplicate IDs must not create unstable selection.
- [ ] Add a pure-Java PowerShell runner using the existing preview runner's parameter pattern. Explicitly compile only the new pure classes/tests that exist plus MediaPuppetMotion; never wildcard all Android sources. Use Java 17, existing JUnit/Hamcrest, an ignored build directory, native exit checks and optional TestClasses. New test files from later tasks are added to the runner's explicit allowlist by their owning task. No generated test expectations or source greps.
- [ ] Implement the policy using the exact table; select against modes with enable/grant/connection true, applying stable preference. Package comparison is exact. The clock accrues only when running, ignores negative/backward deltas, bounds elapsed phase before multiplying (avoid overflow), uses fractional carry or equivalent integer scaling so frequent frame sampling does not lose time, freezes while hidden/reduced-motion and resets on EYES. A state update first accounts for elapsed time under the old state, then changes run eligibility.

```java
// Clock transition ordering; arithmetic belongs in an overflow-safe accrue method.
accrue(nowMs);
if (mode == DeezerPuppetPolicy.Mode.EYES) reset(nowMs);
running = mode == DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING && visible && animationsEnabled;
```

- [ ] Run focused tests green including: three-second real-time wrap; many 1ms samples equal one long interval; 59ms skip hold/resume; hidden time; system-animation off; EYES reset; very large and backward/negative times. Assert selection stability under list reorder and eligibility changes.
- [ ] Self-review and write the report with RED/GREEN commands/output and exact files. Do not commit. Root reviews this task before Task 2.

### Task 2: Callback observer, state holder and opt-in/access plumbing

**Files:** Create MAIN/DeezerSessionObserver.java, MAIN/MediaPuppetState.java, MAIN/DeezerMediaListenerService.java, MAIN/DeezerPuppetAccess.java; TEST/DeezerSessionObserverTest.java and TEST/MediaPuppetStateTest.java. Modify shield-overlay/app/src/main/AndroidManifest.xml to declare only the new protected service; extend tools/deezer-puppet/test-core.ps1 allowlist. Task 1 classes read-only unless a proven interface defect is reported to root.

**Interfaces:**

```java
public final class MediaPuppetState {
    public static final class Snapshot {
        public final boolean enabled, granted, connected;
        public final long sessionId;
        public final DeezerPuppetPolicy.Mode mode;
        public String status(); // Off / Access needed / Connecting / On
    }
    public interface Listener { void onChanged(Snapshot snapshot); }
    public void updateAccess(boolean enabled, boolean granted, boolean connected);
    public void updateSession(long sessionId, Integer playbackState);
    public Snapshot snapshot();
    public Runnable subscribe(Listener listener); // immediate snapshot; idempotent unsubscribe
}
public final class DeezerPuppetAccess {
    public static DeezerPuppetAccess get(android.content.Context context);
    public MediaPuppetState state();
    public void setEnabled(boolean enabled);
    public void refresh(); // re-read OS grant; no grant/rebind loop
    public boolean openAccessSettings(android.app.Activity activity); // false if unavailable
}
public final class DeezerSessionObserver {
    public interface SessionPort {
        Object token(); String packageName(); Integer playbackState();
        void register(Callback callback); void unregister(Callback callback);
    }
    public interface Callback { void stateChanged(Integer state); void destroyed(); }
    public interface Platform {
        void register(Runnable sessionsChanged); void unregister(Runnable sessionsChanged);
        java.util.List<SessionPort> sessions();
    }
    public DeezerSessionObserver(Platform platform, MediaPuppetState output);
    public void connect(); public void disconnect();
}
```

- [ ] Write/run failing behavioural tests for state subscription dedup, idempotent unsubscribe, opt-out and loss of grant clearing stale session data (restoring grant alone cannot resurrect PLAYING). Status gives Off precedence over Android grant. Callbacks mutate on the main thread in production; tests are single-threaded.
- [ ] Implement state with immutable snapshots and defensive listener iteration so unsubscribe during delivery is safe. Access update that loses eligibility clears cached session; grant/enable returning requires observer snapshot. Identical externally visible snapshots do not notify. No Activity/View references or Android imports in pure classes.

```java
MediaPuppetState state = new MediaPuppetState();
state.updateAccess(true, true, true);
state.updateSession(7L, 3);
assertEquals(HEADPHONES_PLAYING, state.snapshot().mode);
state.updateAccess(false, true, true);
state.updateAccess(true, true, true);
assertEquals(EYES, state.snapshot().mode); // old session cannot silently revive
```

- [ ] Use test-only fake ports with retained callbacks and controllable lists. Assert the observer's real output and boundary registrations: only exact Deezer, register before initial read, stable IDs for equal tokens across wrapper recreation, new ID on reattach, atomic multi-controller reconciliation, detached callbacks ignored, session destruction selects replacement, duplicate connect/disconnect safe, registration/read SecurityException cleans up and returns original eyes.
- [ ] Implement observer using a LinkedHashMap keyed by token and monotonic nonzero local IDs. Capture both watcher identity and generation for callback guards. Register the list listener then reconcile; batch changes while reconciling, publish one selected session at the end. Disconnect invalidates generation before unregistering; cleanup must continue if one unregister raises an expected lifecycle/security exception. Catch expected access failures at the Android boundary; do not swallow arbitrary programmer exceptions.

```java
// Core stale-callback contract, exercised using a detached fake callback.
if (!connected || generation != callbackGeneration || watchers.get(token) != watcher) return;
// Selection uses Task 1 policy; output always publishes actual state or (0,null).
```

- [ ] Implement the Android listener/ports with MediaSessionManager and MediaController callbacks on the main looper. Start only after onListenerConnected and opt-in with actual grant. Do not fetch metadata or notifications. onListenerDisconnected/onDestroy/opt-out release observations. New callbacks and snapshots must reach the same process-local state holder consumed by Task 3. Preserve lifecycle ownership: service registers/unregisters its preference/access listener, no static service instance.
- [ ] Access wrapper: use application context, named non-credential SharedPreferences, default false. Refresh via NotificationManager.isNotificationListenerAccessGranted for this exact component; on API 26 use read-only enabled-listener-component parsing if the method is unavailable (API 27 guard). Observe relevant grant-setting changes with a ContentObserver and refresh on service callbacks/Settings resume. Preference change publishes access state and notifies the live service to start/stop. Explicit On with existing grant may requestRebind once. Never grant access, run shell, or enumerate unrelated notification data. openAccessSettings catches ActivityNotFoundException/SecurityException and returns false.
- [ ] Declare the listener non-exported, permission BIND_NOTIFICATION_LISTENER_SERVICE and action android.service.notification.NotificationListenerService. Parse XML in a focused test if adding a manifest contract test; validate service attributes/permissions semantically rather than searching strings.
- [ ] Run pure tests green and Android compile/unit tests once; report exact proof and platform-only physical gaps. No device operations, commits or changes to Settings/renderer yet.

### Task 3: H1 rendering, lifecycle frame loop and remote Settings integration

**Files:** Create MAIN/MediaPuppetFrameLoop.java, MAIN/HeadphoneGeometry.java, MAIN/HeadphoneRenderer.java; TEST/MediaPuppetFrameLoopTest.java, TEST/HeadphoneGeometryTest.java. Modify MAIN/BoopOverlayView.java, MAIN/BoopOverlayService.java, MAIN/TvSettingsView.java, MAIN/BoopHomeActivity.java (Settings composition/lifecycle subscription only); add shield-overlay/app/src/main/res/drawable-nodpi/boop_headphones.png by copying tools/media-motion-preview/assets/music.png unchanged. Extend pure runner allowlist. Add tests for the permission-unavailable Settings outcome through a small pure presenter if needed, in MAIN/DeezerPuppetSettingsModel.java and TEST/DeezerPuppetSettingsModelTest.java; it is in scope only if UI decisions would otherwise be untestable.

**Interfaces:** Consumes Task 1 policy/clock and Task 2 access/state. Produces:

```java
public final class MediaPuppetFrameLoop {
    public interface Scheduler { void post(Runnable frame); void cancel(Runnable frame); }
    public interface Drawing { void draw(long sampleTimeMs); }
    public MediaPuppetFrameLoop(Scheduler scheduler, java.util.function.LongSupplier now, Drawing drawing);
    public void update(DeezerPuppetPolicy.Mode mode, boolean visible, boolean animationsEnabled);
    public void reset(); // fresh selected session
    public void detach();
}
// Existing view gains package-private integration methods:
// void setPuppetSnapshot(MediaPuppetState.Snapshot snapshot)
// void setDisplayActive(boolean active)
// void refreshAnimationPreference()
```

- [ ] Write/run failing real-frame-loop tests using a fake scheduler queue/clock and a list of drawn sample times. Check at most one pending frame, duplicate PLAYING no extra scheduling, cancel-on-pause/hidden/detach, one static redraw on actual changes, paused/hidden interval exclusion, stale cancelled callback no draw/reschedule, resume and reset behaviour. With system animations disabled, assert zero repeating frames, not just unchanged position.

```java
loop.update(HEADPHONES_PLAYING, true, true);
assertEquals(1, scheduler.pendingCount());
loop.update(HEADPHONES_PLAYING, true, true);
assertEquals(1, scheduler.pendingCount());
loop.update(HEADPHONES_REST, true, true);
assertEquals(0, scheduler.pendingCount());
int atPause = drawings.size();
scheduler.runCancelledCallback();
assertEquals(atPause, drawings.size());
```

- [ ] Implement frame loop with clock transition before scheduling decision, generation guard per posted callback, cancellation of pending callback on eligibility loss, no repeating frame when not running. A draw callback updates the view's cached sample and invalidates; never call View.draw directly. Use Choreographer adapters with identity-stable cancellation, not a media polling timer.
- [ ] Read the PNG alpha bounds and preview's H1 pivot `(768,580)` with source canvas `1536x1024`. Record source hash/dimensions and copy the PNG without image editing. Write independent geometry tests for 1920x1080, 3840x2160 and small/zero dimensions: full motion envelope fits, positive bounded size, eye span approximately 14% display width and minimum 3% insets. Build an H1-only Layout type in HeadphoneGeometry containing width,height,x,y,scale,originX,originY. Conservative analytical sine/rotation extrema are preferable to tests computing expectations with the same helper. Keep ordinary OverlayGeometry unchanged.
- [ ] Implement H1 renderer decode once plus reusable Paint/Rect objects. For each sample use MediaPuppetMotion.music, apply approved pivot, translated origin/scale, draw only the transparent asset; no per-frame bitmap decode/network/metadata. BoopOverlayView retains exact ordinary-eye path/wake and chooses H1 only on headphone mode. Visibility/attach/detach/display/animator changes update the frame loop. Cancel one-time view-property animation when entering H1 so it cannot squash the headphones unexpectedly; do not introduce repeated wakes.
- [ ] Before protected integration, add failing consumer-level tests for frame visibility and parsed window contract as applicable; retain and run all existing overlay source regression tests unchanged. Service subscribes/unsubscribes to MediaPuppetState, sets view snapshot and updates layout only on mode/real display geometry change. Keep one window, original hide/show commands and display listener; service delegates animation flags to view, never schedules a timer. Observe animator-duration-scale changes and display state, clean up observers; no frames with Home visible, display off, paused or detached. Newly created view receives latest snapshot, including a callback that arrived while hidden.
- [ ] Settings: preserve original constructor via an overload if needed; add two focusable cards after room control (feature toggle, Manage access) and one nonfocusable explanation/status. On first enable use a remote-friendly explicit confirmation dialog explaining broad Android notification access; cancel leaves Off. On confirmation call setEnabled(true), never grant. Off calls setEnabled(false) immediately. Subscribe while the Settings page exists and unsubscribe on page replacement/activity destruction; refresh on resume without rebuilding focused cards. Keep Left return and original firstFocusable room card. If openAccessSettings returns false, display the one-time computer-setup explanation with no crash or automatic retries. Show grant state even while feature Off.
- [ ] Run focused tests green, all source/Shield unit tests, build and lint; inspect packaged resource alpha and manifest. Report renderer/math tests separately from unverified actual Shield rendering and Android binding. No installation or permission grants.

### Task 4: Full candidate verification, build provenance and handoff

**Files:** Create tools/deezer-puppet/README.md. Modify the same directory's test-core.ps1 only if complete-suite execution needs adjustment. No production behaviour changes in this task without routing back to its implementer. Root updates BOOP_MEMORY.txt, BOOP_STATUS.md and task output memory after review.

**Interfaces:** Consumes all earlier tests/code and produces a local APK candidate, parsed build/signing evidence and exact physical-test handoff. Candidate is not installable until its stable signer matches.

- [ ] Run the focused pure suite, existing 42 source regressions, existing motion/export and preview-clock tests, full Shield unit tests, assembleDebug and lintDebug on the unchanged candidate. Record test counts, warnings, paths and SHA256 in report. Fix test environment through local variables, not by weakening assertions or suppressing lint.
- [ ] Inspect the packaged APK, not just sources. aapt badging/permissions: exact app ID, target36, expected protected listener and existing overlay service. apksigner verification: record public certificate only and compare to committed expected fingerprint. Label a nonmatching debug candidate clearly; do not publish it as an update or install it.

```powershell
& $boopAapt dump badging $boopApk
& $boopAapt dump permissions $boopApk
& $boopApksigner verify --print-certs $boopApk
Get-FileHash -LiteralPath $boopApk -Algorithm SHA256
```

- [ ] README documents opt-in/Off vs Android grant distinction, missing Shield settings screen, exact test/build commands, expected states, artifact trust labels and non-destructive recovery requirement. Include the spec's physical test gate, one step at a time. Do not claim a ten-minute soak, HDR or new physical checkpoint has happened.
- [ ] Report candidate readiness and outstanding signed-build/publication/device-approval boundaries. Root performs whole-change review, then requests only the authority genuinely required to produce/install the stable-signed APK. No pushing, signing-key creation, data clearing or grant action here.

## Self-review and execution choice

Coverage: Task 1 state/time; Task 2 consent state and callback lifecycle; Task 3 rendering/window/Settings integration; Task 4 build proof and physical handoff. Cross-task types and method names match the interface map. User selected helper agents with task reviews; no further execution-method question is required. Signed publication, device access and physical checkpoint remain separate gates.
