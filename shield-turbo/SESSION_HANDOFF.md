# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.5 boot-notice diagnostics

The latest signed machine-verified candidate is **v0.5.5 / versionCode 12**, built from exact source `e22beecbaa9d95aeab036ae403684a32a7a33979`.

v0.5.5 is deliberately an **evidence-gathering release**, not another claim that the startup notice is visually fixed. It preserves v0.5.4's fast 500 ms fail-open path and records what Android actually did with the attempted boot overlay so Ryan can report one exact diagnostic line after a real Shield reboot.

### Latest physical evidence

CLEAN START's force-stop/read-back core remains physically accepted from the earlier Shield tests: selected Kodi forks were confirmed stopped after boot; stale Recents/task-manager cards could remain, but the apps were not loaded and reloaded only when focused. Deliberate manual launch still worked normally.

Latest v0.5.4 physical result: Ryan saw **no static CLEAN START sign at all**, but the timing regression was gone. The Shield became navigable again within roughly **one second**. Treat v0.5.4 as physically **positive for fast fail-open/navigation responsiveness** and **negative for notice visibility**. Ryan's latest v0.5.4 message did not separately re-check the target package state, so do not claim a new v0.5.4-specific cleanup acceptance beyond the already accepted CLEAN START core.

Historical presentation results remain important:
- v0.5.1: static card flashed only at the end;
- v0.5.2: 500 ms guessed preroll, no visible card, cleanup physically positive;
- v0.5.3: committed-frame wait, no visible card and almost eight seconds total Turbo completion time;
- v0.5.4: display-bound overlay window context + 500 ms fail-open, still no visible card, but Shield navigation returned within about one second.

After three failed presentation approaches, do **not** add another arbitrary delay or blindly tweak overlay timing. Physical Shield behavior beats CI assumptions.

No movement is allowed. The attempted card remains `SHIELD TURBO · CLEAN START` / `Tidying startup apps`, top-centre, non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation or focus effects.

## v0.5.5 diagnostic change

v0.5.5 adds local boot-notice diagnostics without changing the cleanup engine or increasing the 500 ms presentation bound.

`CleanStartIndicator` now records:
- whether `Settings.canDrawOverlays` was true at the boot attempt;
- which window-context path was used: `APPLICATION_CONTEXT`, `DISPLAY_WINDOW_CONTEXT`, or fallback;
- whether `WindowManager.addView` was accepted or failed;
- whether Android reported `DRAWN`, `FRAME_COMMITTED`, `TIMEOUT`, or bypass;
- elapsed time for the presentation attempt;
- a bounded local detail string for failures such as unavailable display/window manager or addView exception class.

The diagnostic is written to Turbo's existing private `turbo_clean_start` preferences **before** ADB cleanup begins. It contains no account credentials, package target list, ADB key, network address or personal data.

The CLEAN START screen now shows one non-focusable grey line when a diagnostic exists:

`STARTUP NOTICE DIAGNOSTIC: permission=... • window=... • add=... • present=... • ...ms [optional detail]`

Interpretation for the next real-Shield test:
- `permission=NO` => overlay permission was unavailable at boot;
- `add=FAILED` => WindowManager/window creation was rejected; the optional detail should identify the boundary;
- `add=ADDED` + `present=TIMEOUT` => Android accepted the window but never reported a draw/commit within 500 ms;
- `present=DRAWN` or `FRAME_COMMITTED` while Ryan still sees nothing => Android/Tegra/Shield composition is suppressing or occluding the overlay after app-side render. That is evidence to abandon the boot overlay architecture rather than add more timing hacks.

Do not infer visual success from `DRAWN` or `FRAME_COMMITTED`; Ryan's TV remains the authority.

## CLEAN START mechanism unchanged

- `STOP + VERIFY NOW` performs current-user `am force-stop` for one validated selected package, then verifies matching package processes are absent, Android reports stopped state, and the package remains enabled.
- CLEAN START membership is a private reviewed target list; removing a target does not disable/uninstall/clear it.
- Group cleanup is eligible non-system user apps only. BOOP, Android, NVIDIA, Google core/system and system/updated-system packages remain excluded.
- AUTO CLEAN START is opt-in. Non-exported boot receiver + one-shot JobService only when auto is enabled and targets exist.
- Attempts remain approximately 30s, 60s and 120s after boot, maximum 3. No periodic job, foreground service, resident RAM killer or indefinite retry.
- Boot cleanup uses `withTrustedAdb` only and cannot request a fresh ADB RSA approval.
- Current resumed app is skipped. Background-only playback is not independently detected.
- CLEAN START is post-boot cleanup, not universal pre-execution interception; deliberate manual launch releases stopped state.
- Old StartupLedger undo records and explicit HARD BLOCK remain separate/preserved.
- The startup-notice presentation attempt remains bounded at **500 ms max** and fails open into the accepted cleanup path.

No root, device-owner/bootloader work, third-party re-signing, uninstall, `pm clear`, broad kill-all, overclocking or fake RAM score.

## TDD and verification receipts

### Diagnostic RED

Contract commit `888235117ec3b6bb6ea12534ee2a82625aab8eb3` required persisted boot-notice diagnostics and the CLEAN START screen readout before production wiring existed. Workflow run `34225638179`, job `102058962025`: all **68 JVM tests passed** and the source-safety stage failed on the missing diagnostic contract before build/signing. This is the intended RED result.

### Diagnostic GREEN

The diagnostic plumbing was added in small commits to `CleanStartStore`, `CleanStartIndicator`, `CleanStartJobService`, then `StartupManagerActivity`. Intermediate workflow runs were cancelled by the branch's `cancel-in-progress` concurrency rule as newer commits arrived; they are not checkpoints.

The final combined diagnostic implementation commit is `f0563b064724f4c665bf62c1f797780c5a03a349`. Workflow run `34226102737`, job `102060638029`, conclusion **success**: 68 JVM tests, source/API/security contracts, lint, signer/package checks and nonvisual cold/warm launch/no-fatal smoke all passed.

### Final v0.5.5 release

Final source `e22beecbaa9d95aeab036ae403684a32a7a33979` stamps versionCode 12 / versionName 0.5.5 and matching workflow receipt assertions. Workflow run `34226811605`, job `102062828791`, conclusion **success**:

- JVM tests: **68 passed**, 0 failures/errors/skips;
- source/API/security contracts: **passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10056167964`, ZIP `764873` bytes, SHA-256 `85cb44b357b5c7979e3e719ac4b55088a6e8c860774c51047c40dcb0d9391340`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10056218423`, ZIP `96072` bytes, SHA-256 `f6fd08f62ccacd0f22baee16cae71357e29319f4b6d7603808e019a834537e25`;
- delivered APK `Shield-Turbo-v0.5.5.apk`, `2328962` bytes;
- APK SHA-256 `40323820eda72df3592fc756a2b30ee15816cc8633a3e577ade65b5475d7b77f`;
- package `com.boop.shieldturbo`, versionCode 12, versionName 0.5.5;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- downloaded artifact ZIP SHA-256 matched GitHub's artifact digest exactly;
- exactly one APK was extracted and its SHA-256 matched the build receipt;
- the built-source receipt matched exact release source `e22beec...`;
- the APK v2 signing block was independently parsed after download and yielded `CN=BOOP Development,O=BOOP` with the permanent certificate SHA-256 above;
- APK ZIP integrity passed;
- nonvisual install/cold launch/process/Back/warm launch/no-fatal smoke passed;
- **no visual tests ran**. No screenshots, hierarchy dumps, image/golden/layout/focus/appearance/motion judgment.

## Preserve working behavior and boundaries

Keep the proven 10-100% brightness overlay and BrightnessService behavior unchanged. Keep APPS direct launch, app labels, Cancel/Back behavior, StartupLedger undo, loopback-only ADB key in `noBackupFilesDir`, trusted-only boot ADB, and the established signer.

Display & Sound and Accessibility remain parked unless Ryan explicitly returns to them.

Historical receipts remain in Git. In particular v0.5.3 is machine-green but physically rejected for presentation/timing; v0.5.4 is the fast fail-open baseline and physically negative for notice visibility. Never repoint historical checkpoints.

## Next physical test

Install v0.5.5 over the current build, reboot normally, then open SHIELD TURBO -> CLEAN START. Report:
1. whether the static sign appeared;
2. whether navigation still returns quickly;
3. the exact `STARTUP NOTICE DIAGNOSTIC:` line;
4. whether the selected Kodi forks are stopped after this reboot.

Do not change the overlay architecture again until that diagnostic evidence is available.

This handoff update is documentation-only after exact built source `e22beecbaa9d95aeab036ae403684a32a7a33979`; it does not identify a different APK. `main` remained at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` through the release and was not edited by Turbo. GitHub publication is not Windows sync or physical-device deployment.
