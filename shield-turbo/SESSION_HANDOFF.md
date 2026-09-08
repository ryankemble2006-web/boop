# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.6 attach-gated committed-frame notice

The latest signed machine-verified candidate remains **v0.5.6 / versionCode 13**, built from exact source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`.

CLEAN START's force-stop/read-back core remains physically accepted from earlier real-Shield testing. Selected Kodi forks can leave stale Recents/task-manager cards after force-stop, while the apps themselves are not loaded and reload only when focused. Deliberate manual launch still works. Do not modify the accepted stop/read-back mechanism to solve notice presentation.

## Latest physical evidence: v0.5.6

Ryan installed/rebooted v0.5.6 and reported:
- Shield navigation was still quick;
- Android Home appeared to refresh for a microsecond;
- **no static CLEAN START message was visibly present at startup**;
- the new diagnostic reported **`present=FRAME_COMMITTED` in about `103ms`**.

Ryan did not restate the other diagnostic fields in this report and did not separately restate target-package stopped state for this reboot, so do not invent those details.

This result closes the earlier app-side timing ambiguity but is **not proof that the card was physically visible**. Android's `registerFrameCommitCallback` contract says the content has been rendered into a frame and submitted to the swap chain, but the frame may not currently be visible on the display. Android also explicitly permits the system to change an application-overlay window's position, size or visibility.

Therefore v0.5.6 is physically:
- **positive** for fast fail-open/navigation responsiveness;
- **positive** for app-side attachment/hardware-render/frame-submission evidence;
- **negative** for the current small `WRAP_CONTENT` startup-card visibility.

The microsecond Home refresh is useful supporting evidence that the overlay-window lifecycle affected the launcher surface stack, but it is not visual proof of the card itself.

## Presentation architecture conclusion

Do **not** add another sleep, preroll, longer timeout or draw/commit timing tweak. The small boot-time `WRAP_CONTENT` `TYPE_APPLICATION_OVERLAY` card has now failed physically despite successful permission, window creation/addView in v0.5.5 and successful Android-10+ frame submission in v0.5.6.

The physically proven comparison inside the same app is `BrightnessService`:
- `TYPE_APPLICATION_OVERLAY`;
- `MATCH_PARENT x MATCH_PARENT` window surface;
- `FLAG_NOT_FOCUSABLE` + `FLAG_NOT_TOUCHABLE` + `FLAG_LAYOUT_IN_SCREEN` + `FLAG_LAYOUT_NO_LIMITS`;
- physically visible on Ryan's Shield.

The next presentation experiment, if notice work continues, should therefore change **surface geometry only** rather than cleanup timing or ADB behavior:
- create a transparent **full-screen host overlay** using the proven brightness-style `MATCH_PARENT x MATCH_PARENT` surface and layout flags;
- keep the static CLEAN START card as a child pinned top-centre inside that host;
- preserve the exact text `SHIELD TURBO · CLEAN START` / `Tidying startup apps`;
- preserve non-focusable and non-touchable behavior;
- preserve zero movement: no spinner, fade, pulse, slide, countdown, moving dots, animation or focus effect;
- preserve the **500ms max fail-open** and do not add any artificial dwell delay;
- keep CLEAN START force-stop/read-back, target selection, scheduler and trusted ADB untouched.

This is an architecture/geometry experiment based on a physically proven Turbo surface, not another guess at how long Android needs to draw.

## v0.5.5 -> v0.5.6 diagnostic chain

v0.5.5 physically showed no card and reported:

`permission=yes • window=DISPLAY_WINDOW_CONTEXT • add=ADDED • present=DRAWN • 54ms`

That exposed a false-positive because `isHardwareAccelerated` had been checked before the view attached, allowing `OnDrawListener` to count as presentation.

v0.5.6 corrected that boundary:
- requests `FLAG_HARDWARE_ACCELERATED`;
- on Android 10/API 29+ waits for view attachment;
- only after attachment checks hardware acceleration and registers `registerFrameCommitCallback`;
- Android 10+ only counts `FRAME_COMMITTED`; `DRAWN` is pre-Android-10 fallback only;
- presentation remains bounded to 500ms and fails open.

The real Shield then reported `FRAME_COMMITTED` in ~103ms while the card remained invisible. That means further work belongs at overlay surface geometry/visibility, not app-side draw timing.

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
- v0.5.2: fixed 500ms guessed preroll; no visible card; cleanup physically positive.
- v0.5.3: longer committed-frame wait; no visible card and almost eight seconds total Turbo completion time. Physically rejected for timing/presentation.
- v0.5.4: display-bound window context + 500ms fail-open; no visible card, but navigation returned within roughly one second.
- v0.5.5: diagnostic release; physically invisible card; `permission=yes`, `DISPLAY_WINDOW_CONTEXT`, `ADDED`, `DRAWN`, ~54ms. This proved the old OnDraw presentation signal was a false-positive.
- v0.5.6: attach-gated hardware frame path; navigation remained quick; Home microscopically refreshed; no visible card; diagnostic `FRAME_COMMITTED`, ~103ms. Small-window presentation is physically rejected.

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
- **no visual tests ran**.

## Preserve working behavior and boundaries

Keep the proven 10-100% brightness overlay and `BrightnessService` behavior unchanged. Keep APPS direct launch, app labels, Cancel/Back behavior, StartupLedger undo, loopback-only ADB key in `noBackupFilesDir`, trusted-only boot ADB and the established signer.

Display & Sound and Accessibility remain parked unless Ryan explicitly returns to them.

A temporary preparation branch `shield-turbo-v01-stamp-temp` points at the already-green `d290042...` implementation. It is not the live Turbo lineage or a release checkpoint. Delete it later only via a normal safe branch deletion, never by force-changing the live Turbo branch.

## Next safe step

Do not build another small-window/timing variant. If Ryan continues the startup notice work, use TDD first for the **brightness-style full-screen transparent host** architecture, changing only presentation geometry while preserving CLEAN START semantics and the 500ms fail-open. Physical Shield acceptance remains authoritative.

This handoff update is documentation-only after exact built source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`; it does not identify a different APK. `main` remains outside ordinary Turbo progress and was not edited. GitHub publication is not Windows sync or physical-device deployment.
