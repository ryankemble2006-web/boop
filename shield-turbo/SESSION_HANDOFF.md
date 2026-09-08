# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.2 static notice preroll

The latest signed machine-verified candidate is **v0.5.2 / versionCode 9**, built from exact source `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`.

### Latest physical evidence

Ryan physically confirmed the important CLEAN START result on Shield: after reboot he checked Shield Apps and the selected apps **had been force-closed**. Treat the force-stop cleanup core as physically working for his tested targets. He still observes a noticeable roughly three-second Home-screen freeze while startup/cleanup does that work.

v0.5.1 added the agreed static `SHIELD TURBO · CLEAN START` / `Tidying startup apps` notice, but Ryan saw it only for a microsecond at the end. His sequence was effectively: Shield freezes while the apps/startup work happens -> notice flashes -> check Apps -> targets are force-closed. This is a notice timing failure, not a cleanup failure.

No movement is allowed. Do not add spinner/pulse/fade/slide/countdown/moving dots/animation/focus effects to compensate.

### v0.5.2 change

Root cause in v0.5.1: `indicator.show()` was immediately followed by `executor.submit { ... LocalBridge ... }`. Adding an overlay window does not guarantee that a frame has already been presented before background cleanup makes the Shield busy.

v0.5.2 adds one bounded render opportunity: `INDICATOR_PREROLL_MS = 500L`, slept on the CLEAN START **worker thread** after `indicator.show()` and before `LocalBridge(applicationContext)` / any ADB force-stop. The UI/main thread is not slept. The static indicator helper itself is unchanged.

This pass does **not** change the 30/60/120-second boot-job scheduler, max-three-attempt limit, target list, force-stop/read-back semantics, trusted-only ADB, system-app exclusions, brightness, package identity or signer.

Expected physical sequence for Ryan: notice appears first -> stays completely motionless -> cleanup/freeze happens underneath -> notice disappears when cleanup completes. This exact visibility/order remains pending physical v0.5.2 acceptance.

## TDD and verification receipt

RED contract commit `8e1c022bc14bb4165e7b00e2e0d3bebb828a6f7e` added a nonvisual ordering guard requiring `indicator.show()` < 500ms worker preroll < `LocalBridge`. Run `34218093671`, job `102034531355`: all 68 JVM tests passed; the source-contract suite failed exactly one new preroll test because the delay did not yet exist. Old contracts stayed green.

GREEN implementation commit `4a2e15b42be5f336f0e68bea3271ca52241b08c4` changed only `CleanStartJobService.kt` for the worker-thread preroll. Its verification cleared 68 JVM tests, all 22 source/security contracts and lint before release stamping.

Final v0.5.2 release:
- exact source: `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`;
- workflow run `34218659116`, job `102036355692`, conclusion **success**;
- JVM tests: **68 passed**, 0 failures/errors/skips;
- Python source/API/security contracts: **22 passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10052914776`, ZIP 759836 bytes, SHA-256 `0f59a1d4412b6b9671977d4fe18a874d02a1ed75c334b54d611ddf703f82c875`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10052958031`, ZIP 89730 bytes, SHA-256 `601f53247802e45c8099827c855c31587598832fbf6677065c2adff3e75a7fd8`;
- extracted delivered APK `Shield-Turbo-v0.5.2.apk`, 2317430 bytes;
- APK SHA-256 `e2920b1d6c0a5ea36a5f93829729a7391ca7a038f0c9241d891d3bd75b71fd51`;
- package `com.boop.shieldturbo`, versionCode 9, versionName 0.5.2, Leanback launchable, non-debuggable release;
- permanent signer cert SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- downloaded artifact ZIP digest/CRC, exactly-one-APK extraction, APK digest, source receipt, package/version and signer independently matched CI;
- nonvisual install/cold launch/process/Back/warm launch/no-fatal smoke passed;
- **no visual tests ran**. No screenshots, hierarchy dumps, image/golden/layout/appearance/motion judgment.

A transient half-stamp commit `4756785e95f9b34bc6a0843c612dd7f38889748b` existed briefly while version and workflow receipt assertions were being updated sequentially. It was superseded immediately by final release source `6df33fa4...`; do not use it as a candidate or checkpoint. An off-branch orphan commit `59b2d7da...` was never moved onto the live branch and is not a release.

## CLEAN START mechanism retained

- `STOP + VERIFY NOW` performs current-user `am force-stop` for one validated selected package, then verifies matching package processes are absent, Android reports stopped state, and the package remains enabled for manual launch.
- CLEAN START membership is a private reviewed target list; removing a target does not disable/uninstall/clear it.
- Group cleanup is eligible non-system user apps only. BOOP, Android, NVIDIA, Google core/system and system/updated-system packages remain excluded.
- AUTO CLEAN START is opt-in. Non-exported boot receiver + one-shot JobService only when auto is enabled and targets exist.
- Attempts remain approximately 30s, 60s and 120s after boot, maximum 3. No periodic job, foreground service, resident RAM killer or indefinite retry.
- Boot cleanup uses `withTrustedAdb` only and cannot trigger a fresh ADB RSA approval.
- Current resumed app is skipped. Background-only playback is not independently detected.
- CLEAN START is post-boot cleanup, not universal pre-execution interception; deliberate manual launch releases stopped state.
- Old StartupLedger undo records and explicit HARD BLOCK remain separate/preserved.

No root, device-owner/bootloader work, third-party re-signing, uninstall, `pm clear`, broad kill-all, overclocking or fake RAM score.

## Preserve working behavior and boundaries

Keep the proven 10-100% brightness overlay and BrightnessService behavior unchanged. Keep APPS direct launch, app labels, Cancel/Back behavior, StartupLedger undo, loopback-only ADB key in `noBackupFilesDir`, trusted-only boot ADB, and the established signer.

Display & Sound and Accessibility remain parked unless Ryan explicitly returns to them.

Historical receipts remain in Git: v0.5.1 source `99c90a63a17f4a3a72b561e2c7ed3792deb41649`; v0.5.0 source `6f89c0d90fb08e7ef723226b10d33f45d6468f34`; v0.4.1 handoff at `d64d51db1fd6b7201429ce484be3e93973ff622c`; v0.4.0 TDD handoff at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`. Never repoint historical checkpoints.

This handoff update is documentation-only after exact built source `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`; it does not identify a different APK. `main` advanced concurrently to `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` during the release; Turbo did not edit or overwrite it. GitHub publication is not Windows sync or physical-device deployment.
