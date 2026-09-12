# BOOP Startup Manager v2 Design

**Status:** Implemented candidate; v128 physical compatibility failure is repaired in the continuation. Final signed UI/device acceptance is pending.
**Target:** BOOP Unified on Nvidia Shield TV.
**Visual direction:** approved dark/charcoal BOOP mockup with cyan focus chrome, large remote-first cards, Package Control list, detail pane, confirmation flow and Restore screen.

## Goal

Replace the current long Startup Manager scroll view with one remote-first package control console that lets Ryan inspect and control installed packages, including system packages, launchers, recommendation/ad components and launcher dependencies.

Startup Manager v2 combines five package actions in one place: Disable, Re-enable, Force stop, Clean after boot and Prevent background start. BOOP records the original state before every persistent change and provides exact Restore rather than guessing Android defaults.

## Product principles

- The Shield belongs to the user. High-impact packages are warned about, not hidden merely because they are system packages.
- BOOP protects only itself and a minimal recovery floor needed to undo mistakes locally.
- Google TV Launcher and related Google/NVIDIA recommendation or advertising packages are ordinary manageable packages, not special exclusions.
- Plain-English impact warnings replace vague danger dialogs.
- Every persistent mutation has a visible Undo/Restore route.
- Existing Clean after boot and Prevent background start behavior remains available, but both are represented as actions on the same package record.
- Remote navigation must be deterministic and require no touch input.
- Ryan remains the visual acceptance authority. No automated visual/golden-image tests are added.

## Package model

Startup Manager reads the installed-package inventory and creates one `PackageControlRecord` per package. Each record exposes:

- package name and user-facing label;
- system/user classification;
- enabled/disabled state;
- running/stopped evidence where available;
- whether BOOP manages background-start prevention;
- whether BOOP manages one-shot boot cleanup;
- BOOP restore history;
- impact classification: Low, Medium, High impact or Protected;
- optional relationship hints such as launcher, recommendation, advertising, input, settings or package-management capability.

The inventory must include packages without launcher activities. The current launcher-only candidate scan is insufficient because the user explicitly wants background dependencies and companion packages visible.

Filters are `All`, `Launchers`, `Ads / recommendations`, `System`, `User`, and `Disabled`. Filtering never changes the underlying package state.

## Minimal protected recovery floor

The protected floor is intentionally narrow. Protected packages remain visible but Disable, Force stop, Boot close and Background block actions are unavailable where the action would compromise recovery.

Always protect `com.boop.alpha1`. At runtime also protect the currently resolved Android System UI, Settings/recovery surface and package-installer/package-management components required to re-enable an app. Do not protect packages merely because their name starts with `com.google`, `com.android` or `com.nvidia`.

The protection decision is capability-based where Android exposes a resolver, with exact package fallback only when the platform has no resolver. The UI explains why a protected package is protected. There is no broad vendor-prefix allowlist.

Google TV Launcher (`com.google.android.tvlauncher`) is explicitly **not** protected.

## Action semantics

### Disable app

BOOP captures the package's current enabled state and relevant background app-op state before mutation. It then disables the package for the current Android user using the authenticated local bridge, verifies the resulting package state, and records a restore entry only after verification succeeds.

Disable does not uninstall or clear app data. If Android rejects the operation, BOOP reports that plainly and leaves no false restore receipt.

### Re-enable

Re-enable is available for a package currently disabled by Android, whether BOOP disabled it or not. If BOOP has an original-state receipt, Restore is preferred because it restores all BOOP-managed state. Plain Re-enable only changes enabled state and does not erase unrelated restore history.

### Force stop

Force stop is immediate and non-persistent. It stops the selected package for the current user and verifies process/stopped-state evidence. It does not add boot cleanup or prevention rules.

### Clean after boot

This retains the existing bounded post-boot behavior: selected packages may start normally, then BOOP performs one verified cleanup pass. It does not repeatedly kill packages the user launches later.

The old maximum-five policy is removed for v2. The user may select any number of eligible packages because package selection is now part of the unified model rather than a tiny fallback list.

### Prevent background start

This retains the existing app-op-based prevention behavior and exact original-state restore ledger. Manual foreground launch remains allowed where Android permits it. System packages are no longer rejected solely because they are system packages; only the protected recovery floor is excluded.

## Exact Restore ledger

Persistent actions are transactional. Before the first BOOP-managed change to a package, BOOP records an immutable baseline containing the observed enabled state and the explicit/effective background app-op modes used by Startup Manager.

Subsequent BOOP changes update the managed-action set but do not overwrite the original baseline. Restore returns the package to that captured baseline, verifies each restored field, then removes the receipt only when the complete restore succeeds.

Restore supports one package or a selected batch. The Restore screen shows package label, package name, BOOP-managed actions and when BOOP first changed it. A package changed outside BOOP is marked `Changed outside BOOP`; Restore still uses the recorded baseline but shows a confirmation because the current state no longer matches BOOP's last applied state.

There is no fake migration from historical Shield Turbo ledgers. Existing Startup Manager v1 selections/receipts are migrated only when their exact persisted format can be read and verified. Otherwise v1 remains intact until the user explicitly recreates the rule in v2.

## Relationship and impact hints

BOOP may display relationship hints derived from Android package metadata, intent resolution, shared UID/process information and package naming only as advisory context. It must not claim a dependency is proven merely because package names are similar.

Impact levels affect confirmation copy, not authorization:

- **Low:** ordinary user/companion package with no identified system role.
- **Medium:** background service, recommendation, advertising or companion package whose removal may change a feature.
- **High impact:** launcher, media provider, core vendor surface or package participating in a currently resolved role.
- **Protected:** minimal recovery-floor package whose destructive actions BOOP will not expose.

High-impact Disable always uses a confirmation dialog. Low/Medium actions can execute directly from the package action strip after the normal local-bridge authorization exists.

## UI structure

Startup Manager v2 uses the approved three-part information architecture from the mockup.

### Overview

Large cards: `Disable apps`, `Clean after boot`, `Prevent background start`, and `Restore changes`. A small status card reports protected-core status and number of managed packages. This screen replaces the current all-in-one vertical scroll wall.

### Package Control

The left/main list shows one package per row with label, package name, state/impact badge and compact action strip. The focused row uses the existing BOOP cyan focus treatment. A detail pane shows current state, impact explanation, relationship hints and the complete action list for the selected package.

The list is virtual/scrollable and must remain usable with hundreds of packages. Package icons are optional decoration; missing icons must never break navigation or state display.

### Confirmation

High-impact Disable opens a compact confirmation surface with plain-English consequence text and `Disable` / `Cancel`. BOOP automatically creates the Restore receipt as part of a successful mutation; the user never has to remember to add one manually.

### Restore

Restore is a dedicated screen showing every BOOP-managed package. Rows are multi-selectable for batch restore, while OK on one row exposes a single-package restore action and details.

## Remote navigation contract

- `Up/Down`: move between package rows or dashboard cards.
- `Left/Right`: move between actions for the focused package without accidentally leaving the row.
- `OK`: execute a low/medium action or open confirmation/details where required.
- `Back`: return exactly one level and preserve prior list position/filter.
- `Menu`: open Quick Restore from anywhere inside Startup Manager v2.
- Holding Back continues to use the existing 250 ms BOOP Shield-settings gesture where that global launcher behavior applies; Startup Manager must not create a competing long-press handler.

Focus must never disappear when a row is disabled, filtered out, restored or removed from a filtered result. After a state-changing action, focus stays on the same package if still visible; otherwise it moves to the nearest remaining row.

## Architecture

Startup Manager v2 separates package inventory, policy, mutation and presentation:

1. **Package inventory** enumerates installed packages and current state without changing anything.
2. **Package policy** calculates protection and impact labels from explicit rules and resolved Android capabilities.
3. **Package controller** performs one requested action through `StartupLocalBridge`, then verifies the result before reporting success.
4. **Restore store** owns immutable pre-change baselines and BOOP-managed action records.
5. **Startup rules** reuse the existing boot-cleanup and background-prevention mechanisms through the unified record model.
6. **TV presentation** owns filtering, focus, action routing and confirmation surfaces but contains no shell-command construction.

No package-control logic is duplicated in view classes. Shell/ADB command creation stays behind the bridge/policy boundary so command validation and result verification remain testable without a TV UI.

## Local bridge and authorization

The existing private local-ADB identity/authorization path remains the mutation authority. Startup Manager does not request root, add a cloud service or embed a privileged daemon.

Every package name passed to the bridge must come from the PackageManager inventory or a persisted verified receipt. Free-form package strings are never executed directly.

Long-running or failed bridge calls must not freeze D-pad navigation. The current action row becomes temporarily busy while the operation runs; Back cancels pending authorization/work where the existing bridge supports cancellation.

## Errors and truthful status

Success is shown only after fresh Android state verifies the requested result. Failure copy uses plain English, for example `Android kept this app enabled` or `Could not stop this package`, with technical detail kept out of the primary UI.

A partial Restore is not labelled Done. The restore receipt remains until every recorded field is verified restored. Batch restore reports which packages succeeded and leaves failed rows selected for retry.

## Migration from Startup Manager v1

The existing v1 stores remain readable during migration. v2 imports:

- current Clean after boot selections;
- Auto-clean enabled state;
- current background-prevention managed packages;
- exact original background app-op receipts where present.

Migration never fabricates a baseline. If a v1 record is incomplete, v2 shows the package as currently managed by legacy Startup Manager and offers `Adopt into v2`, which captures current state as a new baseline only after explicit user action.

The v1 UI can be removed only after migration tests prove the persisted-data path and a physical Shield test proves Restore for at least one migrated package.

## Testing and acceptance

Automated tests are nonvisual and behavior-focused. They cover package classification, protected-floor resolution, command validation, disable/re-enable verification, Force stop verification, boot-cleanup selection, background-prevention capture/restore, migration, batch Restore, and focus-state transitions as pure navigation logic where practical.

No screenshot, golden-image, source-string appearance or automated visual certification is added. Ryan performs visual and remote acceptance on the physical Shield.

Physical acceptance is staged so recovery stays obvious:

1. Navigate the new screens using only the SofaBaton/Shield remote and confirm no focus traps.
2. Disable and restore one harmless test/user package.
3. Apply and remove background prevention on one harmless package.
4. Add and remove one package from Clean after boot.
5. Disable Google TV Launcher, verify BOOP Home remains usable, then restore it from BOOP.
6. Only after the launcher round-trip passes, inspect/disable optional launcher companion or recommendation packages one at a time.

The implementation is not physically accepted merely because CI passes.

## Explicit non-goals for v2

- No automatic debloat recipe that disables a predefined vendor list.
- No root requirement.
- No uninstall, data clearing or filesystem deletion.
- No process-performance or NVIDIA processor/headroom tuning.
- No claim that guessed package relationships are dependencies.
- No automatic disabling of companion packages when another package is disabled.
- No background polling loop repeatedly killing apps.
- No visual redesign of BOOP Home outside the Startup Manager entry and required return/focus behavior.

## Done criteria

Startup Manager v2 is complete when the approved remote-first UI is implemented; all installed packages are visible through Package Control; Google TV Launcher can be disabled and restored; existing boot cleanup and prevention actions work through the unified model; persistent changes have verified exact Restore; the minimal recovery floor cannot be disabled from BOOP; nonvisual tests and signed CI pass; and Ryan physically accepts navigation plus the launcher disable/restore round trip.
