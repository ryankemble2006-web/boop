# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.1 static CLEAN START notice

The latest signed machine-verified candidate is **v0.5.1 / versionCode 8**, built from exact source `99c90a63a17f4a3a72b561e2c7ed3792deb41649`.

Ryan physically tested v0.5.0 CLEAN START and reported that it appears to have beaten the unwanted Kodi-fork startup problem. He also reported a noticeable roughly three-second Home-screen freeze while CLEAN START does its useful work. That pause needs visible explanation rather than animation or extra activity.

v0.5.1 therefore adds one deliberately static, non-interactive overlay shown only while the automatic CLEAN START job is executing. It says `SHIELD TURBO · CLEAN START` with `Tidying startup apps`. It is fixed at the top centre, does not accept focus or touch, has window animations disabled, and is removed when the job finishes or is stopped/destroyed. If overlay permission is unavailable, the notice is skipped and CLEAN START continues normally.

**No movement is allowed.** Do not add a spinner, pulse, fade, slide, countdown, progress animation, moving dots, layout updates or focus effects. Physical appearance and actual motionlessness remain Ryan's real-Shield acceptance call; machine checks only verify the lifecycle/API constraints and absence of known animation/update mechanisms.

The v0.5.1 pass does not change CLEAN START timing, targets, force-stop/read-back behavior, ADB transport, brightness behavior, manifest permissions, or safety exclusions. The 30s/60s/120s bounded scheduler and maximum three attempts are unchanged.

## CLEAN START mechanism retained

- `STOP + VERIFY NOW` performs current-user `am force-stop` for one validated selected package, then verifies matching package processes are absent, Android reports the package stopped, and the package remains enabled for manual launch.
- CLEAN START membership is a private reviewed target list. Removing a target changes only Turbo's list; it does not disable/uninstall/clear the app.
- Group cleanup runs only against eligible non-system user apps. BOOP, Android, NVIDIA, Google core/system apps and system/updated-system packages remain excluded.
- The old v0.4 StartupLedger remains intact so previous app-op/HARD BLOCK changes remain undoable. HARD BLOCK is separate and is never the automatic CLEAN START mechanism.
- AUTO CLEAN START is opt-in. Its non-exported boot receiver schedules a non-exported one-shot JobService only when auto is enabled and targets exist.
- Attempts remain approximately 30s, 60s and 120s after boot, maximum 3. There is no periodic job, foreground service, resident RAM killer or indefinite retry.
- Boot cleanup uses `withTrustedAdb` only and cannot trigger a fresh ADB RSA approval. Unavailable/untrusted ADB is recorded as NOT_APPLIED with bounded retry/final stop.
- The currently resumed app is skipped. Background-only playback is not independently detected, so do not claim universal playback protection.
- CLEAN START is post-boot cleanup rather than pre-execution interception. An optional app may run briefly before the cleanup job stops it; deliberate manual launch releases stopped state.

No root, device-owner/bootloader work, third-party APK re-signing, uninstall, `pm clear`, broad kill-all, overclocking or fake RAM score.

## Exact v0.5.1 release receipt

- Exact built source: `99c90a63a17f4a3a72b561e2c7ed3792deb41649`.
- Workflow: `Build SHIELD TURBO`, run `34215598324`, job `102026489736`, conclusion **success**.
- JVM unit tests: **68 passed**, 0 failures, 0 errors, 0 skipped.
- Python API/source/security contracts: **21 passed**. Three new contracts cover static/non-interactive overlay API usage, show/hide lifecycle, and unchanged CLEAN START scheduler timing.
- Android lint: **0 errors, 24 warnings**. The two additional warnings are translation-resource warnings for the new fixed indicator strings; they are not runtime failures.
- Signed artifact `SHIELD-TURBO`: ID `10051719431`, ZIP size `759660` bytes, ZIP SHA-256 `2d3b9a3bd5bd04d338c5c553b3ddea9f7734af651e53477dedac14952fa688a1`.
- Test artifact `SHIELD-TURBO-TESTS`: ID `10051766008`, ZIP size `92395` bytes, ZIP SHA-256 `8ea98b5550ab000878fb0cfbe7fce005461925b46ab33f076fda3dd643ca39b5`.
- Extracted delivered APK: `Shield-Turbo-v0.5.1.apk`, `2316998` bytes.
- APK SHA-256: `0b92436b50ac8cb94d3d17855a13d203c4c48bda3a7727c55f5c1167dc74de3c`.
- Package receipt: `com.boop.shieldturbo`, versionCode `8`, versionName `0.5.1`, Leanback launchable, non-debuggable release archive.
- Permanent signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Downloaded artifact ZIP SHA-256 matched GitHub's artifact digest and its CRC passed. Exactly one APK was extracted. APK SHA-256, built-source receipt, package/version receipt and signer receipt all matched CI.
- Nonvisual emulator install/cold launch/process/Back/warm launch/no-fatal smoke passed after artifact upload.
- **No GitHub visual tests ran**: no screenshots, hierarchy dumps, image/golden/layout/focus/appearance/motion judgment. Ryan owns physical visual acceptance.

TDD receipt for this pass: RED commit `b9556bc1409fd11f53fd786198acb46efdfa7d73` left all 68 JVM tests green and failed only the two expected new indicator contracts because `CleanStartIndicator` and job lifecycle hooks did not yet exist. GREEN implementation commit `8e28cde4fea25a64e3c32cb5972dbb1b65af5941` then passed 68 JVM tests, all 21 contracts and lint before the v0.5.1 release stamp.

## Physical v0.5.1 test boundary

Install v0.5.1 over the working v0.5.0 setup and reboot with AUTO CLEAN START and the existing target group enabled.

During the actual cleanup work, expect one static top-centre notice. It must remain completely motionless, must not steal D-pad focus/input, and must disappear when CLEAN START finishes. The underlying cleanup must still achieve the v0.5.0 result Ryan reported, and manually launched apps must continue to work normally afterward. If the notice is absent, first distinguish missing overlay permission from a CLEAN START failure; the notice is deliberately nonessential to cleanup.

Do not mark v0.5.1's appearance/motion physically accepted until Ryan reports the real Shield result.

## Preserve working behavior and boundaries

Keep the proven 10-100% brightness overlay and BrightnessService behavior unchanged. Keep APPS direct launch, app labels, Cancel/Back behavior, StartupLedger undo, loopback-only ADB key in `noBackupFilesDir`, trusted-only boot ADB, and the established signer.

Display & Sound and Accessibility remain parked/unresolved unless Ryan explicitly returns to them.

Historical receipts remain in Git: v0.5.0 exact handoff at the parent documentation lineage, v0.4.1 physical-failure handoff at `d64d51db1fd6b7201429ce484be3e93973ff622c`, and v0.4.0 TDD handoff at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`. Never repoint historical checkpoints.

This handoff update is documentation-only after exact built source `99c90a63a17f4a3a72b561e2c7ed3792deb41649`; it does not identify a different APK. Main and other BOOP bodies were not changed. GitHub publication is not Windows sync or physical-device deployment.
