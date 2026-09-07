# SHIELD TURBO v0.1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an independently installable Android TV SHIELD TURBO v0.1 APK that truthfully analyses Shield capabilities and telemetry before any optimisation actions are introduced.

**Architecture:** A self-contained `shield-turbo/` Gradle project with a TV activity, isolated probe classes, immutable analysis models and a privilege detector. The analyzer aggregates probe results; the UI only renders evidence and capability state. A dedicated GitHub Actions workflow builds/tests/signs this app without altering BOOP runtime or the unified APK.

**Tech Stack:** Kotlin, Android SDK, Android TV/Leanback manifest integration, JUnit, AndroidX test, Gradle, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-07-shield-turbo-design.md`

## Global Constraints

- Source lives under `shield-turbo/` and remains independent of `com.boop.alpha1`.
- Package identity: `com.boop.shieldturbo`.
- v0.1 is analysis/telemetry only; no overclocking, governor changes, arbitrary process killing or other-app private-data clearing.
- Capability states are `STANDARD`, `ADB_TURBO`, `ROOT`.
- Probe statuses are `AVAILABLE`, `RESTRICTED`, `UNSUPPORTED`, `ERROR`.
- D-pad plus centre/Enter must operate the v0.1 UI.
- Missing sysfs/kernel data is evidence, not a fatal error.
- Signing material stays in GitHub secrets/workflows; never commit keys or credentials.
- BOOP unified source, package, permissions and runtime behavior are not modified.
- CI green and physical Shield acceptance remain separate states.

---

### Task 1: Independent Android TV shell and truthful result model

**Files:**
- Create: `shield-turbo/settings.gradle.kts`
- Create: `shield-turbo/build.gradle.kts`
- Create: `shield-turbo/app/build.gradle.kts`
- Create: `shield-turbo/app/src/main/AndroidManifest.xml`
- Create: `shield-turbo/app/src/main/res/values/strings.xml`
- Create: `shield-turbo/app/src/main/res/values/themes.xml`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/model/ProbeResult.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/model/ProbeResultTest.kt`

**Interfaces:**
- Produces: `enum class ProbeStatus { AVAILABLE, RESTRICTED, UNSUPPORTED, ERROR }`
- Produces: `data class ProbeResult(val key: String, val label: String, val status: ProbeStatus, val value: String, val evidence: String)`

- [ ] **Step 1: Write the failing model test**

```kotlin
@Test fun restrictedResultRetainsEvidence() {
    val result = ProbeResult("thermal", "Thermal", ProbeStatus.RESTRICTED, "Restricted", "/sys source unreadable")
    assertEquals(ProbeStatus.RESTRICTED, result.status)
    assertEquals("/sys source unreadable", result.evidence)
}
```

- [ ] **Step 2: Run the focused test and verify the model is absent**

Run: `cd shield-turbo && ./gradlew testDebugUnitTest --tests '*ProbeResultTest'`
Expected: FAIL because the model does not exist yet.

- [ ] **Step 3: Add the minimal Gradle Android application, TV launcher manifest, theme/resources and exact model types above**

The manifest declares `android.software.leanback` required, touchscreen not required, and `MainActivity` with `LEANBACK_LAUNCHER`. Set `applicationId = "com.boop.shieldturbo"`, `minSdk = 28`, `targetSdk = 35`, `compileSdk = 35`.

- [ ] **Step 4: Run unit tests and assemble a debug APK**

Run: `cd shield-turbo && ./gradlew testDebugUnitTest assembleDebug`
Expected: PASS and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 5: Commit the independently buildable shell**

```bash
git add shield-turbo
git commit -m "feat(shield-turbo): add Android TV application shell"
```

### Task 2: Probe contract and analyzer aggregation

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/Probe.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/analysis/AnalysisSnapshot.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/analysis/ShieldAnalyzer.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/analysis/ShieldAnalyzerTest.kt`

**Interfaces:**
- Consumes: `ProbeResult` from Task 1.
- Produces: `fun interface Probe { fun read(): ProbeResult }`
- Produces: `data class AnalysisSnapshot(val capturedAtMillis: Long, val results: List<ProbeResult>)`
- Produces: `class ShieldAnalyzer(private val probes: List<Probe>, private val clock: () -> Long) { fun analyze(): AnalysisSnapshot }`

- [ ] **Step 1: Write a failing aggregation test using two fake probes and a fixed clock**

```kotlin
@Test fun analyzerKeepsEveryProbeResult() {
    val first = Probe { ProbeResult("a", "A", ProbeStatus.AVAILABLE, "1", "ok") }
    val second = Probe { ProbeResult("b", "B", ProbeStatus.UNSUPPORTED, "Unavailable", "not exposed") }
    val snapshot = ShieldAnalyzer(listOf(first, second)) { 123L }.analyze()
    assertEquals(123L, snapshot.capturedAtMillis)
    assertEquals(listOf("a", "b"), snapshot.results.map { it.key })
}
```

- [ ] **Step 2: Run the focused test**

Run: `cd shield-turbo && ./gradlew testDebugUnitTest --tests '*ShieldAnalyzerTest'`
Expected: FAIL because analyzer contracts are absent.

- [ ] **Step 3: Implement the exact interfaces above; analyzer calls each probe once and preserves result order**

- [ ] **Step 4: Run all unit tests**

Run: `cd shield-turbo && ./gradlew testDebugUnitTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shield-turbo/app/src
git commit -m "feat(shield-turbo): add capability analyzer"
```

### Task 3: Device, memory and storage probes

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/DeviceProbe.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/MemoryProbe.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/StorageProbe.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/probe/BasicProbeFormattingTest.kt`

**Interfaces:**
- Produces probe keys `device`, `memory`, `storage`.
- `DeviceProbe` reports manufacturer/model/device and Android release/API.
- `MemoryProbe` consumes an injectable memory reader returning total/available bytes.
- `StorageProbe` consumes an injectable storage reader returning total/available bytes.

- [ ] **Step 1: Write failing formatting tests that assert byte counts render as human-readable GiB values and device evidence is non-empty**
- [ ] **Step 2: Run `./gradlew testDebugUnitTest --tests '*BasicProbeFormattingTest'` and verify failure**
- [ ] **Step 3: Implement Android-backed readers plus injectable constructors for deterministic tests; map successful reads to `AVAILABLE` and exceptions to `ERROR` with short evidence**
- [ ] **Step 4: Run all unit tests and `assembleDebug`; expect PASS**
- [ ] **Step 5: Commit with `feat(shield-turbo): add device memory and storage probes`**

### Task 4: CPU and thermal capability probes

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/FileSourceReader.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/CpuProbe.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/ThermalProbe.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/probe/SysfsProbeTest.kt`

**Interfaces:**
- Produces probe keys `cpu`, `thermal`.
- `FileSourceReader.readFirst(paths: List<String>): SourceRead` returns readable text plus path, or a classified unreadable/missing result.
- CPU/thermal probes convert missing sources to `UNSUPPORTED`, permission-denied sources to `RESTRICTED`, malformed readable sources to `ERROR`, and valid readings to `AVAILABLE`.

- [ ] **Step 1: Write failing tests for all four status mappings using a fake `FileSourceReader`**
- [ ] **Step 2: Run `./gradlew testDebugUnitTest --tests '*SysfsProbeTest'`; expect FAIL**
- [ ] **Step 3: Implement safe read-only source probing; never write sysfs and never require root**
- [ ] **Step 4: Run all unit tests; expect PASS**
- [ ] **Step 5: Commit with `feat(shield-turbo): add CPU and thermal capability probes`**

### Task 5: Network probe and privilege detector

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/probe/NetworkProbe.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/privilege/PrivilegeTier.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/privilege/PrivilegeDetector.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/privilege/PrivilegeDetectorTest.kt`

**Interfaces:**
- Produces: `enum class PrivilegeTier { STANDARD, ADB_TURBO, ROOT }`.
- `PrivilegeDetector.detect()` uses explicit evidence providers. Root evidence outranks ADB-granted evidence; ADB-granted evidence outranks standard.
- Network probe reports active transport (`Ethernet`, `Wi-Fi`, or other exposed transport) and connectivity facts available without secrets.

- [ ] **Step 1: Write failing privilege precedence tests for standard, ADB-granted and root evidence**
- [ ] **Step 2: Run the focused test; expect FAIL**
- [ ] **Step 3: Implement detector and read-only network probe. Do not execute `su`, open an ADB socket or store a Shield address**
- [ ] **Step 4: Run all unit tests; expect PASS**
- [ ] **Step 5: Commit with `feat(shield-turbo): detect network and privilege capabilities`**

### Task 6: TV dashboard and ANALYSE SHIELD flow

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Create: `shield-turbo/app/src/main/res/layout/activity_main.xml`
- Create: `shield-turbo/app/src/main/res/drawable/focus_panel.xml`
- Create: `shield-turbo/app/src/androidTest/java/com/boop/shieldturbo/MainActivityTest.kt`

**Interfaces:**
- Consumes: `ShieldAnalyzer`, all v0.1 probes and `PrivilegeDetector`.
- Primary button text is exactly `ANALYSE SHIELD`.
- Result cards render label, value/status and concise evidence; restricted/unsupported results remain visible.

- [ ] **Step 1: Write an Android test asserting launch, initial focus on `ANALYSE SHIELD`, centre activation, and visible result container**
- [ ] **Step 2: Run `./gradlew connectedDebugAndroidTest`; expect the new test to fail before UI implementation**
- [ ] **Step 3: Implement a black TV-first screen with large text, strong focus drawable, vertically navigable result cards and no touch-only control**
- [ ] **Step 4: Run unit tests, lint, assembleDebug and Android test on the available emulator; expect PASS**
- [ ] **Step 5: Commit with `feat(shield-turbo): add TV analysis dashboard`**

### Task 7: Dedicated CI, established secret-backed signing and APK artifact

**Files:**
- Create: `.github/workflows/shield-turbo.yml`
- Create: `shield-turbo/README.md`

**Interfaces:**
- Workflow path filter includes `shield-turbo/**` and its workflow file.
- Artifact name: `SHIELD-TURBO`.
- Release package validation expects `com.boop.shieldturbo`.

- [ ] **Step 1: Inspect existing BOOP workflows and identify the established signing secret names and decode/sign pattern without reading or exposing secret values**
- [ ] **Step 2: Add a workflow that checks out, sets up the matching JDK/Android environment, runs `testDebugUnitTest`, lint, assembles release, uses the existing secret-backed signer where compatible, validates package identity and uploads `SHIELD-TURBO`**
- [ ] **Step 3: Add README text describing STANDARD/ADB TURBO/ROOT as capability states and explicitly stating v0.1 performs analysis rather than performance mutation**
- [ ] **Step 4: Push the task branch and inspect the actual GitHub Actions run. If it fails, use job steps/logs to repair only evidenced failures and rerun**
- [ ] **Step 5: When CI is green, record exact commit, workflow run, artifact ID and verification boundary; do not call it physically accepted until tested on a real Shield**

### Task 8: Final isolation and safety verification

**Files:**
- Verify: `shield-turbo/**`
- Verify: `.github/workflows/shield-turbo.yml`
- Verify unchanged BOOP runtime paths by commit comparison

**Interfaces:**
- Produces a reviewable v0.1 checkpoint with no BOOP runtime modifications.

- [ ] **Step 1: Compare the implementation branch against its base and confirm application-code changes are confined to `shield-turbo/**` plus the dedicated workflow/docs**
- [ ] **Step 2: Search the diff for private keys, tokens, passwords, device addresses and signing blobs; expect none**
- [ ] **Step 3: Verify package identity in the built APK is `com.boop.shieldturbo` and the TV launcher activity resolves**
- [ ] **Step 4: Verify CI remains green at the exact final head after any documentation update**
- [ ] **Step 5: Commit/push final reviewed documentation and report exact branch/commit/run/artifact with status `CI/signer green; physical Shield test pending`**
