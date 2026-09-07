# BOOP Launcher Alpha 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace BOOP Launcher Alpha 1 UI with a clean, Pixel-like, pure-black launcher baseline on `boop-launcher-alpha2` while preserving `com.boop.launcher` and the existing BOOP release-signing identity.

**Architecture:** Rebuild the launcher module around small launcher-specific units instead of the Alpha 1 monolithic interaction flow. Use Android 16 Launcher3 behavior as the reference for workspace, all-apps, drag/drop, widgets, insets, and transitions, but keep the BOOP implementation minimal and free of Google proprietary code or Lawnchair code.

**Tech Stack:** Android SDK 36, Java 17, Android HOME intent, AppWidgetHost, PackageManager launcher activity enumeration, existing GitHub Actions release signing.

**Spec:** `docs/superpowers/specs/2026-09-07-boop-launcher-alpha2-design.md`

## Global Constraints

- Keep application ID `com.boop.launcher`.
- Keep existing BOOP GitHub release-signing secrets/key unchanged.
- No Internet or microphone permission for basic launcher operation.
- No copied Google Pixel Launcher proprietary code, resources, icons, trademarks, or bundled assets.
- Pure black edge-to-edge home; no permanent clock, At a Glance, dock, or search pill.
- App drawer search is on demand only.
- Physical feel on Pixel 10 Pro XL is the acceptance authority; CI is guardrail evidence only.

---

### Task 1: Replace Alpha 1 shell with Alpha 2 launcher state architecture

**Files:**
- Modify: `launcher/app/src/main/AndroidManifest.xml`
- Modify: `launcher/app/src/main/java/com/boop/launcher/MainActivity.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/LauncherState.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/EdgeToEdge.java`
- Test: `launcher/app/src/test/java/com/boop/launcher/LauncherStateTest.java`

**Interfaces:**
- Produces: `LauncherState` with `HOME`, `ALL_APPS`, `SEARCH`; transition helpers consumed by `MainActivity`.
- Produces: `EdgeToEdge.apply(Activity)` for transparent system bars and black background.

- [ ] Write tests for deterministic state transitions and Back behavior.
- [ ] Run unit tests and confirm failure before implementation.
- [ ] Implement state coordinator and edge-to-edge window setup.
- [ ] Remove Alpha 1 modal/editor startup behavior from `MainActivity` and render only a black root plus state containers.
- [ ] Run unit tests and lint.
- [ ] Commit.

### Task 2: Build installed-app model and Pixel-like all-apps drawer

**Files:**
- Create: `launcher/app/src/main/java/com/boop/launcher/AppEntry.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/AppRepository.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/AllAppsView.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/MainActivity.java`
- Test: `launcher/app/src/test/java/com/boop/launcher/AppRepositoryTest.java`

**Interfaces:**
- `AppRepository.loadLaunchableApps(PackageManager)` returns sorted `List<AppEntry>`.
- `AllAppsView.submit(List<AppEntry>)` renders native icons/labels on pure black.

- [ ] Write tests for sorting/filter inputs and stale package handling.
- [ ] Implement local launcher-activity enumeration using `ACTION_MAIN` + `CATEGORY_LAUNCHER`.
- [ ] Implement black all-apps surface with native icons and no permanent search bar.
- [ ] Add swipe-up/down state transitions with direct finger tracking and restrained settle animation.
- [ ] Run unit tests/lint.
- [ ] Commit.

### Task 3: Add on-demand local search

**Files:**
- Create: `launcher/app/src/main/java/com/boop/launcher/AppSearch.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/AllAppsView.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/MainActivity.java`
- Test: `launcher/app/src/test/java/com/boop/launcher/AppSearchTest.java`

**Interfaces:**
- `AppSearch.filter(List<AppEntry>, String)` returns local matches only.

- [ ] Write failing tests for case-insensitive prefix/contains matching and empty query.
- [ ] Implement search filter.
- [ ] Add contextual search UI that appears only when intentionally invoked and disappears when cleared/backed out.
- [ ] Verify drawer open has no permanent search furniture.
- [ ] Run tests/lint.
- [ ] Commit.

### Task 4: Implement workspace placement and persistence

**Files:**
- Create: `launcher/app/src/main/java/com/boop/launcher/WorkspaceItem.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/WorkspaceStore.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/WorkspaceView.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/MainActivity.java`
- Test: `launcher/app/src/test/java/com/boop/launcher/WorkspaceStoreTest.java`

**Interfaces:**
- `WorkspaceStore.load/save` persists placed app component, page, normalized position, and size.
- `WorkspaceView.addApp(AppEntry)` creates a placement on the current page.

- [ ] Write tests for persistence, clamping, stale app cleanup, and page creation/removal.
- [ ] Implement minimal local storage independent of Alpha 1 schema.
- [ ] Add long-press/tap action from drawer to pin an app.
- [ ] Render native icon and label on pure black workspace.
- [ ] Run tests/lint.
- [ ] Commit.

### Task 5: Implement continuous drag move/remove

**Files:**
- Create: `launcher/app/src/main/java/com/boop/launcher/DragController.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/WorkspaceView.java`
- Test: `launcher/app/src/test/java/com/boop/launcher/DragControllerTest.java`

**Interfaces:**
- `DragController` owns pointer stream DOWN→MOVE→UP/CANCEL and emits move/drop/remove outcomes.

- [ ] Write tests for pointer ownership, cancel, move, remove target, and overlap rejection.
- [ ] Implement drag controller without modal UI during active gesture.
- [ ] Show temporary non-modal remove target only during pickup.
- [ ] Persist successful move/remove and revert invalid drops.
- [ ] Run tests/lint.
- [ ] Commit.

### Task 6: Restore Android widget fundamentals

**Files:**
- Create: `launcher/app/src/main/java/com/boop/launcher/WidgetController.java`
- Create: `launcher/app/src/main/java/com/boop/launcher/LauncherAppWidgetHost.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/WorkspaceView.java`
- Modify: `launcher/app/src/main/java/com/boop/launcher/MainActivity.java`

**Interfaces:**
- `WidgetController.beginAddWidget()`, `handleBindResult()`, `handleConfigureResult()`, `releaseOrphanedId()`.

- [ ] Port only the proven AppWidgetHost lifecycle concepts needed for add/bind/config/cancel.
- [ ] Persist widget ID/page/position/size in the Alpha 2 workspace schema.
- [ ] Ensure canceled or removed providers release IDs and remove placeholders.
- [ ] Verify widget child touches remain normal when not dragging.
- [ ] Run lint/build.
- [ ] Commit.

### Task 7: Build, CI smoke checks, signed artifact, and handoff

**Files:**
- Modify: launcher CI smoke scripts only as needed for Alpha 2 state names/flows.
- Modify: `launcher/README.md`
- Modify: `SESSION_HANDOFF.md`
- Modify: `BOOP_STATUS.md`

**Interfaces:**
- Produces signed release APK from existing GitHub signing workflow.

- [ ] Update smoke checks for HOME startup, drawer open/close, local search, app launch, persistence, and drag continuity.
- [ ] Run unit tests, lint, release assemble, and smoke checks.
- [ ] Scan for AndroidRuntime crashes.
- [ ] Trigger/verify existing signed GitHub release workflow without changing key identity.
- [ ] Record exact commit, workflow run, artifact ID/hash, CI evidence, and physical-test checklist.
- [ ] Verify live GitHub branch HEAD matches the final commit.
