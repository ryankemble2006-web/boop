# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns current evidence and exact receipts; STATUS.md is the concise verification view. New user evidence overrides dated pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not the unified BOOP body. Keep changes scoped to Turbo and its workflow. Use connected GitHub tools in chat when available. Check live main and Turbo heads before publication, preserve concurrent work, never force-push, and verify live remote HEAD afterward. GitHub publication is not Windows synchronization or device deployment.

Use only the established secret-backed `boop-dev` signer. Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never replace/expose it or publish private ADB/device material.

## User goal and physical evidence

Ryan has four Kodi forks he does not want lingering after Shield boot. He wants only required Shield/Android functionality active by default, with optional apps available for deliberate manual use. He specifically confirmed the forks remain in the Shield task manager under the old v0.4 restriction and that swiping/force-closing them noticeably speeds the Shield. Do not ask again whether this was merely an App Info Force Stop button.

Physically confirmed behavior: bedroom brightness, corrected STANDARD maintenance selectability, Developer Options, v0.4.1 Startup Manager menu, normal manual Kodi launch. v0.4 app-op startup suppression failed acceptance. Display & Sound and Accessibility remain parked.

## CLEAN START is the current mechanism

v0.5.0/code 7 introduces CLEAN START. Exact built source is `6f89c0d90fb08e7ef723226b10d33f45d6468f34`; physical acceptance is pending.

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` on one validated selected package and verifies package processes are absent, Android's stopped state is true, and the package remains enabled. Do not treat command exit, a saved preference or a task card as equivalent verification.

CLEAN START membership is a private reviewed target list. Manual group cleanup and optional AUTO CLEAN START use that list. Only eligible non-system user apps can be targets; existing PowerPolicy exclusions protect BOOP, Android, NVIDIA, Google core and system/updated-system packages. Never broaden to blanket system killing merely to maximize free RAM.

AUTO CLEAN START is opt-in. The app now intentionally has RECEIVE_BOOT_COMPLETED plus a non-exported receiver and non-exported JobService. It is a bounded one-shot cleanup, not a contradiction of the no-resident-cleaner rule: attempts occur at about 30s, 60s and 120s, maximum 3, then stop. No periodic job, foreground service, boot-time polling daemon or indefinite retry.

Boot cleanup may use only the already-trusted loopback ADB key through `withTrustedAdb`. It must never trigger a fresh RSA approval prompt. ADB unavailable/untrusted means NOT_APPLIED and bounded retry/final failure, never a false success.

The currently resumed app is skipped. **Background-only media playback is not separately detected in v0.5.0**, so do not claim all active playback is protected. If Ryan needs that after physical CLEAN START testing, add explicit media-session/playback protection in a later reviewed pass.

Post-boot cleanup is not startup interception. Optional apps may execute briefly before cleanup. Deliberate manual launch releases force-stopped state. Do not promise universal manual-launch-only enforcement on stock Shield, and do not silently escalate to root, device-owner provisioning, modified third-party APKs or automatic HARD BLOCK.

Each automatic run records bounded per-app outcomes: STOPPED, SKIPPED_IN_USE, FAILED or NOT_APPLIED. Use these results to distinguish ADB unavailability from an app that is successfully stopped and later relaunched.

## Rollback and safety

The old v0.4 StartupLedger and original-state records are preserved so prior app-op/hard-block changes remain undoable. First original state wins; verify restore before deleting records; retain failed entries. CLEAN START target removal itself does not mutate package state. HARD BLOCK remains a separately confirmed package disable and must never be the default/hidden fallback.

No `pm clear`, uninstall, cache/file/login removal, broad kill-all, rooting, bootloader work, overclocking or fake RAM/performance score. Force-stop cannot restore interrupted playback or unsaved work.

Local ADB remains loopback port 5555 only with the per-install key in `noBackupFilesDir`; it is separate from the APK signer. Initial Network Debugging and RSA approval belong to the user. No LAN scanning, listening server or persistent shell.

Keep the proven 10-100% brightness overlay and non-exported BrightnessService unchanged; 100% removes the overlay. Keep normal APPS direct launch, real app labels, Cancel/Back behavior and other physically accepted controls.

## Testing and delivery

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/animation judgment or source-string appearance certification. Allowed gates are focused logic/API/protocol/security tests, compilation, lint, package/signer/archive integrity and basic nonvisual crash smoke. Machine green never equals real-Shield CLEAN START acceptance.

Latest candidate receipt: v0.5.0/code 7, source `6f89c0d90fb08e7ef723226b10d33f45d6468f34`, run `34211569892`, job `102013555053`, success; 68 JVM tests, 18 source/security contracts, lint 0 errors/22 warnings. Signed artifact `10050102992`, ZIP SHA-256 `344e42969ec61c20dfda1da5748d3468024daeebadac3dd59b27067e8dddb59c`; tests artifact `10050154988`, SHA-256 `0c2916b5c435bcb0737b696f99c3b2ab4357854ba6135ce455b74e0cf6774624`. APK `Shield-Turbo-v0.5.0.apk`, 2314138 bytes, SHA-256 `a7b8e25ea73e69976244a706abe301ad2e92b585d420a061480b6a1c760c2145`. Permanent signer SHA-256 as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual checks ran.

Historical v0.4.1 physical failure and exact prior receipts remain in Git history at `d64d51db1fd6b7201429ce484be3e93973ff622c:shield-turbo/SESSION_HANDOFF.md`; older TDD receipts remain at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`. Do not repoint checkpoints.
