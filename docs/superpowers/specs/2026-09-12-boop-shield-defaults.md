# BOOP Shield defaults

Status: isolated design and branch preparation; buttons are not implemented.
Date: 2026-09-12.
Branch/worktree: `boop-shield-defaults` / `.worktrees/boop-shield-defaults`.
Base: `boop-v125-animation-integration@582bd0d404a3d4ca61abd718551f7af5ef20aabe`,
with accepted v135 application source `7408ab85c8c58b4478a3a64f18af834160efb346`.

## Naming and scope

The feature is called **BOOP defaults**, never a person's defaults.
Overview actions: **Use BOOP defaults** and **Undo BOOP defaults**.
Keep the existing chunky remote-first UI and 130% text-size support.

The preceding inspection was explicitly read-only. This continuation creates
and publishes only the requested isolated branch/worktree plus its naming,
preset proposal and implementation handoff. App source remains the accepted
v135 baseline; no build, merge, install, grants or live package changes are made.
The separate lyrics and animation work is excluded and must remain untouched.

## Proposed first preset

Use a frozen, versioned exact-package list, not a vendor-prefix rule. The prior
read-only device inspection found 12 disabled packages but only nine changes
with a BOOP before-state confirming enabled/default -> disabled. The first
preset proposal contains those nine disables plus the explicit recorded startup
choices for the five still-enabled packages listed below. This is a device-derived
starting proposal, not universal safety certification for every Shield firmware.

`Yes` means apply that action. `Leave` means do not change that dimension;
in particular, do not re-enable packages or remove existing restrictions just
because an action was not selected in this preset. Entries without Disable stay
at their current enabled state. Background means both recorded background limits.

| Exact package | Disable | Close after boot | Background limits |
| --- | --- | --- | --- |
| `com.amazon.amazonvideo.livingroom.nvidia` | Yes | Yes | Yes |
| `com.android.printspooler` | Yes | Yes | Leave |
| `com.android.providers.contacts` | Yes | Yes | Leave |
| `com.google.android.tv` | Yes | Yes | Leave |
| `com.google.android.tvlauncher` | Yes | Yes | Leave |
| `com.google.android.tvrecommendations` | Yes | Yes | Yes |
| `com.nvidia.factory` | Yes | Leave | Leave |
| `com.nvidia.nvgamecast` | Yes | Leave | Yes |
| `com.nvidia.shield.nvcustomize` | Yes | Yes | Leave |
| `com.lonelycatgames.Xplore` | Leave | Yes | Leave |
| `org.xbmc.kodi` | Leave | Yes | Leave |
| `com.nvidia.ControllerMapper` | Leave | Leave | Yes |
| `flar2.homebutton` | Leave | Leave | Yes |
| `uk.local.eastenders` | Leave | Leave | Yes |

Proposed totals: 14 package entries, nine disables, nine boot-cleanup selections,
six paired background-limit actions. Missing packages are skipped; Android
rejecting a command is reported, not presented as success. The known legacy boot
list had ten entries because it also contained tegrazone3, excluded below.

### Not selected automatically

- `com.google.android.tungsten.setupwraith`: already disabled before its BOOP baseline.
- `com.nvidia.tegrazone3`: already disabled before its BOOP baseline; its recorded
  boot rule is also excluded from this first preset pending explicit inclusion.
- `com.nvidia.benchmarkblocker`: disabled but has no BOOP restore receipt.
- Kodi's single pre-existing `RUN_ANY_IN_BACKGROUND=ignore`: not a new BOOP
  background action, so leave the receiving device's value alone.

These items may be shown as unselected optional information; do not silently
infer consent to disable setup or unknown components on a different machine.
The package IDs are exact; do not label nvgamecast as a proven GameStream
relationship merely from its name. Do not copy private logs or raw preferences.

## Proposed flow

Use BOOP defaults -> review matching packages and effects -> Apply.
Show plain-English consequences, allow individual exclusions, and identify apps
that people may still want, such as Prime Video, Live Channels, controllers and
button remappers. Do not frame this as a guaranteed universally safe debloat list.
Reuse the existing local-ADB authorization. Never turn on ADB, change its access,
or grant Android permissions automatically. This user's ADB connection was seen
working during branch setup; another user's authorization is independent.

Before the stock launcher action, verify on the receiving device that BOOP Home
is available and recovery/input/local control are intact. Make that launcher
change last. Preserve the minimal recovery policy; allow missing or protected
entries to be skipped with an explicit result. No root or background kill loop.

## Transaction and Undo requirements

The preset carries only package IDs and requested actions, never this device's
private Restore files. Snapshot the receiving device before each write, persist
that recovery data first, and verify every applied action.

A grouped preset Undo must return only this preset's changes to their own
pre-preset state. It must preserve earlier individual BOOP baselines and custom
rules. Do not implement grouped Undo as blindly calling the existing global
Restore for every package: that could erase customisations predating the preset.

Capture/recover the auto-clean global switch as well, enabling it only when
boot-cleanup actions are actually applied. Do not clear unrelated boot targets.
Repeat Apply must not replace the original preset baseline or broaden selection.
Back/cancel, process death and partial Android rejection must keep an honest
partial receipt. Later manual changes need review rather than silent overwrite.
Recheck foreground/input/recovery safety during the operation, not only at preview.

## Likely implementation seam

Reuse StartupPackageController, StartupLocalBridge.PackageSession,
StartupRecoveryPolicy, StartupRestoreStore and StartupActionGate. Add an isolated
preset definition/coordinator and its own versioned batch receipt, then hook the
two Overview actions into the existing Activity/View. Avoid a new app/package,
shared-worktree edits or another Startup Manager redesign.

Nonvisual tests should prove exact-list matching, untouched dimensions, missing
and protected package handling, per-device baselines, prior-customisation-safe
Undo, repeated Apply, cancelled/partial operations, global-switch preservation,
launcher-last gating and failed verification. Existing 15 Startup Manager suites
and Android 11 linkage check passed on this unmodified branch baseline. New preset
behavior and UI have not yet been implemented or tested.

## Publication and device boundary

Publish this feature only to boop-shield-defaults. Do not push the ongoing
integration branch, merge lyrics, change signing or install an APK over the
shared Shield's active development build. Recheck live owning branches before
later edits/merge, choose the next free release version only for a real candidate,
and obtain deployment approval separately. The working v135 install stays intact.
