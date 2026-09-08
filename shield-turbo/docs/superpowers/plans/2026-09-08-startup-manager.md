# SHIELD TURBO Startup Manager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a reversible ADB-backed Startup Manager that prevents chosen non-system apps such as Kodi forks from freely running in the background at boot while keeping the normal launch path available.

**Architecture:** Keep the existing loopback ADB transport. Add a pure command/state policy with unit tests, a private ledger for exact rollback, and one remote-friendly Startup Manager activity reachable from ADVANCED. Background block uses Android app-ops; hard block is separate and confirmed.

**Tech Stack:** Android/Kotlin, existing Java ADB transport, SharedPreferences ledger, GitHub Actions unit/source/lint/signing checks.

**Spec:** `shield-turbo/docs/superpowers/specs/2026-09-08-startup-manager-design.md`

## Global Constraints

- Package remains `com.boop.shieldturbo` and uses the established signer.
- No visual CI, screenshots, UI hierarchy assertions, golden tests, or appearance certification.
- Exclude system/updated-system apps, Turbo itself and BOOP from default target list.
- No data/cache clearing, uninstall, root, clocks/governors, bulk RAM cleaner, or automatic hard block.
- Every change is reversible from recorded prior state.

---

### Task 1: Startup policy and state parsing

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/startup/StartupPolicy.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/startup/StartupPolicyTest.kt`

**Interfaces:**
- Produces `StartupPolicy.backgroundBlock(packageName)`, `backgroundRestore(packageName, runMode, runAnyMode)`, `hardBlock(packageName)`, and package/mode parsers.

- [ ] Write tests proving package-name validation, app-op command construction, parsing of `allow/ignore/deny/default`, and hard-block separation.
- [ ] Run unit tests and verify RED because `StartupPolicy` does not exist.
- [ ] Implement the smallest pure policy needed to make them green.
- [ ] Re-run unit tests.

### Task 2: Exact undo ledger

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/startup/StartupLedger.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/startup/StartupLedgerTest.kt`

**Interfaces:**
- Stores `OriginalStartupState(packageName, runInBackground, runAnyInBackground, enabledState)` before the first Turbo mutation and exposes per-app/all restore records.

- [ ] Write tests proving first-write wins, later changes do not overwrite original state, successful removal is per app, and malformed records fail closed.
- [ ] Verify RED.
- [ ] Implement private SharedPreferences-backed ledger with serialization limited to validated package/mode/state values.
- [ ] Verify GREEN.

### Task 3: LocalBridge startup operations

**Files:**
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/power/LocalBridge.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/startup/StartupCommandsTest.kt`

**Interfaces:**
- Produces read/apply/verify methods using the existing loopback ADB session.

- [ ] Write command-level tests for reading both app-ops, applying BLOCK STARTUP, restoring exact prior modes, hard-blocking one package, and restoring package state.
- [ ] Verify RED.
- [ ] Implement operations through `checked()` only; verify read-back after writes.
- [ ] Verify GREEN.

### Task 4: Remote-friendly Startup Manager

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/startup/StartupManagerActivity.kt`
- Modify: `shield-turbo/app/src/main/AndroidManifest.xml`
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/power/PowerActivity.kt`
- Modify: `shield-turbo/tests/test_contract.py`

**Interfaces:**
- ADVANCED exposes `STARTUP MANAGER`.
- Startup Manager lists safe user apps and actions `ALLOW`, `BLOCK STARTUP`, `HARD BLOCK`, `UNDO`, `UNDO ALL`.

- [ ] Add non-visual source/security contracts requiring an explicit Startup Manager activity, no boot receiver/service, no QUERY_ALL_PACKAGES, no clear/uninstall commands, and no visual CI hooks.
- [ ] Verify RED.
- [ ] Build the activity with D-pad-focusable app rows and explicit confirmation for HARD BLOCK.
- [ ] Preserve normal launchability for BLOCK STARTUP.
- [ ] Verify contracts and unit tests.

### Task 5: Version, signing, handoff and artifact

**Files:**
- Modify: `shield-turbo/app/build.gradle`
- Modify: `.github/workflows/shield-turbo.yml`
- Modify: `shield-turbo/SESSION_HANDOFF.md`
- Modify: `shield-turbo/STATUS.md`
- Modify: `shield-turbo/MEMORY.md`

- [ ] Bump version/code and workflow assertions.
- [ ] Run full unit tests, source contracts and lint.
- [ ] Build with the established BOOP signer and verify package/version/signer/archive.
- [ ] Allow only basic nonvisual install/launch/no-fatal smoke.
- [ ] Download and checksum the signed APK.
- [ ] Record exact source/run/artifact/checksum and physical-acceptance boundary in handoff/status/memory.
- [ ] Verify live `shield-turbo-v01` and `main` heads before delivery.
