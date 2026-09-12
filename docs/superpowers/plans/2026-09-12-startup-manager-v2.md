# Startup Manager v2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace BOOP's current Startup Manager scroll wall with a remote-first package control console that can disable, re-enable, force-stop, boot-close, background-block and exactly restore installed Shield packages.

**Architecture:** Keep all work inside the existing Unified Shield Home module. Introduce one package-state model and repository, one immutable original-state restore ledger, one protected recovery-floor policy, and one action bridge that verifies every mutation. The new UI consumes that model and reuses the existing authenticated local bridge, boot cleanup scheduler and background app-op prevention path.

**Tech Stack:** Java 17, Android API 30 Shield runtime / targetSdk 36, SharedPreferences for private package-state receipts, existing authenticated local ADB bridge, JUnit 4 focused unit tests, existing GitHub stable-signing workflow.

**Spec:** `docs/superpowers/specs/2026-09-12-startup-manager-v2-design.md`

## Global Constraints

- Preserve the approved dark/charcoal BOOP mockup direction with cyan focus chrome and large remote-first controls.
- No GitHub visual tests, screenshots, golden images or source-string appearance guards.
- Google TV Launcher `com.google.android.tvlauncher` must be manageable, including true Disable and Restore.
- Protection is a tiny recovery floor, not a broad Google/NVIDIA/system-app exclusion.
- Persistent changes must capture original state before mutation and verify state after mutation.
- Disable must never uninstall or clear app data.
- Existing `Clean after boot` and background-prevention semantics must migrate into the unified model without losing their existing Undo information.
- Up/Down moves rows, Left/Right moves row actions, Back returns one level, Menu opens Quick Restore, and Startup Manager must not compete with the existing 250 ms global Back-hold gesture.
- Preserve protected BOOP checkpoints and unrelated concurrent work.
- Physical Shield acceptance remains Ryan-owned.

---

## File structure

New focused units:

- `StartupPackageState.java`: immutable current package snapshot consumed by UI/policy.
- `StartupPackageRepository.java`: enumerates installed packages and resolves labels/system/launcher/enabled/background state.
- `StartupRecoveryPolicy.java`: decides Protected/Low/Medium/High impact and explains protection.
- `StartupRestoreRecord.java`: immutable pre-change baseline plus BOOP-managed actions.
- `StartupRestoreStore.java`: private persisted exact-restore ledger.
- `StartupPackageController.java`: performs verified disable/re-enable/force-stop/boot-clean/background-block/restore operations through `StartupLocalBridge` and existing stores.
- `StartupManagerNav.java`: pure D-pad focus-state transitions used by the view.

Existing files retained and adapted:

- `StartupLocalBridge.java`: add verified package enable/disable and package-state probe commands.
- `StartupCleanupPolicy.java` / `StartupCleanupStore.java`: remove broad system exclusion for v2 package listing while retaining boot-clean execution safety.
- `StartupPreventionStore.java`: expose existing exact app-op baseline to the unified controller.
- `ShieldStartupManagerActivity.java`: orchestrate repository/controller and screen routing.
- `ShieldStartupManagerView.java`: replace scroll-wall UI with Overview, Package Control and Restore surfaces.
- `ShieldHomeSettingsView.java`: keep one Startup Manager entry point.

---

### Task 1: Unified package snapshot and recovery-floor policy

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageState.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupRecoveryPolicy.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/StartupRecoveryPolicyTest.java`

**Interfaces:**
- Produces `StartupPackageState` record with packageName, label, systemApp, launcher, enabledState, backgroundModes, and BOOP-managed flags.
- Produces `StartupRecoveryPolicy.assess(StartupPackageState, RecoveryCapabilities)` returning impact and optional protection reason.

- [ ] **Step 1: Write failing policy tests**

Cover: BOOP package protected; resolved Settings/package-installer/input/local-bridge packages protected; Google TV Launcher not protected and High impact; ordinary system recommendation package visible/manageable; normal user app Low/Medium; no vendor-prefix blanket protection.

- [ ] **Step 2: Run focused test and verify RED**

Run the Shield Home JUnit harness used by existing Startup tests with `StartupRecoveryPolicyTest` included. Expected: compile/test failure because the new types do not exist.

- [ ] **Step 3: Implement minimal snapshot and policy types**

Use enums `Impact { LOW, MEDIUM, HIGH, PROTECTED }` and `ManagedAction { DISABLED, BOOT_CLEAN, BACKGROUND_BLOCK }`. `RecoveryCapabilities` contains the package names currently resolving BOOP itself, Android Settings, package installer, input method/IME path if exposed, and the authenticated local-control bridge owner. Exact package fallbacks are allowed only for capabilities Android cannot resolve.

- [ ] **Step 4: Re-run focused tests and verify GREEN**

Expected: all new policy cases pass.

- [ ] **Step 5: Commit**

`git commit -m "feat: define Startup Manager package safety model"`

---

### Task 2: Enumerate all installed packages into one repository

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageRepository.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/StartupPackageRepositoryTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldStartupManagerActivity.java`

**Interfaces:**
- Consumes `StartupPackageState` and `StartupRecoveryPolicy`.
- Produces `List<StartupPackageState> load()` sorted label-first with disabled/system/non-launcher packages included.

- [ ] **Step 1: Write failing repository tests**

Use a fake package source to prove the repository includes: enabled user app, disabled user app, system launcher, non-launcher system package, and a package with no Leanback activity. Verify labels fall back to package name and sorting is stable.

- [ ] **Step 2: Run and verify RED**

Expected: missing repository/fake-source interfaces.

- [ ] **Step 3: Implement repository with a narrow Android adapter**

Create an internal package-source interface so pure tests do not require PackageManager. Android adapter queries installed applications with disabled components included, separately resolves TV/launcher intents, reads application enabled state and folds existing cleanup/prevention selections into each snapshot.

- [ ] **Step 4: Run focused tests and verify GREEN**

Expected: all package classes appear regardless of launcher/system/disabled status.

- [ ] **Step 5: Commit**

`git commit -m "feat: enumerate all Shield packages for Startup Manager"`

---

### Task 3: Exact restore ledger

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupRestoreRecord.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupRestoreStore.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/StartupRestoreStoreTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupPreventionStore.java`

**Interfaces:**
- `StartupRestoreStore.captureIfAbsent(packageName, baseline)` freezes the first baseline.
- `StartupRestoreStore.updateManagedActions(packageName, Set<ManagedAction>, lastAppliedState)` updates BOOP intent without overwriting baseline.
- `StartupRestoreStore.remove(packageName)` only after verified complete restore.

- [ ] **Step 1: Write failing ledger tests**

Prove first baseline is immutable across later changes; disabled state and both background app-op modes round-trip; boot-clean and prevention actions coexist; outside-BOOP drift is detected from last-applied state; receipt survives recreation; removal only happens after explicit success.

- [ ] **Step 2: Run and verify RED**

Expected: restore record/store unavailable.

- [ ] **Step 3: Implement JSON/private-preference encoding**

Persist only package name, original enabled state, original app-op modes, managed-action set, last applied state, first-changed timestamp and schema version. Reuse the current prevention record data when present rather than taking a new baseline.

- [ ] **Step 4: Add migration test from existing prevention records**

Given an existing `StartupPreventionRecord`, importing into v2 must preserve its original app-op modes exactly and must not delete the old record until v2 commit succeeds.

- [ ] **Step 5: Run focused tests and verify GREEN**

- [ ] **Step 6: Commit**

`git commit -m "feat: add exact Startup Manager restore ledger"`

---

### Task 4: Verified package actions through the local bridge

**Files:**
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupLocalBridge.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageController.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/StartupPackageControllerTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupCleanupPolicy.java`

**Interfaces:**
- Controller methods: `disable`, `reenable`, `forceStop`, `setBootClean`, `setBackgroundBlock`, `restore`.
- Every persistent method returns a verified result object with success flag, plain-English summary and current `StartupPackageState`.

- [ ] **Step 1: Write failing controller tests**

Fake bridge must prove command order: capture baseline -> mutate -> fresh state probe -> receipt update. Cover `pm disable-user --user current`, `pm enable --user current`, `am force-stop --user current`, existing app-op prevention path, boot-clean selection, rejected operation, verification mismatch, protected package rejection, and Google TV Launcher allowed.

- [ ] **Step 2: Run and verify RED**

- [ ] **Step 3: Extend bridge with safe command builders and probes**

Package names must pass strict package-name validation. Disable/re-enable operations probe current user enabled state after command. Force stop verifies process absence but does not create a persistent restore action. Existing bridge authorization and cancellation semantics remain unchanged.

- [ ] **Step 4: Implement controller orchestration**

Controller consults `StartupRecoveryPolicy`, captures baseline once, invokes bridge/store, verifies, and only then records success. On failure it leaves the baseline/receipt in a retryable truthful state.

- [ ] **Step 5: Run focused tests and verify GREEN**

- [ ] **Step 6: Commit**

`git commit -m "feat: add verified Startup Manager package actions"`

---

### Task 5: Migrate v1 boot-clean and prevention state

**Files:**
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupCleanupStore.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupPreventionStore.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupManagerMigration.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/StartupManagerMigrationTest.java`

**Interfaces:**
- `StartupManagerMigration.runOnce()` imports v1 selections/records into v2 and writes a migration marker only after verification.

- [ ] **Step 1: Write failing migration tests**

Cover: boot-clean-only target; prevention-only package with exact original app-op modes; package in both systems; stock launcher boot-clean target; failed import leaves v1 data untouched; second run is idempotent.

- [ ] **Step 2: Run and verify RED**

- [ ] **Step 3: Implement two-phase migration**

Read v1, build prospective v2 records, persist/verify v2, then mark migrated. Keep v1 stores readable until the physical migration/restore test passes; do not delete old data in this task.

- [ ] **Step 4: Remove v1 max-five UI constraint from the unified selection model**

Keep execution bounded per package operation, but allow unlimited selected boot-clean packages as specified.

- [ ] **Step 5: Run tests and verify GREEN**

- [ ] **Step 6: Commit**

`git commit -m "feat: migrate Startup Manager v1 state into v2"`

---

### Task 6: Pure remote navigation state machine

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/StartupManagerNav.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/StartupManagerNavTest.java`

**Interfaces:**
- Input: screen, filter index, focused package id, action index, visible package ids.
- Output: next focus target for Up/Down/Left/Right/OK/Back/Menu and list mutations.

- [ ] **Step 1: Write failing navigation tests**

Cover: Up/Down stays in package column; Left/Right cycles actions without leaving row; edges clamp rather than fall into unrelated controls; filtering preserves same package when visible; disabling/restoring a row preserves focus or nearest neighbor; Back returns one level; Menu routes to Restore; empty lists focus a stable empty-state Back action.

- [ ] **Step 2: Run and verify RED**

- [ ] **Step 3: Implement pure navigation reducer**

No Android View dependencies. Do not implement the global 250 ms Back hold here.

- [ ] **Step 4: Run tests and verify GREEN**

- [ ] **Step 5: Commit**

`git commit -m "feat: define Startup Manager remote navigation"`

---

### Task 7: Replace the scroll-wall UI with the approved v2 surfaces

**Files:**
- Rewrite in scope: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldStartupManagerView.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldStartupManagerActivity.java`
- Modify if needed for entry copy only: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java`
- Test: existing/new behavior tests only; no visual automation.

**Interfaces:**
- Consumes repository/controller/navigation model from Tasks 1-6.
- Exposes Overview, Package Control, confirmation and Restore screens matching the approved mockup direction.

- [ ] **Step 1: Add nonvisual activity/view contract tests**

Assert callbacks exist for open Package Control, open Boot Cleanup filter, open Background filter, open Restore, package action execution, filter change, Back and Menu/Quick Restore. Assert package action buttons are actual focusable controls with stable IDs/tags suitable for manual device inspection, without asserting pixels/colors/layout geometry.

- [ ] **Step 2: Run and verify RED**

- [ ] **Step 3: Implement Overview**

Large cards: Disable apps, Clean after boot, Prevent background start, Restore changes; managed count and protected-core status; cyan/charcoal styling through existing `FocusChrome` primitives.

- [ ] **Step 4: Implement Package Control**

Top filters All/Launchers/Ads/System/User/Disabled, package rows, current state/impact tag, row actions Disable or Re-enable, Boot close, Block bg, Force stop, and a detail pane where TV width permits. All package classes remain visible.

- [ ] **Step 5: Implement confirmations and Restore**

High-impact disable requires confirmation. Successful mutations automatically create receipts. Restore screen supports one or multi-select batch restore and retains failed rows for retry.

- [ ] **Step 6: Wire `StartupManagerNav` to D-pad behavior**

Use explicit next-focus requests/listeners where Android default focus search is unreliable. Do not intercept long-Back. Menu routes to Quick Restore.

- [ ] **Step 7: Run focused behavior tests and full local nonvisual unit suite**

Expected: all existing Startup Manager tests plus new v2 tests pass; unrelated Shield Home tests remain green.

- [ ] **Step 8: Commit**

`git commit -m "feat: redesign Startup Manager package control UI"`

---

### Task 8: Version, CI, signed build and physical Shield acceptance

**Files:**
- Modify: `unified/app-build.gradle`
- Modify: `SESSION_HANDOFF.md`
- Modify: `BOOP_STATUS.md`
- Modify: `BOOP_MEMORY.txt` only for durable accepted behavior/results.

**Interfaces:**
- Produces the first signed Startup Manager v2 candidate and physical acceptance receipt.

- [ ] **Step 1: Bump one candidate version**

Use the next free `versionCode` after the live branch version and a descriptive `versionName` such as `startup-manager-v2`.

- [ ] **Step 2: Run materialization and full nonvisual local gate**

Count tests/failures/errors/skips. Restore any known materializer-generated unrelated files before staging.

- [ ] **Step 3: Commit and push only reviewed Startup Manager v2 files**

Fetch live branch again immediately before push; reconcile any concurrent EastEnders or other task commits by fast-forward/rebase without overwriting them.

- [ ] **Step 4: Run existing stable-signing GitHub Unified workflow**

Require success for package/version, permanent signer, archive integrity, focused Shield tests and artifact upload.

- [ ] **Step 5: Preserve installed rollback APK privately before installation**

Verify its version and SHA locally. Do not publish rollback APKs.

- [ ] **Step 6: Install only after Ryan's explicit install approval**

After installation verify version and open Startup Manager without granting any new permissions automatically.

- [ ] **Step 7: Physical acceptance sequence**

On Shield, Ryan/agent jointly verify: remote navigation; disable/restore a harmless package; Boot close one package then reboot once; background-block one package then manually launch it; disable Google TV Launcher and verify BOOP Home remains usable; Restore Google TV Launcher; batch Restore at least two BOOP-managed records. Record each result separately and never turn CI green into physical acceptance.

- [ ] **Step 8: Publish receipt**

Update handoff/status with exact source SHA, workflow run, artifact/hash, test counts and scoped physical results. Verify local HEAD equals live GitHub branch HEAD.

---

## Completion gate

Do not remove the legacy v1 persisted stores until migration is CI-green **and** at least one migrated package has been physically restored on the Shield. Do not disable arbitrary packages during automated tests. Google TV Launcher disable/restore is a deliberate physical acceptance step only after the signed candidate is installed and Ryan is present.