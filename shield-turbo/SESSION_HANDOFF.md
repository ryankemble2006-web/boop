# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.6 attach-gated committed-frame notice

The latest signed machine-verified candidate is **v0.5.6 / versionCode 13**, built from exact source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`.

CLEAN START's force-stop/read-back core remains physically accepted from earlier real-Shield testing. Selected Kodi forks can leave stale Recents/task-manager cards after force-stop, while the apps themselves are not loaded and reload only when focused. Deliberate manual launch still works. Do not modify the accepted stop/read-back mechanism to solve notice presentation.

## Latest physical evidence: v0.5.5 diagnostic

Ryan installed/rebooted the v0.5.5 diagnostic build and reported the startup notice still was not physically visible. The exact semantic diagnostic was:

`permission=yes • window=DISPLAY_WINDOW_CONTEXT • add=ADDED • present=DRAWN • 54ms`

This is decisive app-side evidence:
- overlay permission was present at boot;
- Android 11+ primary-display/window context creation succeeded;
- `WindowManager.addView` succeeded;
- the app-side view drew in about 54 ms;
- **but Android did not report `FRAME_COMMITTED`**.

The v0.5.5 diagnostic therefore exposed a false-positive in our presentation signal. `CleanStartIndicator` checked `card.isHardwareAccelerated` too early, immediately after `addView` and before the overlay was attached to a window. At that point it could report false and fall back to `OnDrawListener`, allowing `DRAWN` to be recorded as if it proved presentation. A draw into a buffer is not proof that the frame was committed to the display compositor.

Do not reinterpret v0.5.5 `DRAWN` as visual/compositor success.

## v0.5.6 fix

v0.5.6 corrects that presentation boundary without changing CLEAN START cleanup behavior:
- the overlay requests `FLAG_HARDWARE_ACCELERATED`;
- on Android 10/API 29+ the indicator waits until the view is attached to a window;
- attachment is observed through `addOnAttachStateChangeListener` / `onViewAttachedToWindow`;
- only after attachment does Turbo check `isHardwareAccelerated` and register `registerFrameCommitCallback`;
- on Android 10+ **only `FRAME_COMMITTED` counts as presentation**;
- `OnDrawListener`/`DRAWN` remains only as the pre-Android-10 fallback;
- if an Android 10+ attached overlay is not hardware accelerated, Turbo records that diagnostic and does not falsely count a draw;
- the presentation wait remains bounded at **500 ms max** and fails open into cleanup.

The static card itself remains `SHIELD TURBO · CLEAN START` / `Tidying startup apps`, top-centre, non-focusable and non-touchable. No spinner, fade, pulse, slide, countdown, moving dots, progress animation, repeated layout animation or focus effects were added.

v0.5.6 also preserves a concurrent evidence improvement: if trusted boot ADB fails, CLEAN START records the actual exception class/message in the bounded item detail and the CLEAN START screen exposes it as `LAST CLEAN START DETAIL:`. Retry scheduling and cleanup semantics are unchanged.

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
- Startup-notice presentation remains bounded at 500 ms and fail-open.

No root, device-owner/bootloader work, third-party re-signing, uninstall, `pm clear`, broad kill-all, overclocking or fake RAM score.

## Presentation history

- v0.5.1: static card flashed only at the end.
- v0.5.2: fixed 500 ms guessed preroll; no visible card; cleanup physically positive.
- v0.5.3: longer committed-frame wait; no visible card and almost eight seconds total Turbo completion time. Physically rejected for timing/presentation.
- v0.5.4: display-bound window context + 500 ms fail-open; no visible card, but navigation returned within roughly one second.
- v0.5.5: diagnostic release; physically invisible card; diagnostic `permission=yes`, `DISPLAY_WINDOW_CONTEXT`, `ADDED`, `DRAWN`, ~54 ms. This proved the old OnDraw presentation signal was a false-positive.
- v0.5.6: attach-gated, hardware-accelerated Android 10+ frame-commit candidate. Physical result pending.

Do not add arbitrary sleeps to solve presentation. Physical Shield evidence remains authoritative.

## TDD / implementation receipts

### RED: require committed boot frame

Test-only commit `53ef056100e63e04787d0b4d6dfe251dd7443dec` required the hardware/attachment/commit path and also contained a concurrent guard requiring real ADB failure detail. Workflow run `34228754199`, job `102069273962`: all **68 JVM tests passed** and the source-safety stage failed before signing, which is the intended RED result.

### Frame implementation

Commit `57247a23908d755d37683e7bf3d2684ded656198` implemented the attach-gated hardware frame-commit path. In workflow run `34229091171`, job `102070415728`, all 68 JVM tests passed and the new frame/attachment contract passed. That run still failed the separate concurrent ADB-detail source contract, so it is not the final GREEN checkpoint.

### Combined GREEN

Commit `d290042b650e63dc54d251118ce022fbe8c21637` preserved the frame fix and added the real boot-ADB failure detail/readout. Workflow run `34229523488`, job `102071858015`, conclusion **success**: unit tests, source/API/security contracts, lint, established signer/package checks and nonvisual install/cold/warm launch/no-fatal smoke all passed.

### Final v0.5.6 release

Final source `5870742c83b193251b323a48e12b0ef6c5b8b5ad` is an atomic version stamp on the combined GREEN implementation. Compare against `d290042...` shows exactly two files changed (`app/build.gradle` and the Turbo workflow), each only changing version assertions to versionCode 13 / versionName 0.5.6. No production code changed after the GREEN checkpoint.

Workflow run `34230235524`, job `102074235862`, conclusion **success**:
- JVM tests: **68 passed**, 0 failures/errors/skips;
- source/API/security contracts: **passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10057548249`, ZIP `765636` bytes, SHA-256 `1c601a47fd94d002f8a4e5d5722444dc81f256057fe444cd86e16223d560a4c3`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10057598695`, ZIP `106686` bytes, SHA-256 `ed2d1f902e577a538c5fe8d041c79fc4c30f75930eb7780fcc0c2902a03e9c4a`;
- delivered APK `Shield-Turbo-v0.5.6.apk`, `2330594` bytes;
- APK SHA-256 `e462db094cf3f09fa4815949b492ed85f0fa12a57a53635951ce875c8c48cd78`;
- package `com.boop.shieldturbo`, versionCode 13, versionName 0.5.6, Leanback launchable;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- downloaded release/test ZIP SHA-256 values matched GitHub's artifact digests exactly;
- exactly one APK was extracted and its SHA-256 matched the build receipt;
- built-source receipt matched exact release source `5870742...`;
- APK v2 signing block was independently parsed after download and yielded `CN=BOOP Development,O=BOOP` with the permanent certificate SHA-256 above;
- nonvisual install/cold launch/process/Back/warm launch/no-fatal smoke passed;
- **no visual tests ran**. No screenshots, hierarchy dumps, image/golden/layout/focus/appearance/motion judgment.

## Preserve working behavior and boundaries

Keep the proven 10-100% brightness overlay and BrightnessService behavior unchanged. Keep APPS direct launch, app labels, Cancel/Back behavior, StartupLedger undo, loopback-only ADB key in `noBackupFilesDir`, trusted-only boot ADB and the established signer.

Display & Sound and Accessibility remain parked unless Ryan explicitly returns to them.

A temporary preparation branch `shield-turbo-v01-stamp-temp` was created while preparing the atomic v0.5.6 stamp and points at the already-green implementation `d290042...`. It is not the live Turbo lineage or a release checkpoint. No safe branch-delete action was available in the connector during this session; delete it later only via a normal safe branch deletion, never by force-changing the live Turbo branch.

## Next physical test

Install v0.5.6 over v0.5.5 and reboot normally. Report:
1. whether the static sign is visible;
2. whether Shield navigation remains quick;
3. the exact `STARTUP NOTICE DIAGNOSTIC:` line;
4. whether the selected Kodi forks are stopped after the reboot.

Expected diagnostic success on Android 10+ is `present=FRAME_COMMITTED`. If the card is still physically invisible **and** diagnostics say `FRAME_COMMITTED`, investigate Shield/Tegra compositor/z-order or move to the physically proven brightness-style surface architecture. If diagnostics time out or report non-hardware acceleration, investigate that exact boundary. Do not add another arbitrary delay.

This handoff update is documentation-only after exact built source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`; it does not identify a different APK. `main` was not edited by Turbo. GitHub publication is not Windows sync or physical-device deployment.
