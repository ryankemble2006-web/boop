# SHIELD TURBO Startup Manager Design

## Goal
Stop user-selected non-system apps such as multiple Kodi forks from waking themselves at Shield startup or freely running in the background, while keeping the normal app launch path available and preserving an exact reversible record of every Turbo change.

## User model
STARTUP MANAGER lives in ADVANCED / ADB TURBO. It lists only launchable, non-system apps by default. Each app has three states:

- ALLOW: Turbo has applied no startup/background restriction to that app.
- BLOCK STARTUP: Turbo applies Android's documented background-execution app-ops (`RUN_IN_BACKGROUND ignore` and `RUN_ANY_IN_BACKGROUND ignore`) through the already-approved local ADB shell. The package stays enabled and manually launchable.
- HARD BLOCK: Turbo disables the selected package for the current Android user with `pm disable-user`. This prevents launch as well as background execution and therefore requires an explicit confirmation.

The default recommendation is BLOCK STARTUP. HARD BLOCK is deliberately separate and never used automatically.

## Reversibility
Before Turbo changes an app for the first time it reads and stores the current background app-op modes and package enabled state in private app preferences. UNDO for one app restores those saved values. UNDO ALL restores every app recorded by Startup Manager and then clears only successfully-restored entries from the ledger. Turbo never assumes `allow` or `enabled` was the user's original state when an exact prior value can be read.

## Persistence
Android app-op and package enabled-state changes are persistent system state, so Turbo does not need a boot receiver or continuous background service merely to reapply them. Startup Manager remains user-triggered. This keeps the app from becoming another thing that starts at boot.

## Safety boundaries
- Exclude SHIELD TURBO itself, BOOP, system apps, updated-system apps, and NVIDIA/Android system packages by default.
- One chosen app per change. No bulk 'clean RAM' action.
- No clearing package data, caches, permissions, logins, or files.
- No CPU/GPU clocks, governors, root, bootloader, or package uninstall.
- Background restriction is described as reducing unwanted background/startup activity, never as a guaranteed RAM-speed score.
- Every disruptive HARD BLOCK action requires confirmation.
- Physical NVIDIA Shield testing is the acceptance authority. GitHub performs no visual checks, screenshots, UI hierarchy assertions, golden tests, or source-string appearance certification.

## Implementation
Use the existing `LocalBridge` / loopback ADB transport. A focused `StartupPolicy` builds and parses shell commands. `StartupLedger` stores original state. `StartupManagerActivity` presents the app list and actions using remote-focusable controls. PowerActivity gets a STARTUP MANAGER entry.

For BLOCK STARTUP, use:

`cmd appops set <package> RUN_IN_BACKGROUND ignore`
`cmd appops set <package> RUN_ANY_IN_BACKGROUND ignore`

Read current modes with `cmd appops get <package> RUN_IN_BACKGROUND` and `cmd appops get <package> RUN_ANY_IN_BACKGROUND` and verify after writes.

For HARD BLOCK, use `pm disable-user --user current <package>` and verify package enabled state. Restore with the saved previous state, preferring `pm enable --user current <package>` or `pm default-state --user current <package>` according to the saved value.

## First physical test
Ryan's four Kodi forks are the primary target. Mark one fork BLOCK STARTUP, reboot Shield manually, confirm that fork does not wake itself while it still launches normally from its icon. Repeat for remaining forks. HARD BLOCK is not required for that test.
