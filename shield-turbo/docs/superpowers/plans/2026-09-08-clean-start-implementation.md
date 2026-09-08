# SHIELD TURBO Clean Start Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add verified selected-app force-stop and bounded one-shot post-boot CLEAN START while preserving manual app launching, prior undo state, brightness, signer and nonvisual-only CI.

**Architecture:** Keep the existing interactive loopback ADB transport but add a no-new-approval authentication mode for boot use. A new `cleanstart` package owns pure policy/store/snapshot logic plus a non-exported boot receiver/job. StartupManagerActivity becomes the user surface for group membership and immediate stop/verify without changing the old StartupLedger format.

**Tech Stack:** Android/Kotlin/Java, JobScheduler, BroadcastReceiver, existing custom ADB wire protocol, SharedPreferences, JUnit 4, Python source/security contracts, GitHub Actions.

**Spec:** `shield-turbo/docs/superpowers/specs/2026-09-08-clean-start-design.md`

## Global Constraints

- Package remains `com.boop.shieldturbo`; target candidate is versionCode 7 / `0.5.0`.
- Use only the established secret-backed BOOP signer.
- No GitHub screenshots, UI hierarchy, golden/image/layout/appearance checks or visual certification.
- No root, device-owner, bootloader, third-party APK modification/re-signing, broad RAM cleaner, `pm clear`, uninstall or data/cache/login deletion.
- Automatic cleanup is selected safe user packages only, bounded, one-shot, foreground-protected and no-new-ADB-approval.
- Existing StartupLedger format and brightness implementation remain unchanged.

---

### Task 1: No-Prompt ADB Authentication

**Files:**
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/power/AdbWire.java`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/power/LocalBridge.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/power/PowerTransportTest.kt`

**Interfaces:**
- Produces: `AdbWire.connect(int, KeyPair, int, Runnable, boolean allowNewApproval)` and `LocalBridge.withTrustedAdb(operation)`.
- `withTrustedAdb` must never send AUTH public-key payload; approval-required handshake throws a dedicated IOException subtype/message.

- [ ] **Step 1: Write failing tests** proving interactive connect may send the public key but trusted/no-prompt connect rejects the second AUTH challenge before public-key send.
- [ ] **Step 2: Run `:app:testDebugUnitTest` and confirm RED** for the missing no-prompt API.
- [ ] **Step 3: Implement the smallest transport change**: retain the existing overload for interactive callers; add an explicit boolean path that throws `AdbApprovalRequiredException` instead of AUTH type 3 when approval would be required. Add `LocalBridge.withTrustedAdb` using the stored identity.
- [ ] **Step 4: Run unit tests and confirm GREEN.**
- [ ] **Step 5: Commit the focused transport change.**

### Task 2: Stop/Verify Policy and Process Parsing

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart/CleanStartPolicy.kt`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/power/LocalBridge.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/cleanstart/CleanStartPolicyTest.kt`

**Interfaces:**
- Produces: `CleanStartPolicy.forceStopCommand(packageName)`, `CleanStartPolicy.parseProcessNames(psOutput)`, `CleanStartPolicy.packageProcesses(packageName, names)`, `CleanStopResult` and `LocalBridge.stopAndVerify(adb, packageName)`.
- Verification requires no matching package or `package:` subprocess names and package still enabled.

- [ ] **Step 1: Write failing parser/command tests** for exact package, colon subprocess, unrelated names and malicious package strings.
- [ ] **Step 2: Run focused unit tests and confirm RED.**
- [ ] **Step 3: Implement command validation and parsers.** `LocalBridge.stopAndVerify` captures `ps -A -o PID,NAME`, executes `am force-stop --user current`, captures a second process list plus package enabled/stopped state, and returns a structured result rather than a hopeful string.
- [ ] **Step 4: Run full unit tests and confirm GREEN.**
- [ ] **Step 5: Commit the stop/verify primitive.**

### Task 3: Clean Start State Store and Foreground Protection

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart/CleanStartStore.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/cleanstart/CleanStartStoreTest.kt`
- Extend: `CleanStartPolicy.kt` + tests for resumed-package parsing.

**Interfaces:**
- Produces: `CleanStartStore.targets(): Set<String>`, `setTarget(packageName, enabled)`, `autoEnabled()`, `setAutoEnabled(Boolean)`, `recordSummary(CleanStartSummary)`, `lastSummary()`.
- Produces: `CleanStartPolicy.parseResumedPackage(dumpsysOutput): String?`.

- [ ] **Step 1: Write failing tests** for deterministic target persistence, add/remove, auto flag, summary serialization and resumed-package parsing.
- [ ] **Step 2: Verify RED.**
- [ ] **Step 3: Implement store with private SharedPreferences and strict package validation.** Do not put raw shell dumps in preferences.
- [ ] **Step 4: Run tests and verify GREEN.**
- [ ] **Step 5: Commit.**

### Task 4: Startup Manager CLEAN START Actions

**Files:**
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/startup/StartupManagerActivity.kt`
- Create/modify functional source contracts under `shield-turbo/tests/`.

**Interfaces:**
- Consumes Tasks 1-3.
- Produces user actions: ADD/REMOVE CLEAN START, STOP + VERIFY NOW, RUN CLEAN START NOW, AUTO CLEAN AFTER REBOOT toggle, LAST CLEAN START summary, old ledger Undo, existing Hard Block/Launch/App Info.

- [ ] **Step 1: Add a failing nonvisual source/API contract** asserting the old `BLOCK STARTUP / KEEP LAUNCHABLE` is not offered as a new action, Clean Start group controls exist, old ledger Undo remains, and no message/list AlertDialog conflict returns.
- [ ] **Step 2: Run Python contracts and confirm RED.**
- [ ] **Step 3: Implement the activity changes.** Recheck `PowerPolicy.safeUserPackage` immediately before any stop. Manual STOP + VERIFY confirms one selected app. Group run skips current resumed target and reports per-app outcomes.
- [ ] **Step 4: Run JVM and Python gates; confirm GREEN.**
- [ ] **Step 5: Commit.**

### Task 5: Bounded Boot Receiver and Job

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart/CleanStartBootReceiver.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart/CleanStartJobService.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart/CleanStartScheduler.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/cleanstart/CleanStartSchedulerTest.kt`
- Modify: `shield-turbo/app/src/main/AndroidManifest.xml`
- Modify: `shield-turbo/tests/test_contract.py`

**Interfaces:**
- `CleanStartScheduler.schedule(context, attempt)` supports attempts 0..2 only and uses finite delay/backoff.
- Receiver schedules only on BOOT_COMPLETED when auto enabled and targets non-empty.
- Job uses `withTrustedAdb`, never interactive approval; it revalidates packages, skips resumed target, stops/verifies selected packages, stores summary, retries only ADB-unavailable cases within the attempt budget, then finishes.

- [ ] **Step 1: Write failing scheduler/policy tests and manifest/source contracts** for `RECEIVE_BOOT_COMPLETED`, non-exported receiver/service, max three attempts, no foreground service, no periodic job, no visual checks.
- [ ] **Step 2: Run tests/contracts and confirm RED.**
- [ ] **Step 3: Implement receiver/job/scheduler and manifest entries.** No network scan, no public-key approval at boot, no continuous worker.
- [ ] **Step 4: Run full JVM/contracts/lint and confirm GREEN.**
- [ ] **Step 5: Commit.**

### Task 6: Version, Release Verification, Documentation and Artifact

**Files:**
- Modify: `shield-turbo/app/build.gradle`
- Modify: `.github/workflows/shield-turbo.yml` only for v0.5.0/code 7 receipt checks; retain nonvisual smoke.
- Update: `shield-turbo/SESSION_HANDOFF.md`, `STATUS.md`, `MEMORY.md` after the exact built source is known.

- [ ] **Step 1: Bump to versionCode 7 / `0.5.0` and update package-version workflow assertions.**
- [ ] **Step 2: Fetch/check live `shield-turbo-v01` and `main` before publication; preserve any concurrent advance.**
- [ ] **Step 3: Run the full GitHub build gate:** JVM tests, source/security contracts, lint, established signer release, package/version/non-debuggable/archive checks, artifact upload, basic nonvisual install/launch/no-fatal smoke. No visual/UI hierarchy checks.
- [ ] **Step 4: Download the signed artifact, extract exactly one APK, verify APK SHA-256 against CI receipt and inspect signer/package/version receipts.**
- [ ] **Step 5: Update handoff/status/memory with exact source/run/job/artifact/APK digests plus the physical-test boundary. Commit documentation only after built source, then verify live branch HEAD and unchanged/advanced main separately.**
- [ ] **Step 6: Deliver the actual APK. Physical acceptance procedure:** one Kodi fork -> STOP + VERIFY NOW -> add to CLEAN START -> enable AUTO -> reboot -> confirm absent from Shield task manager -> manually launch normally. Do not claim CLEAN START physically accepted before Ryan reports this sequence.

## Self-Review

- Spec coverage: no-prompt ADB, real force-stop verification, selected group, foreground skip, bounded boot work, old undo preservation, nonvisual CI, signer/version/artifact and physical boundary are each mapped to a task.
- Placeholder scan: no TBD/TODO or undefined implementation handwaves remain.
- Type consistency: `withTrustedAdb`, `stopAndVerify`, `CleanStartStore`, `CleanStartPolicy.parseResumedPackage`, scheduler attempt budget and v0.5.0/code 7 are used consistently across tasks.
