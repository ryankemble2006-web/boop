# SHIELD TURBO Clean Start Design

## Goal

Replace the misleading background-only startup restriction with a real, reversible CLEAN START system that can stop explicitly selected optional apps after Shield boot, verify the result, and leave them normally launchable afterward.

## Physical evidence driving this design

Ryan confirmed on a real Shield that v0.4.1's Startup Manager menu works and the Kodi forks still launch normally, but after reboot the four forks remain present in the Shield task manager. He reports a noticeable speed improvement after swiping/force-closing them. Therefore the existing RUN_IN_BACKGROUND/RUN_ANY_IN_BACKGROUND app-op writes have failed the acceptance requirement.

## Product behavior

### Startup Manager becomes a Clean Start manager

For each safe user-installed app, the action menu can:

- **ADD TO CLEAN START** / **REMOVE FROM CLEAN START**: manage membership in the post-boot cleanup group.
- **STOP + VERIFY NOW**: perform the exact stop operation immediately and report whether package processes are absent afterward.
- **UNDO OLD BACKGROUND RESTRICTION** when a v0.4.x StartupLedger record exists.
- **HARD BLOCK** remains separately confirmed and non-default.
- **LAUNCH APP NOW** and **ANDROID APP INFO** remain.

The parent page provides:

- **AUTO CLEAN AFTER REBOOT: ON/OFF**. It is off until the user explicitly enables it.
- **RUN CLEAN START NOW** for the whole selected group.
- a bounded **LAST CLEAN START** summary showing stopped, skipped, failed, or not-applied results.

The old **BLOCK STARTUP / KEEP LAUNCHABLE** action is not offered as a new fix. Existing records remain undoable so updates do not strand prior state.

### Real stop semantics

Use the existing authenticated loopback ADB shell and `am force-stop --user current <package>` for one selected safe user package at a time. Before and after the stop, gather a bounded process snapshot from `ps -A -o PID,NAME`; treat process names equal to the package or prefixed with `<package>:` as package processes. Re-check package enabled/stopped state. A successful result requires the package to remain enabled and no matching process names after force-stop.

`STOP + VERIFY NOW` does not clear data, caches, files, logins, uninstall, disable the package, or alter launcher availability. Manual launch after the stop is expected and releases Android's stopped state.

### Foreground protection

Before an automatic or group stop, obtain the current resumed package from ActivityManager diagnostics. If a target package is currently foreground/resumed, skip it and record **SKIPPED — IN USE**. The manual single-app STOP + VERIFY action is explicit and may stop its selected app after confirmation.

### Clean Start store

Add a private `CleanStartStore` independent of the existing `StartupLedger`. It stores:

- selected package-name set;
- auto-clean enabled flag;
- latest run summary and timestamp;
- no signing keys, raw shell dumps, device addresses, or credentials.

Package membership is validated on every use. System, updated-system, BOOP, NVIDIA/Android/Google-core exclusions already enforced by `PowerPolicy.safeUserPackage` remain hard gates for v0.5.0. This first CLEAN START release does not automatically classify or kill every installed package; the user selects optional targets. Broader essential/KEEP RUNNING classification is deferred until the selected-package primitive is physically proven.

### One-shot boot execution

Add `RECEIVE_BOOT_COMPLETED`, a non-exported `CleanStartBootReceiver`, and a non-exported `CleanStartJobService`.

On `BOOT_COMPLETED`, the receiver does nothing unless auto-clean is enabled and the selected group is non-empty. If enabled, it schedules one JobScheduler run with a short delay so core Shield services can settle.

The job:

1. re-validates every selected package as a safe user package;
2. attempts the existing local ADB identity **without permission prompting**;
3. skips any currently resumed target;
4. force-stops and verifies each remaining target independently;
5. writes a concise result summary;
6. finishes and leaves no resident worker.

ADB availability retries are bounded to at most three scheduled attempts for that boot. No permanent service, polling loop, repeating RAM sweeper, LAN target, server socket, or indefinite retry. If the existing key is not already authorised or port 5555 is unavailable, boot cleanup records **NOT APPLIED — ADB UNAVAILABLE** and exits/retries within the finite budget. It must never send the public key at boot and thereby provoke a new Android approval dialog.

### ADB transport change

Extend `AdbWire.connect`/`LocalBridge` with an explicit no-new-approval mode. Interactive ENABLE ADB TURBO keeps current behavior. Boot jobs may authenticate with the already trusted signature, but if ADB asks for a public key they fail immediately with a specific `AdbApprovalRequiredException` instead of sending it.

### What this does not promise

CLEAN START is post-boot cleanup, not interception before every optional app executes. An app can run briefly before the job fires. Deliberately opening a force-stopped app releases stopped state. Stock shell access is not treated as authority to disable arbitrary third-party boot receivers, and this design does not add root, re-sign third-party apps, device-owner provisioning, bootloader changes, or automatic HARD BLOCK.

## Safety and rollback

- v0.4.x `StartupLedger` format is preserved; existing old restrictions remain exactly undoable.
- CLEAN START group state is separate and deleting a package from the group causes no package mutation.
- Every destructive command rechecks package safety immediately before execution.
- Automatic cleanup never operates on a currently resumed target.
- Failure of one target does not widen scope to other apps or delete the saved group.
- Brightness and other previously accepted controls are unchanged.

## Testing

No GitHub visual confirmation, screenshot, UI hierarchy, golden/image/layout check, or appearance source-string certification.

Required nonvisual gates:

- pure command/process parser tests;
- no-prompt ADB authentication tests;
- CleanStartStore tests;
- boot scheduling/retry policy tests as pure logic where possible;
- source/security contract asserting receiver/service are non-exported, boot work is bounded, visual CI remains absent, and no broad query/root/data-clearing permissions are introduced;
- Android compilation/lint;
- existing permanent signer/package/archive checks;
- basic install/launch/no-fatal smoke only.

Physical acceptance belongs to Ryan: first one Kodi fork with STOP + VERIFY NOW, then add it to CLEAN START, enable auto-clean, reboot, verify task-manager absence and normal manual launch. Only then extend the group.

## Version

Target candidate: **v0.5.0 / versionCode 7**, package `com.boop.shieldturbo`, existing BOOP development signer only.
