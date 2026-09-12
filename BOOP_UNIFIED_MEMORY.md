## BOOP defaults: isolated feature preparation, 2026-09-12

Own this work on `boop-shield-defaults`, at `.worktrees/boop-shield-defaults`.
Base is `582bd0d404a3d4ca61abd718551f7af5ef20aabe`, preserving accepted v135.
The product labels are **Use BOOP defaults** and **Undo BOOP defaults**; no personal
name in the feature. The prior read-only inspection is the source for the proposed
nine confirmed disables and separately recorded startup actions. Three older
unattributed/pre-existing disables and Kodi's old single restriction are excluded
from automatic inclusion. Details and exact IDs:
`docs/superpowers/specs/2026-09-12-boop-shield-defaults.md`.

This checkpoint creates/publishes the requested branch and documentation only.
Preset UI/controller implementation is not started. Baseline verification passed:
15 Startup Manager suites and Android 11 linkage. No app code, version, signing,
permissions, installed packages or Shield settings were changed. ADB was confirmed
connected. Keep the ongoing integration, lyrics and animation worktrees untouched.
Do not copy this device's Restore records to another user; preserve per-device and
pre-preset baselines. Do not install this branch by implication.

The inherited receipts below describe the accepted BASE, not a built preset.

# BOOP Unified continuation memory

## Ryan accepted Startup Manager v135, 2026-09-12

Ryan reported that the installed Startup Manager works perfectly after using it
to disable his unwanted packages, including the stock launcher, and prevent that
launcher's startup. This is user-reported physical acceptance of v135's Startup
Manager UI and the package controls he exercised. Preserve his selected setup.

Accepted source: `7408ab85c8c58b4478a3a64f18af834160efb346`; signed run
`34673227727`; artifact `10291770657`; APK SHA256
`b50823e9b902de049ee7e19d919947bcd2062d24bd953ad1616792bfa0485d8f`.
Use this exact v135 artifact as the accepted Startup Manager rollback reference.
The exact disabled-package inventory was not supplied. Do not infer a universal
Android/NVIDIA debloat recipe, measured performance gains, a completed Restore
round trip or reboot persistence from this report. Lyrics remains excluded.
This acceptance update is documentation only; the Shield is left untouched.

Updated 2026-09-12. Historical product/checkpoint detail is preserved at
[docs/history/startup-v133/BOOP_UNIFIED_MEMORY.md](docs/history/startup-v133/BOOP_UNIFIED_MEMORY.md).
Current user instructions and the owning handoff supersede its dated roadmap.

## Current scope and delivery

Ryan explicitly requires Startup Manager only; lyrics is WIP and excluded.
Do not merge, debug or deploy lyrics with this delivery. Its separate branch and
worktree stay intact. Existing approved music artwork is retained without editing
or regeneration. The old combined-v133 proposal is superseded.

Installed v135: `1.2.135-startup-manager-large-text`, source `7408ab85c8c58b4478a3a64f18af834160efb346`,
run `34673227727`, artifact `10291770657`. Installed APK bytes match the permanent-signed
artifact. The actual previous v134 APK was privately preserved before update.
The four private Startup Manager settings stores were byte-identical across the
update, preserving user package choices and exact recovery receipts.

## Protected engineering decisions

Unified remains `com.boop.alpha1`, with the permanent GitHub signer. Preserve
local-first control, profile routing, approved eyes/hands, audio behavior and all
protected rollback checkpoints. CI runs nonvisual functional/build/integrity tests;
Ryan owns visual and destructive real-device acceptance.

Capture exact original enabled/background/boot state before persistent changes.
Undo must repair partial writes even if successful-action flags were not recorded.
Reject unknown/mismatched state, preserve damaged receipts, bind shell commands to
the verified Android user and cancel stale callbacks. Protect only the minimum
recovery plumbing; the stock Google TV launcher remains manageable.

Java compilation did not prove Android 11 linkage: v128 failed on String.lines.
The repaired parsers passed the linkage gate and synthetic tests on the actual
Shield runtime. A live populated package screen was then observed in v134/v135.
Do not equate that with every disable/Restore/reboot path being physically accepted.

The Shield uses font_scale 1.3. v134's fixed text containers clipped some sidebar
and Background states. v135 uses single-line sidebar labels, intrinsic package
label/card heights and scaled-font-metric action heights. The actual v135 screen
confirmed the correction without reducing Android's font setting.

## Completion boundaries

Local v135 Android compilation and 277 tests passed; 15 Startup suites and linkage
passed. Signed CI passed including 68 Shield and 217 Unified focused tests. Scoped
manual v135 review saw 148 packages, readable actions and cyan focus; no current
BOOP-process fatal appeared in the inspected log. Ryan subsequently accepted
Startup Manager and the controls he exercised, as recorded above; unrelated
whole-app, Restore and reboot acceptance remain separate.

Ryan authorized installation and a music-track completion cue. At the final check
there was no active music track, so no skip was sent and no playback was started.
No destructive package changes, new grants, signing changes or automatic debloat
were performed by this review. See SESSION_HANDOFF.md for exact evidence.
