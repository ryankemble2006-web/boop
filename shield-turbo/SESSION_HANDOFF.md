# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.4 boot-safe static notice

The latest signed machine-verified candidate is **v0.5.4 / versionCode 11**, built from exact source `5f50ac028eacbe64b1578466a535cef73f10b957`.

### Latest physical evidence

CLEAN START's force-stop core remains physically accepted on Ryan's NVIDIA Shield. The selected Kodi forks are stopped after boot; stale Recents/task-manager cards can remain, but the apps are not loaded and reload only when focused. Preserve that interpretation: recents are history, not evidence that the stopped processes survived. Deliberate manual launch still works normally.

v0.5.3 is **physically rejected for presentation and timing**. Ryan rebooted v0.5.3 and saw absolutely no CLEAN START notice. He also reported that Turbo took almost eight seconds to finish. Treat that as a presentation/timing regression introduced by the 3-second committed-frame wait, not as a failure of the accepted force-stop engine. Do not use v0.5.3 as a physical checkpoint.

Earlier presentation evidence: v0.5.1 flashed the static card only for a microsecond at the end; v0.5.2's 500 ms worker preroll produced no visible card. Two timing-only approaches therefore failed on real Shield hardware.

No movement is allowed. The fixed card remains `SHIELD TURBO · CLEAN START` / `Tidying startup apps`, top-centre, non-focusable and non-touchable. Do not add spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation or focus effects.

### v0.5.4 architectural change

After two timing fixes failed and v0.5.3 regressed duration, the presentation path was re-examined instead of adding another delay. The Android 11+ non-Activity overlay path now uses a display-bound window context for `TYPE_APPLICATION_OVERLAY`:

- obtain the primary display through `DisplayManager` / `Display.DEFAULT_DISPLAY`;
- create a display context, then `createWindowContext(TYPE_APPLICATION_OVERLAY, null)`;
- create the static card and obtain `WindowManager` from that window context;
- keep the existing first-frame commit/on-draw signal;
- wait at most **500 ms** for presentation;
- if presentation is not confirmed within 500 ms, hide/abandon the card and continue CLEAN START immediately.

This is intentionally fail-open. A broken or unavailable notice must not add several seconds to the physically proven cleanup path again. The 500 ms bound is on Turbo's worker, not the UI thread.

The CLEAN START stop engine itself is unchanged: target list, current-app skip, trusted-only loopback ADB, `am force-stop` + read-back verification, system-app exclusions, 30/60/120-second bounded scheduler, max-three attempts, brightness behavior, package identity and permanent signer are all retained.

Expected physical v0.5.4 sequence if the Shield accepts the boot window context: static sign is visible first -> stays completely motionless -> cleanup/freeze occurs underneath -> sign disappears. If the Shield still refuses to present the card, Turbo abandons the notice within 500 ms and proceeds with cleanup. **Neither visual outcome nor real-device total duration is machine-certified; Ryan owns physical acceptance.**

## TDD and verification receipt

v0.5.4 RED contract commit `29829ac2ff6232b50ccc8a5765cdbe1380c38100` added nonvisual requirements for the Android 11+ display-bound overlay window context and a 500 ms fail-open bound. Workflow run `34223321756`, job `102051358423`: all **68 JVM tests passed** and the source-safety stage failed on the new requirements before lint/build/signing, which is the intended RED result.

GREEN implementation commit `afcebf98a330d1a25245324c5b39b576b8d5678e` changed only the indicator window-context path and presentation timeout/fail-open handling. Workflow run `34223556252`, job `102052126373`, conclusion **success**: 68 JVM tests, source/API/security contracts, lint, established signer/package checks and nonvisual install/cold/warm launch smoke all passed before release stamping.

Final v0.5.4 release source `5f50ac028eacbe64b1578466a535cef73f10b957` stamps versionCode 11 / versionName 0.5.4 and matching workflow assertions. Final workflow run `34224066450`, job `102053781796`, conclusion **success**:

- JVM tests: **68 passed**, 0 failures/errors/skips;
- source/API/security contracts: **passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10055037532`, ZIP 760722 bytes, SHA-256 `3b45611dfc5f9fd2795b66e55db7be73e2a369e48b9287f035b969452a2e0f56`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10055084368`, ZIP 93595 bytes, SHA-256 `c00df4b9b7a1dccb4091de41c4342b6e627c3cde74c9a73788320293aef45e55`;
- delivered APK `Shield-Turbo-v0.5.4.apk`, 2319510 bytes;
- APK SHA-256 `f3a685845a0e0a230ef81ae52935e15afb4db94d81197907c84ee4a2d12c476d`;
- package `com.boop.shieldturbo`, versionCode 11, versionName 0.5.4, Leanback launchable;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- downloaded artifact ZIP digest matched GitHub exactly; built-source receipt, package/version receipt, APK digest and signer receipt matched; exactly one APK was extracted;
- nonvisual install/cold launch/process/Back/warm launch/no-fatal smoke passed;
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

Historical receipts remain in Git. In particular, v0.5.3 source `a65c800c459d292a37a092f430d08ddfd2be3742` is machine-green but physically rejected for notice/timing; v0.5.2 source `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4` retains positive cleanup evidence but negative notice visibility. Never repoint historical checkpoints.

This handoff update is documentation-only after exact built source `5f50ac028eacbe64b1578466a535cef73f10b957`; it does not identify a different APK. `main` remained at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` through this release and was not edited by Turbo. GitHub publication is not Windows sync or physical-device deployment.
