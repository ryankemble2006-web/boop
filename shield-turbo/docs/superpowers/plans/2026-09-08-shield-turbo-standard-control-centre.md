# SHIELD TURBO Standard Control Centre Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the physically proven brightness utility into a remote-first STANDARD-mode Shield control centre with TURBO, APPS, SHIELD, NETWORK and PICTURE surfaces while preserving brightness behaviour exactly.

**Architecture:** Keep `MainActivity` as the single Android TV entry activity but split system/app/network/display actions into small focused helpers. Build the home screen as large D-pad cards that swap a single content panel instead of creating a maze of activities. Existing probe classes remain the source of truthful diagnostics; new helpers only launch Android-owned settings/actions or expose read-only facts.

**Tech Stack:** Kotlin, Android SDK, programmatic Android TV UI, JUnit 4, Gradle, GitHub Actions, existing `boop-dev` signer.

**Spec:** `shield-turbo/docs/superpowers/specs/2026-09-08-shield-turbo-control-centre-design.md`

## Global Constraints

- Owner branch: `shield-turbo-v01`; package: `com.boop.shieldturbo`.
- Preserve the physically working brightness contract: 10–100%, 100% removes the overlay, below 100% uses the existing private non-exported service, no focus interception, persisted value, immediate undo at 100%.
- STANDARD mode must not claim root/shell authority, kill arbitrary apps, clear other-app private data/cache, overclock, alter governors, bypass thermal limits or manufacture performance scores.
- Main UI is landscape, chunky, D-pad first, plain English, with visible focus and Back returning one level before exit.
- Use Android-owned settings surfaces for app management/storage/system settings when ordinary app authority cannot perform the action directly.
- Keep permissions minimal; do not add microphone, camera, BOOP relay credentials or unrelated background receivers.
- Preserve established signing workflow and blocking Android lint errors.

---

### Task 1: Navigation shell and card model

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/ui/TurboSection.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Modify: `shield-turbo/app/src/main/res/values/ids.xml`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/TurboSectionTest.kt`

**Interfaces:**
- Produces: `enum class TurboSection { TURBO, PICTURE, APPS, NETWORK, SHIELD }`
- Produces: `TurboSection.fromKey(String): TurboSection?`
- `MainActivity` consumes the enum to render one section into a shared content container.

- [ ] **Step 1: Write the failing enum test**

```kotlin
@Test fun sectionKeysAreStable() {
    assertEquals(TurboSection.TURBO, TurboSection.fromKey("turbo"))
    assertEquals(TurboSection.PICTURE, TurboSection.fromKey("picture"))
    assertEquals(TurboSection.APPS, TurboSection.fromKey("apps"))
    assertEquals(TurboSection.NETWORK, TurboSection.fromKey("network"))
    assertEquals(TurboSection.SHIELD, TurboSection.fromKey("shield"))
    assertNull(TurboSection.fromKey("adb"))
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run from `shield-turbo/`: `./gradlew testDebugUnitTest --tests com.boop.shieldturbo.TurboSectionTest`
Expected: compilation failure because `TurboSection` does not exist.

- [ ] **Step 3: Implement the enum and replace the old top-row UI with five large focusable home cards plus one shared content panel**

Use stable string keys only for tests/state. Preserve the existing brightness controls by moving the same `SeekBar`, listener and `applyBrightness()` logic into `renderPicture()` without changing `Brightness.kt` or `BrightnessService.kt`.

- [ ] **Step 4: Add Back behaviour**

When a section is open, Back returns to the five-card home screen. Only Back from the home screen exits the activity. Ensure Left/Right/Up/Down focus ids cannot trap on the brightness SeekBar.

- [ ] **Step 5: Run unit tests and lint**

Run: `./gradlew testDebugUnitTest lintDebug`
Expected: all tests pass; lint has zero errors.

- [ ] **Step 6: Commit**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/ui/TurboSection.kt shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt shield-turbo/app/src/main/res/values/strings.xml shield-turbo/app/src/main/res/values/ids.xml shield-turbo/app/src/test/java/com/boop/shieldturbo/TurboSectionTest.kt
git commit -m "feat(shield-turbo): add control centre navigation"
```

### Task 2: TURBO snapshot and safe maintenance routes

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/system/SystemRoutes.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/system/QuickCheck.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/system/QuickCheckTest.kt`

**Interfaces:**
- Produces: `data class QuickCheckFinding(val level: Level, val message: String)` with `Level.OK`, `Level.WARNING`.
- Produces: `QuickCheck.storage(freeBytes: Long, totalBytes: Long): QuickCheckFinding`.
- Produces: `SystemRoutes.storage(context): Intent?` and `SystemRoutes.manageApps(context): Intent?` that return a resolvable Android-owned settings intent or `null`.

- [ ] **Step 1: Write failing storage-threshold tests**

```kotlin
@Test fun lowStorageWarnsBelowFivePercent() {
    assertEquals(Level.WARNING, QuickCheck.storage(4, 100).level)
}
@Test fun healthyStorageDoesNotPretendToBoost() {
    assertEquals(Level.OK, QuickCheck.storage(20, 100).level)
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew testDebugUnitTest --tests com.boop.shieldturbo.system.QuickCheckTest`
Expected: missing types.

- [ ] **Step 3: Implement QuickCheck with explicit storage thresholds and no synthetic performance score**

Use only concrete conditions. `totalBytes <= 0` returns an OK/unknown-style message rather than dividing by zero.

- [ ] **Step 4: Implement safe settings routes**

Try public `Settings` actions in descending specificity and check `resolveActivity(packageManager)` before returning. Never hard-code an NVIDIA component name unless later proven on hardware.

- [ ] **Step 5: Render TURBO**

Reuse `ShieldAnalyzer` with `DeviceProbe`, `MemoryProbe`, `StorageProbe`, `CpuProbe`, `ThermalProbe`, and `NetworkProbe`. Add large actions: `REFRESH`, `FREE SPACE`, `MANAGE APPS`, `RESTART TURBO`. `RESTART TURBO` recreates/relaunches only `com.boop.shieldturbo`.

- [ ] **Step 6: Run tests/lint and commit**

Run: `./gradlew testDebugUnitTest lintDebug`
Commit message: `feat(shield-turbo): add honest turbo tools`.

### Task 3: APPS picker and Android-owned app management

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/apps/LaunchableApp.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/apps/AppCatalog.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/apps/AppRoutes.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/apps/AppCatalogTest.kt`

**Interfaces:**
- Produces: `data class LaunchableApp(val packageName: String, val label: String)`.
- Produces: `AppCatalog.sortAndDedupe(List<LaunchableApp>): List<LaunchableApp>`.
- Produces: `AppRoutes.info(packageName: String): Intent` using `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` with `package:` URI.
- Runtime catalog uses `PackageManager` launch intents and excludes SHIELD TURBO itself.

- [ ] **Step 1: Write failing deterministic filtering/sorting tests**

```kotlin
@Test fun appsAreSortedCaseInsensitivelyAndDedupedByPackage() {
    val result = AppCatalog.sortAndDedupe(listOf(
        LaunchableApp("b", "Kodi"), LaunchableApp("a", "deezer"), LaunchableApp("b", "Kodi duplicate")
    ))
    assertEquals(listOf("a", "b"), result.map { it.packageName })
}
```

- [ ] **Step 2: Run focused test and verify RED**

- [ ] **Step 3: Implement catalog and routes**

Only show packages with a resolvable launch intent. Do not request QUERY_ALL_PACKAGES unless testing proves it is required for the TV launcher query; prefer `ACTION_MAIN` + `CATEGORY_LEANBACK_LAUNCHER` / `CATEGORY_LAUNCHER` queries.

- [ ] **Step 4: Render APPS as remote-friendly rows**

Centre opens a small action dialog containing `LAUNCH` and `APP INFO`. If Android exposes a separate storage-management intent for that package, show it; otherwise omit it. Do not add fake `FORCE STOP` or `CLEAR CACHE` buttons.

- [ ] **Step 5: Run all unit tests/lint and commit**

Commit message: `feat(shield-turbo): add remote app toolbox`.

### Task 4: SHIELD system shortcuts and device facts

**Files:**
- Extend: `shield-turbo/app/src/main/java/com/boop/shieldturbo/system/SystemRoutes.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/system/SystemShortcut.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/system/SystemShortcutTest.kt`

**Interfaces:**
- Produces: `enum class SystemShortcut { DISPLAY_SOUND, APPS, STORAGE, NETWORK, ACCESSIBILITY, DEVELOPER, ABOUT }`.
- Produces: ordered fallback action strings for each shortcut so route selection can be unit-tested without Android framework resolution.

- [ ] **Step 1: Write failing fallback-order tests**

Assert each shortcut has at least one public `Settings.ACTION_*` route and that unsupported routes fall through rather than crashing.

- [ ] **Step 2: Implement route candidates and runtime resolution**

Do not add sleep/reboot controls in STANDARD mode. Developer options is shown only when a resolvable settings intent exists.

- [ ] **Step 3: Render SHIELD**

Show model/build/uptime at top, then large shortcut buttons. When a route is unavailable, show a plain-English dialog: `This Shield firmware does not expose that shortcut to normal apps.`

- [ ] **Step 4: Run tests/lint and commit**

Commit message: `feat(shield-turbo): add shield settings toolbox`.

### Task 5: NETWORK card and on-demand reachability

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/ConnectivityCheck.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/probe/ConnectivityCheckTest.kt`

**Interfaces:**
- Produces: `enum class Reachability { LOCAL_ONLY, INTERNET_REACHABLE, OFFLINE, UNKNOWN }`.
- Produces a pure mapper from Android network-capability booleans to `Reachability`; runtime probe may use the existing `ACCESS_NETWORK_STATE` permission.

- [ ] **Step 1: Write failing mapper tests**

Cover offline, local-only, validated internet, and unknown states.

- [ ] **Step 2: Implement mapper and runtime collection**

Use `ConnectivityManager`/`NetworkCapabilities`; label validation as reachability, never as broadband speed or latency.

- [ ] **Step 3: Render NETWORK**

Show transport and current network facts from the existing `NetworkProbe`, plus a `CHECK CONNECTION` action that refreshes the reachability line.

- [ ] **Step 4: Run tests/lint and commit**

Commit message: `feat(shield-turbo): add network status tools`.

### Task 6: PICTURE information without touching proven dimming internals

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/picture/DisplayFacts.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/picture/DisplayFactsTest.kt`
- Preserve unchanged: `shield-turbo/app/src/main/java/com/boop/shieldturbo/Brightness.kt`
- Preserve unchanged: `shield-turbo/app/src/main/java/com/boop/shieldturbo/BrightnessService.kt`

**Interfaces:**
- Produces: `data class DisplayFacts(val width: Int, val height: Int, val refreshRateHz: Float, val hdrTypes: List<String>)`.
- Runtime collection reads active `Display.Mode` and HDR capabilities where available; missing values render as `Not exposed`.

- [ ] **Step 1: Write pure formatting tests**

Assert `3840x2160 @ 59.94 Hz` style output and stable `Not exposed` fallback.

- [ ] **Step 2: Implement facts collection/formatting**

No display setting writes in this task.

- [ ] **Step 3: Add read-only facts under the existing brightness control**

Do not alter brightness ranges, service actions, permission flow or persistence keys.

- [ ] **Step 4: Run brightness regression plus full tests/lint**

Run: `./gradlew testDebugUnitTest lintDebug`
Expected: existing `BrightnessTest`, `RegressionTest`, and all new tests pass; zero lint errors.

- [ ] **Step 5: Commit**

Commit message: `feat(shield-turbo): add picture diagnostics`.

### Task 7: CI smoke, version bump, documentation and signed candidate

**Files:**
- Modify: `shield-turbo/app/build.gradle`
- Modify: `.github/workflows/shield-turbo.yml` only if existing smoke navigation must be extended
- Modify: `shield-turbo/STATUS.md`
- Modify: `shield-turbo/SESSION_HANDOFF.md`
- Modify: `shield-turbo/MEMORY.md`

**Interfaces:**
- Produces a signed STANDARD control-centre candidate tied to one exact commit/run/artifact/checksum.
- Preserves package `com.boop.shieldturbo` and established signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

- [ ] **Step 1: Bump version to the next candidate version without changing package/signing identity**

Use one intentional version bump for the completed STANDARD control-centre slice.

- [ ] **Step 2: Extend emulator smoke to prove home-card focus, open/Back navigation and PICTURE focus without requiring unavailable ADB capabilities**

The smoke test must still install the release APK and exercise D-pad input.

- [ ] **Step 3: Run the complete verification locally/CI**

Required gates: unit tests, source safety contracts, lint with zero errors, signed release assembly, signer/package/archive checks, installed-release smoke.

- [ ] **Step 4: Fetch the completed GitHub Actions run and artifact**

Record exact source SHA, run ID, job ID, artifact ID, APK SHA-256 and artifact digest.

- [ ] **Step 5: Update handoff/status/memory**

Record Ryan's existing bedroom-Shield brightness success as physical evidence only for the brightness baseline. Mark the new STANDARD control-centre candidate CI/signer-green but hardware-pending until Ryan installs it.

- [ ] **Step 6: Verify live branch HEAD and deliver the exact APK for bedroom-Shield testing**

Do not call the STANDARD control centre physically accepted until remote navigation, app picker/routes, system shortcuts, network facts and brightness non-regression are tested on the real Shield.

---

## Separate follow-on plan

ADB TURBO is intentionally excluded from this implementation plan. After this STANDARD candidate is physically accepted, create a second plan for the ADB setup/detection framework and probe exactly one reversible elevated capability at a time. This separation protects the known-good brightness/control-centre rollback point from experimental privilege work.
