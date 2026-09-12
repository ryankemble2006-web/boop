# BOOP defaults implementation plan

> Execute inline with superpowers:executing-plans. Approval: go, 2026-09-12.

**Goal:** Apply the reviewed BOOP package choices with per-package exclusions and pre-preset Undo.
**Architecture:** Reuse the verified individual controller and local bridge for Apply. A separate durable preset journal saves receiving-device state plus the prior individual ledger before each mutation. Group Undo restores only dimensions the preset actually changed, preserves earlier individual records, and refuses newer-state conflicts. Keep journal in no-backup app-private storage.
**Tech stack:** Java 17 source, Android 11 compatible APIs, native remote-first Views, existing nonvisual GitHub build/signing.
**Spec:** docs/superpowers/specs/2026-09-12-boop-shield-defaults.md

## Constraints
Only boop-shield-defaults and its worktree may be changed. Use BOOP defaults and Undo BOOP defaults, never a personal name. Fourteen exact entries: nine disables, nine boot selections, six paired background restrictions. Exclude three historical disables and Kodi's pre-existing background restriction. Preserve artwork, signing, lyrics exclusion, current Shield installation and other worktrees. No new permissions or physical deployment.

## Files and execution

### 1. Frozen profile and durable journal
Create StartupDefaultsProfile.java, StartupDefaultsJournal.java and StartupDefaultsTest.java under unified/shield-home/src/{main,test}/java/com/boop/shieldhome. Create scripts/test-startup-defaults.py.
- [x] Test exact entry/action counts, excluded IDs, package-order stability and launcher-last.
- [x] Test empty/damaged/unknown-schema receipts, round trips, failed persistence and immutable original snapshots.
- [x] Implement profile entries and a versioned, bounded binary-text journal over StartupRestoreStore.Backend.
- [x] Observe RED then GREEN using `python scripts/test-startup-defaults.py`.

Profile interface: entries() -> List<Entry(packageName,label,disable,boot,background,consequence)>.
Journal interface: load() -> Batch or null; save(Batch); clear(). Entry includes before/after state, prior/current individual ledger, changed-dimension mask and interrupted flag. Batch includes frozen selection, global-switch ownership and pre-preset cleanup targets.

### 2. Verified preset coordinator
Create StartupDefaultsCoordinator.java; test through an in-memory Environment and a real StartupPackageController.
- [x] Preview is read-only; only exact installed and permitted entries are selected.
- [x] Apply compares preview state, records before writing, skips no-ops/missing/protected targets, rechecks safety and processes stock launcher last.
- [x] Repeated Apply cannot replace baseline or broaden selection; interrupted work keeps Undo.
- [x] Undo restores changed dimensions only; original individual ledger and pre-preset boot switch survive; newer state or ledger conflicts are left alone.
- [x] Verify partial app-op writes, ignored commands, storage failures, cancellation, process recreation and global-switch conflicts.

Environment supplies observe, inventory, controller, direct validated setters, individual ledger backend, cleanup targets/auto flag, and a per-step safety gate. Preview and Results are immutable; failures are explicitly reported. An interrupted write requires a fresh explicit Undo review, never automatic replay.

### 3. Android adapter and existing UI
Create AndroidStartupDefaultsBackend.java and StartupDefaultsScreen.java; modify ShieldStartupManagerActivity.java, ShieldStartupManagerView.java and StartupLocalBridge.java only at feature seams.
- [x] Add Overview buttons without replacing existing controls; use a scrollable review list with whole-row selection and large native buttons.
- [x] Connect preview/apply/undo through existing single worker and cancellation gate; show per-package outcomes without executing any action on opening a screen.
- [x] Check NVIDIA TV, current user, foreground, active input/accessibility, recovery packages and available BOOP HOME before launcher mutation.
- [x] Save the journal atomically under getNoBackupFilesDir; never copy device receipts into the preset.
- [x] Run existing Startup Manager suites, linkage check and Android compilation/full unit task.

### 4. Review, publish, signed candidate
- [x] Review actual diff, scoped data, cancellation/Undo boundaries; no visual CI.
- [x] Choose an unused forward version after checking sibling branches; keep the existing signer.
- [x] Add new behavioral suite to existing workflow; push only the feature branch; dispatch its signed build.
- [ ] Verify run source, package/version, archive and signer; update feature handoff/status/context/memory.
- [ ] Verify live branch HEAD after publication. Supply signed artifact. Do not install, merge or apply the defaults on the shared Shield without separate approval.

## Current execution receipt
Final local Android compilation and 277 unit tests passed, zero failures/errors/skips.
The three new suites include 27 coordinator and eight safety scenarios plus profile/
journal assertions. Existing 15 Startup suites, linkage and canonical core tests passed.
Candidate143 reserves the next observed free version above sibling142. The initial
UI preview is read-only; the new branch has never been installed or applied to the
shared Shield. Signed artifact verification and final published receipt remain next.
