# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.3 committed-frame static notice

The latest signed machine-verified candidate is **v0.5.3 / versionCode 10**, built from exact source `a65c800c459d292a37a092f430d08ddfd2be3742`.

### Latest physical evidence

CLEAN START's force-stop core remains physically accepted on Ryan's NVIDIA Shield. On the v0.5.2 reboot Ryan reported that he **did not see the CLEAN START notice at all**, but the selected apps were confirmed stopped. Shield task manager still showed cards from the previous session, but those apps were not loaded and reloaded only when focused. Treat those cards as recents/history, not evidence that the target processes survived. This strengthens the physical evidence that CLEAN START applies Android stopped state without disabling normal manual launch.

Therefore v0.5.2 is physically **negative for notice visibility** and physically **positive for cleanup behavior**. Do not modify the accepted force-stop/read-back mechanism merely to fix presentation.

Ryan still observes a noticeable roughly three-second Home-screen freeze while CLEAN START does the stop work. The purpose of the notice is only to make that pause visibly intentional.

No movement is allowed. The fixed card remains `SHIELD TURBO · CLEAN START` / `Tidying startup apps`, top-centre, non-focusable and non-touchable. Do not add spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation or focus effects.

### v0.5.3 change

v0.5.2 used a fixed 500 ms worker-thread preroll after `indicator.show()`. Physical testing proved that waiting after requesting a WindowManager overlay is not the same as knowing Android has actually submitted its first frame.

v0.5.3 removes that fixed sleep. `CleanStartIndicator` now exposes a one-shot presentation signal backed by `CountDownLatch`:
- on API 29+ with hardware acceleration, `ViewTreeObserver.registerFrameCommitCallback` releases the latch after the card's frame is committed for rendering;
- an `OnDrawListener` is the fallback when frame-commit callback is unavailable;
- overlay permission denial, WindowManager failure or addView failure releases the latch immediately so cleanup remains fail-open;
- the CLEAN START worker calls `indicator.awaitPresented(3000)` before constructing `LocalBridge` or beginning ADB work;
- the wait is bounded at 3 seconds and is on Turbo's worker, never the launcher/UI thread.

The static card itself is otherwise unchanged. The 30/60/120-second boot scheduler, max-three-attempt limit, target list, current-app skip, trusted-only local ADB, force-stop/read-back semantics, system-app exclusions, brightness behavior, package identity and signer are unchanged.

Expected physical sequence for Ryan: notice is actually presented first -> remains completely motionless -> cleanup/freeze happens underneath -> notice disappears when cleanup completes. This exact visibility/order remains **pending real-Shield v0.5.3 acceptance**. Machine verification must not be described as visual acceptance.

## TDD and verification receipt

v0.5.3 RED contract commit `a3cf5618ecac10e422d25c09246b919e22514dbd` replaced the old fixed-delay contract with a requirement for a committed-frame presentation handshake before `LocalBridge`. Workflow run `34220546999`, job `102042382524`: all 68 JVM tests stayed green and the source-safety stage failed on the new presentation contract before lint/build/signing, which is the intended RED result.

GREEN implementation commit `92ebc39494610bb5fef8f95eb5fc3489351db3db` added the bounded frame-presentation handshake without touching the CLEAN START stop engine. Workflow run `34220787490`, job `102043147766`, conclusion **success**: unit tests, source/security contracts, lint, established signer/package checks and nonvisual install/cold/warm launch smoke all passed before release stamping.

Final v0.5.3 release source `a65c800c459d292a37a092f430d08ddfd2be3742` changes only version/workflow receipt assertions from the green implementation. Final workflow run `34221284613`, job `102044740488`, conclusion **success**:
- JVM tests: **68 passed**, 0 failures/errors/skips;
- Python source/API/security contracts: **22 passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10053936105`, ZIP 760538 bytes, SHA-256 `083fe26f122d10f0bc27ef4569ba9731799b3b0c4e24bcc6c15be874f0f81b1f`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10053981252`, ZIP 91356 bytes, SHA-256 `e6277ca06695eae76ddc16c23bf6eb897b89e050b00742ede23fddf2426fc869`;
- delivered APK `Shield-Turbo-v0.5.3.apk`, 2319002 bytes;
- APK SHA-256 `fa50dafa7f01f6c6d2e8c9e840449f0a4fd2d57674ec172fe16e02040da133b1`;
- package `com.boop.shieldturbo`, versionCode 10, versionName 0.5.3, Leanback launchable, non-debuggable release;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- nonvisual install/cold launch/process/Back/warm launch/no-fatal smoke passed;
- downloaded artifact ZIP digest and CRC matched GitHub; exactly one APK was extracted; APK digest and built-source receipt matched; the signing certificate was independently parsed from the delivered APK signing block and matched the permanent BOOP certificate;
- **no visual tests ran**. No screenshots, hierarchy dumps, image/golden/layout/focus/appearance/motion judgment.

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

Historical receipts remain in Git: v0.5.2 source `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`; v0.5.1 source `99c90a63a17f4a3a72b561e2c7ed3792deb41649`; v0.5.0 source `6f89c0d90fb08e7ef723226b10d33f45d6468f34`; v0.4.1 handoff at `d64d51db1fd6b7201429ce484be3e93973ff622c`; v0.4.0 TDD handoff at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`. Never repoint historical checkpoints.

This handoff update is documentation-only after exact built source `a65c800c459d292a37a092f430d08ddfd2be3742`; it does not identify a different APK. `main` remained at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` through this release and was not edited by Turbo. GitHub publication is not Windows sync or physical-device deployment.
