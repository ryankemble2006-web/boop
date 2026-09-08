# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; independent package `com.boop.shieldturbo`.

## Current signed candidate

**v0.4.1 / versionCode 6 is built, signed and machine-verified. It repairs the Startup Manager action-menu blocker. Physical Shield acceptance remains pending.**

| Evidence | Receipt |
| --- | --- |
| Built source | `0961153e5dea94c38027cdf31530f500c5b29573` |
| Actions run / job | `34204102153` / `101989443983`: success |
| JVM tests | 58 passed, 0 failures/errors/skips, verified from JUnit XML |
| Source/API/security checks | 13 checks; successful gate |
| Android lint | 0 errors, 22 warnings, verified from lint XML |
| Signed artifact | `SHIELD-TURBO`, ID `10047107169`, ZIP 744535 bytes |
| Artifact ZIP SHA-256 | `4a1f31abe78e7876414c3524d98701d96ec9fbf3a2c46eb27280ab97eb8c6ee5` |
| Test artifact | `10047154897`, SHA-256 `f98ef2b3092f56cf3baabb24576d984d95fe7bcb0055a08da3f3e482bff4e752` |
| Delivered APK | `Shield-Turbo-v0.4.1.apk`, 2283246 bytes |
| APK SHA-256 | `c7bc147a70378dfe62a14e542aeb5dcb1036fbe6818551e98ddcf6fe061793fa` |
| Permanent signer certificate SHA-256 | `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` |
| Release checks | Expected package/version/Leanback/non-debuggable, cryptographic signer and archive integrity passed |
| Post-upload smoke | Basic install, cold/warm launch, process and no-fatal checks passed |
| Visual tests | None. Ryan is the real-device visual/remote acceptance authority |

The actual extracted APK was linked only after its existence, digest, archive integrity and build receipts were checked. It was made available before the slower nonvisual smoke completed.

## What was wrong

Ryan could select an app in v0.4.0 but only saw its package name and the explanatory message. `StartupManagerActivity.choose` incorrectly combined AlertDialog `setMessage` and `setItems`; the message displaced the action list. A test-first API-use regression reproduced this exact mistake at `47abd6136448ee7ef6771729584d6471569f6c9f`, run `34203516856`. That test now passes.

## Patch scope

The per-app dialog now uses the title and action list without the conflicting message. Label lookup tries a real launcher/application label before the package ID; it never invents names when metadata is absent. Cancel bypasses the busy guard, and remote Back cancels an in-flight operation. These are control-flow/data-resolution changes, not visual acceptance claims.

No change to background app-op commands, Hard Block semantics, Undo records, ADB protocol/access, permissions, brightness, diagnostics, sleep/reboot, native settings or other BOOP bodies. Existing signer and package remain. Display & Sound and Accessibility stay parked.

## Physical boundary and next test

Previously confirmed: bedroom brightness, corrected STANDARD maintenance selectability, Developer Options opening. Startup Manager has not yet passed the real-Shield selection, write/read-back, boot-suppression, manual-launch or Undo cycle. v0.4.0's machine-green result did not make its broken action menu acceptable.

Next: install v0.4.1, ADVANCED -> STARTUP MANAGER -> one Kodi fork -> BLOCK STARTUP / KEEP LAUNCHABLE. Confirm the real menu opens and read-back succeeds before reboot testing. Test manual launch afterward; only then repeat for the remaining forks. These app-ops restrict background activity, not every conceivable startup mechanism. HARD BLOCK remains separately confirmed and prevents manual launch until Undo.

See SESSION_HANDOFF.md for exact diagnosis, verification and historical receipts. Main ownership/context did not change, and no physical installation or laptop synchronization was performed by this chat.
