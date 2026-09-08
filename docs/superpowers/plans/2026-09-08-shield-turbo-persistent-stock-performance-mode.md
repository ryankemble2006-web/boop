# SHIELD TURBO Persistent Stock Performance Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the physically proven NVIDIA `nv_power_mode` actuator into a persistent, reboot-safe TURBO mode with exact NORMAL rollback and automatic thermal fallback at Android `SEVERE` or higher.

**Architecture:** Keep v1 intentionally narrow: the only writable performance lever is the physically proven `system:nv_power_mode`; NVIDIA vendor boost properties remain read-only evidence. A pure transactional controller owns state transitions, an Android SharedPreferences store persists one atomic encoded snapshot, a trusted-local-ADB port performs the stock mode reads/writes, a private boot receiver starts a foreground thermal-watchdog service only for verified TURBO, and the existing TURBO page exposes a remote-first ON/OFF control plus status readouts.

**Tech Stack:** Kotlin/Java Android app, minSdk 28, targetSdk 36, `PowerManager` thermal APIs, existing loopback `AdbWire`/`LocalBridge`, SharedPreferences, Android foreground service/notification APIs, JUnit 4, Python source/security contracts, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

## Global Constraints

- Owning branch: `shield-turbo-v01`; package stays `com.boop.shieldturbo`.
- Stock-envelope tuning only. No root, custom kernel, bootloader, boot-image, voltage, above-stock-frequency, thermal/throttling bypass, or arbitrary sysfs writes.
- V1 writable performance allowlist contains only `system:nv_power_mode`, physically verified on v0.5.13 as `1=Optimized`, `0=Max performance` with downstream evidence `0/0/0/15` and `5/5/5/20` respectively.
- Never directly write `persist.vendor.sys.phs.cpufreq.boost`, `gpufreq.boost`, `frt.boost`, or `frt.min`.
- Save exact pre-TURBO NORMAL state before the first performance write and preserve it until restore verification succeeds.
- Persistent TURBO requires trustworthy Android thermal status. `SEVERE` or higher restores NORMAL, persists it, and requires manual re-arm.
- Boot reapply checks current thermal status before any performance write and never requests fresh ADB approval.
- Watchdog exists only while TURBO is active and never competes with NVIDIA/Android throttling.
- Android fixed-performance mode remains diagnostic-only.
- Preserve CLEAN START, brightness, APPS, animation undo, permanent signer and existing ADB security boundaries.

---

### Task 1: Lock the physical actuator PASS and add pure persistent state/controller tests

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboState.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboController.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/TurboControllerTest.kt`

**Interfaces:**
- Produces `TurboPhase`, `TurboSnapshot`, `TurboStateStore`, `TurboControlPort`, `TurboThermalPort`, `TurboClock`, `TurboOutcome`, and `TurboController`.
- `TurboControlPort` exposes only `readState(): ProcessorModeActuatorState` and `writeMode(mode: Int)`.

- [ ] **Step 1: Write failing controller tests**

```kotlin
@Test fun baselineIsPersistedBeforeFirstWrite()
@Test fun enableVerifiesMaxAndKeepsOriginalMode()
@Test fun partialEnableFailureRestoresOriginalMode()
@Test fun failedRestoreKeepsBaselineAndMarksRecoveryRequired()
@Test fun bootChecksThermalBeforeAnyWrite()
@Test fun severeThermalStatusRestoresNormalAndDisarmsTurbo()
@Test fun lowerThermalStatusLeavesTurboActive()
@Test fun corruptOrAmbiguousStateNeverReappliesTurbo()
```

Each fake port records call order so tests assert `save(ENABLING)` occurs before `writeMode(0)` and `readThermalStatus()` occurs before boot-time `writeMode(0)`.

- [ ] **Step 2: Run the unit suite and verify RED**

Run: `gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace`

Expected: new tests fail because `TurboController`/state types do not exist.

- [ ] **Step 3: Implement the minimal pure transaction engine**

```kotlin
enum class TurboPhase { NORMAL, ENABLING, TURBO_VERIFIED, RESTORING, RECOVERY_REQUIRED }

data class TurboSnapshot(
    val schema: Int = 1,
    val phase: TurboPhase = TurboPhase.NORMAL,
    val desiredTurbo: Boolean = false,
    val baselineMode: Int? = null,
    val appliedControls: Set<String> = emptySet(),
    val lastReason: String = "NORMAL",
    val lastChangeEpochMs: Long = 0L,
    val lastThermalStatus: Int? = null,
    val lastThermalFallbackEpochMs: Long? = null
)
```

`enable()`, `disable()`, `bootReapply()`, and `onThermalStatus()` use only the proven Optimized/Max state shapes from `ProcessorModeActuatorProof`. Any write/read-back failure restores the saved mode. A failed restore preserves the baseline and records `RECOVERY_REQUIRED`.

- [ ] **Step 4: Run tests and verify GREEN**

Run: `gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace`

Expected: all existing tests plus new controller tests pass.

- [ ] **Step 5: Commit**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboState.kt \
        shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboController.kt \
        shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/TurboControllerTest.kt
git commit -m "feat(shield-turbo): add transactional turbo state engine"
```

### Task 2: Add atomic persistence and the physically proven NVIDIA runtime port

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboStateCodec.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboStore.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/NvidiaTurboPort.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboRuntime.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/TurboStateCodecTest.kt`

**Interfaces:**
- `TurboStateCodec.encode(snapshot: TurboSnapshot): String` and `decode(raw: String): TurboSnapshot?`.
- `TurboStore(Context)` implements `TurboStateStore` by committing one encoded snapshot under one private preference key.
- `NvidiaTurboPort(Context)` implements `TurboControlPort` via `LocalBridge.withTrustedAdb` and only the existing `ProcessorModeActuatorPolicy` read commands plus `settings put system nv_power_mode 0|1`.
- `TurboRuntime(Context)` serializes controller calls process-wide and exposes `enable()`, `disable()`, `bootReapply()`, `thermal(status)` and `snapshot()`.

- [ ] **Step 1: Write codec tests first**

```kotlin
@Test fun roundTripPreservesBaselineReasonAndThermalFields()
@Test fun malformedStateDecodesToNull()
@Test fun unsupportedSchemaDecodesToNull()
```

- [ ] **Step 2: Verify RED**, then implement strict codec/store/port/runtime.

Run: `gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace`

- [ ] **Step 3: Verify GREEN** with the same command.

- [ ] **Step 4: Commit**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboStateCodec.kt \
        shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboStore.kt \
        shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/NvidiaTurboPort.kt \
        shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboRuntime.kt \
        shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/TurboStateCodecTest.kt
git commit -m "feat(shield-turbo): persist verified turbo transactions"
```

### Task 3: Add the foreground thermal watchdog and reboot reapply

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboThermalWatchdogService.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboBootReceiver.kt`
- Modify: `shield-turbo/app/src/main/AndroidManifest.xml`
- Create/modify: `shield-turbo/tests/test_persistent_turbo_contract.py`

**Interfaces:**
- `TurboThermalWatchdogService.start(context, bootReapply: Boolean = false)` and `stop(context)`.
- `TurboBootReceiver` starts the watchdog only when stored phase is `TURBO_VERIFIED` and desired TURBO remains true.

- [ ] **Step 1: Add failing source/security contracts** asserting a private boot receiver, private foreground service, `FOREGROUND_SERVICE` permission, trusted-ADB-only boot path, current thermal read before reapply write, SEVERE fallback, no vendor-property writes, no `setprop`, no root/voltage/thermal-disable strings, and unchanged CLEAN START source hashes/critical contracts.

Run: `python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v`

Expected: new persistent-TURBO contract tests fail because service/receiver/manifest declarations do not exist.

- [ ] **Step 2: Implement watchdog and boot receiver**

The service immediately calls `startForeground`, creates a low-importance notification channel, reads `PowerManager.currentThermalStatus`, registers a thermal listener on API 29+, delegates all state changes to `TurboRuntime`, and stops itself after verified NORMAL or any state that is not `TURBO_VERIFIED`. Boot action calls `bootReapply()`; it never requests new ADB approval.

- [ ] **Step 3: Verify source contracts and JVM tests**

Run:
```bash
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

- [ ] **Step 4: Commit**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboThermalWatchdogService.kt \
        shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/TurboBootReceiver.kt \
        shield-turbo/app/src/main/AndroidManifest.xml \
        shield-turbo/tests/test_persistent_turbo_contract.py
git commit -m "feat(shield-turbo): add thermal watchdog and boot reapply"
```

### Task 4: Add the remote-first TURBO MODE control and readouts

**Files:**
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Modify: `shield-turbo/tests/test_persistent_turbo_contract.py`

**Interfaces:**
- The TURBO page shows `TURBO MODE: OFF|ON`, processor mode, thermal state, watchdog state and last change.
- First enable confirmation states persistence, stock-controls-only behavior, active watchdog and SEVERE auto-NORMAL.

- [ ] **Step 1: Extend source contract tests first** for the exact remote-first control, confirmation copy, readouts, asynchronous worker execution, and D-pad focus order.

- [ ] **Step 2: Verify RED**, implement minimal UI, then verify GREEN.

Run:
```bash
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

- [ ] **Step 3: Commit**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt \
        shield-turbo/app/src/main/res/values/strings.xml \
        shield-turbo/tests/test_persistent_turbo_contract.py
git commit -m "feat(shield-turbo): expose persistent turbo mode UI"
```

### Task 5: Release candidate, CI verification, documentation and physical rollout

**Files:**
- Modify: `shield-turbo/app/build.gradle`
- Modify: `.github/workflows/shield-turbo.yml`
- Modify after CI evidence: `shield-turbo/SESSION_HANDOFF.md`, `shield-turbo/STATUS.md`, `shield-turbo/MEMORY.md`

- [ ] **Step 1: Bump one release version** and update CI package/version assertions.

- [ ] **Step 2: Run full CI**

Required gates: JVM tests, Python source/security contracts, lint, permanent signer, package/version/Leanback, APK ZIP integrity, nonvisual cold/warm emulator launch and no package fatal exception.

- [ ] **Step 3: Inspect CI evidence** and download the signed artifact only after the workflow completes successfully.

- [ ] **Step 4: Record physical v0.5.13 actuator PASS and the new candidate receipt** in handoff/status/memory. Do not call persistent TURBO physically accepted yet.

- [ ] **Step 5: Re-fetch live `shield-turbo-v01` and `main` heads before final documentation writes and verify no concurrent advance is overwritten.**

- [ ] **Step 6: Physical rollout**

On the real Shield: enable TURBO, verify processor mode reads Max; reboot and verify TURBO reappears without overwriting the original baseline; disable and verify exact original mode restoration. Exercise the SEVERE fallback with the injected/test path rather than intentionally overheating hardware.
