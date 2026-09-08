# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.0 CLEAN START

The latest machine-verified candidate is **v0.5.0 / versionCode 7**, built from exact source `6f89c0d90fb08e7ef723226b10d33f45d6468f34`. It replaces the failed v0.4 background-app-op startup claim with real selected-package stop/read-back and an explicitly enabled bounded post-boot CLEAN START job.

This candidate is **not physically accepted yet**. Ryan owns real-Shield visual/remote and functional acceptance. Do not describe CLEAN START as proven on Shield until he tests it. Existing physical evidence still stands: bedroom brightness works; STANDARD maintenance items became selectable; Developer Options opens; v0.4.1 Startup Manager menu and manual Kodi launch work; the v0.4 background restriction failed because the Kodi forks remained visible in Shield task manager and manual swipe/force-close noticeably improved performance. Display & Sound and Accessibility remain parked.

### CLEAN START behavior in v0.5.0

- `STOP + VERIFY NOW` performs current-user `am force-stop` for one validated selected package, then verifies matching package processes are absent, Android reports the package stopped, and the package remains enabled for later manual launch.
- CLEAN START membership is a private reviewed target list. Removing a target changes only Turbo's list; it does not disable/uninstall/clear the app.
- Group cleanup runs only against currently eligible non-system user apps. Runtime eligibility is rechecked before stopping. BOOP, Android, NVIDIA, Google core/system apps and system/updated-system packages remain excluded by the existing safety policy.
- The failed v0.4 app-op state ledger is preserved so prior Turbo startup changes remain undoable. HARD BLOCK remains separate and explicit; it is not used automatically by CLEAN START.
- AUTO CLEAN START is opt-in. A non-exported `BOOT_COMPLETED` receiver schedules a non-exported JobService only when auto is enabled and the target list is non-empty.
- Automatic cleanup is finite: attempts are scheduled at approximately 30s, 60s and 120s after boot, maximum 3 attempts. No periodic job, foreground service, resident RAM sweeper or indefinite retry loop exists.
- Boot cleanup uses `withTrustedAdb` only. It cannot request a new ADB RSA approval. If the existing Turbo ADB key is unavailable/untrusted, the attempt is recorded as not applied and bounded retry logic is used; after the final attempt it stops.
- The currently resumed app is skipped rather than force-stopped. Background-only playback is **not independently detected in this release**, so do not claim universal playback protection.
- Each run stores a bounded summary using `STOPPED`, `SKIPPED_IN_USE`, `FAILED` or `NOT_APPLIED` outcomes. A saved setting or task-card presence alone is never treated as proof that an app stopped.
- CLEAN START is post-boot cleanup, not interception. An optional app may start briefly before cleanup. Deliberately launching a force-stopped app releases its stopped state. Do not promise stock Shield can enforce universal "never execute until manual launch" semantics while preserving all normal launch paths.

No root, bootloader/device-owner provisioning, re-signing of third-party APKs, package uninstall, `pm clear`, broad system killing, overclocking or fake RAM score was added.

## Exact v0.5.0 release receipt

- Exact built source: `6f89c0d90fb08e7ef723226b10d33f45d6468f34`.
- Workflow: `Build SHIELD TURBO`, run `34211569892`, job `102013555053`, conclusion **success**.
- JVM unit tests: **68 passed**, 0 failures, 0 errors, 0 skipped. The downloaded JUnit artifact was independently parsed after CI.
- Python API/source/security contracts: **18 passed**. Includes bounded boot job, no-new-ADB-approval, real CLEAN START manager flow, runtime safe-package recheck and no GitHub visual UI testing.
- Android lint: **0 errors, 22 warnings**.
- Signed artifact `SHIELD-TURBO`: ID `10050102992`, ZIP size `758585` bytes, ZIP SHA-256 `344e42969ec61c20dfda1da5748d3468024daeebadac3dd59b27067e8dddb59c`.
- Test artifact `SHIELD-TURBO-TESTS`: ID `10050154988`, ZIP size `84726` bytes, ZIP SHA-256 `0c2916b5c435bcb0737b696f99c3b2ab4357854ba6135ce455b74e0cf6774624`.
- Extracted delivered APK: `Shield-Turbo-v0.5.0.apk`, `2314138` bytes.
- APK SHA-256: `a7b8e25ea73e69976244a706abe301ad2e92b585d420a061480b6a1c760c2145`.
- Package receipt: `com.boop.shieldturbo`, versionCode `7`, versionName `0.5.0`, Leanback launchable, non-debuggable release archive.
- Permanent signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Downloaded artifact ZIP CRC and SHA-256 passed. Extracted APK SHA-256 matched CI `shield-turbo-sha256.txt`; built source matched `shield-turbo-built-commit.txt`; package and signer receipts matched CI.
- Nonvisual emulator install/cold launch/process/Back/warm launch/no-fatal smoke passed after artifact upload.
- **No visual tests ran**: no screenshots, UI hierarchy dumps, golden/image/layout/focus/animation judgment or source-string appearance certification. Ryan owns physical visual acceptance.

Task 5 intermediate source `15ac609c9269525283eaf63631fb93efc2c6ee2d` also passed the same machine gates before the release-version stamp; it is not the delivered v0.5.0 source.

## Physical test procedure for Ryan

Start with one Kodi fork, not all four:

1. Ensure ADB TURBO has already been approved/trusted on this Shield.
2. In `ADVANCED -> STARTUP MANAGER`, select one Kodi fork and use `STOP + VERIFY NOW`. Confirm Turbo reports a verified stop.
3. Add that fork to CLEAN START and enable AUTO CLEAN START.
4. Reboot the Shield. Do not manually launch the test fork before checking the Shield task manager.
5. Check whether the fork has disappeared after CLEAN START has had a chance to run, and whether the Shield feels like the manual force-close state Ryan previously preferred.
6. Launch the same fork normally. It must still open without Undo/reenable steps.
7. Only after that succeeds, add the other Kodi forks.

If the boot result fails, inspect Turbo's last CLEAN START summary first. Distinguish `NOT_APPLIED`/ADB access failure from a verified stop followed by later relaunch. Do not revert to the old app-op-only mechanism as the claimed fix.

## Preserve working behavior and boundaries

Keep the physically proven 10-100% brightness overlay and non-exported BrightnessService unchanged; 100% removes the overlay without intercepting input. Keep APPS direct launch, app-label resolution, Cancel/Back behavior, old startup Undo ledger, local loopback-only ADB key in `noBackupFilesDir`, and the established signer.

Local ADB remains loopback port 5555 only. Initial Network Debugging switch and RSA trust approval belong to the user; automatic boot cleanup cannot create that trust itself. No LAN scan/listening server/persistent shell.

Display & Sound and Accessibility remain unresolved and parked. Do not spend CLEAN START repair passes on them unless Ryan asks.

Historical detailed receipts remain reachable in Git history: v0.4.1 physical-failure handoff at `d64d51db1fd6b7201429ce484be3e93973ff622c`; v0.4.0 startup-policy TDD handoff at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`; earlier release receipts are preserved there. Do not repoint historical checkpoints.

This handoff update is documentation-only after exact built source `6f89c0d90fb08e7ef723226b10d33f45d6468f34`; it does not identify a different APK. Main and other BOOP bodies were not changed by the v0.5.0 work. GitHub publication is not Windows sync or physical Shield deployment.
