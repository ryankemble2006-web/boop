# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current candidate: v0.5.7 brightness-style full-screen notice host

The latest signed machine-verified candidate is **v0.5.7 / versionCode 14**, built from exact source `5f3b18fca5921e2a47f132c1a149d03d59d7f091`.

Physical acceptance of the v0.5.7 notice is **pending**. Ryan owns real-Shield visual/timing acceptance; CI did not judge appearance.

CLEAN START's force-stop/read-back core remains physically accepted from earlier real-Shield testing. Selected Kodi forks can leave stale Recents/task-manager cards after force-stop while the apps themselves are not loaded and reload only when focused. Deliberate manual launch still works. Do not modify the accepted stop/read-back mechanism to solve notice presentation.

## Latest physical evidence: v0.5.6

Ryan installed/rebooted v0.5.6 and reported:
- navigation remained quick;
- Android Home appeared to refresh for a microsecond;
- no static CLEAN START message was visible;
- diagnostic reported `present=FRAME_COMMITTED` in about `103ms`.

`FRAME_COMMITTED` is app-side frame-submission evidence, not physical display proof. Android may still not make that frame visible, and application-overlay windows may have position/size/visibility adjusted by the system. Therefore the small `WRAP_CONTENT` boot card is physically rejected and no further sleep/preroll/timeout/draw-callback tweak is justified.

## v0.5.7 architecture

v0.5.7 changes presentation geometry only, based on Turbo's physically proven `BrightnessService` surface:
- transparent `FrameLayout` host using `MATCH_PARENT x MATCH_PARENT`;
- `TYPE_APPLICATION_OVERLAY`;
- `FLAG_NOT_FOCUSABLE`;
- `FLAG_NOT_TOUCHABLE`;
- `FLAG_LAYOUT_IN_SCREEN`;
- `FLAG_LAYOUT_NO_LIMITS`;
- `FLAG_HARDWARE_ACCELERATED`;
- the existing static CLEAN START card remains a child pinned top-centre with the same 28dp top offset;
- frame-commit tracking now follows the full-screen host;
- the existing Android-10+ attach-gated `registerFrameCommitCallback` remains;
- the existing 500ms max fail-open remains;
- CLEAN START force-stop/read-back, targets, scheduler and trusted ADB are unchanged.

The visible card text remains exactly:
- `SHIELD TURBO · CLEAN START`
- `Tidying startup apps`

No movement is allowed. No spinner, fade, pulse, slide, countdown, moving dots, progress animation, repeated layout animation, focus effect or artificial dwell was added.

## CLEAN START mechanism and safety unchanged

- `STOP + VERIFY NOW` performs current-user `am force-stop` for one validated selected package, then verifies matching package processes are absent, Android reports stopped state, and the package remains enabled.
- CLEAN START membership is a private reviewed target list; removing a target does not disable/uninstall/clear it.
- Group cleanup is eligible non-system user apps only. BOOP, Android, NVIDIA, Google core/system and system/updated-system packages remain excluded.
- AUTO CLEAN START is opt-in. Non-exported boot receiver + one-shot JobService only when auto is enabled and targets exist.
- Attempts remain approximately 30s, 60s and 120s after boot, maximum 3. No periodic job, foreground service, resident RAM killer or indefinite retry.
- Boot cleanup uses `withTrustedAdb` only and cannot request a fresh ADB RSA approval.
- Current resumed app is skipped. Background-only playback is not independently detected.
- Deliberate manual launch releases stopped state.
- Old StartupLedger undo records and explicit HARD BLOCK remain separate/preserved.
- Failed trusted boot ADB retains bounded exception detail for `LAST CLEAN START DETAIL:`.
- Startup-notice presentation remains bounded at 500ms and fail-open.

No root, device-owner/bootloader work, third-party re-signing, uninstall, `pm clear`, broad kill-all, cache/login/data deletion, overclocking or fake RAM score.

Keep the proven 10-100% brightness overlay and `BrightnessService` behavior unchanged. Keep APPS direct launch, labels, Cancel/Back, loopback-only ADB key in `noBackupFilesDir`, trusted-only boot ADB and the established signer. Display & Sound and Accessibility remain parked.

## Presentation history

- v0.5.1: static card flashed only at the end.
- v0.5.2: fixed 500ms guessed preroll; no visible card; cleanup physically positive.
- v0.5.3: no visible card and almost eight seconds total completion time; rejected for timing/presentation.
- v0.5.4: no visible card, but fast navigation restored with the 500ms fail-open.
- v0.5.5: invisible; diagnostic `permission=yes`, `DISPLAY_WINDOW_CONTEXT`, `ADDED`, `DRAWN`, ~54ms; exposed the old OnDraw false-positive.
- v0.5.6: invisible; navigation quick; Home micro-refresh; diagnostic `FRAME_COMMITTED`, ~103ms; small-window architecture rejected.
- v0.5.7: brightness-style transparent full-screen host; physical result pending.

## v0.5.7 TDD and verification receipts

### RED

Test-only commit `87a7fd89fc58f83e8cee7072701dba66cbd81cdc` added the nonvisual structural requirement for a full-screen host, `MATCH_PARENT x MATCH_PARENT`, `FLAG_LAYOUT_NO_LIMITS`, and host frame tracking before production changed.

Workflow run `34234888441`, job `102089929246`: all **68 JVM tests passed** and source-safety failed on the missing full-screen-host requirement before lint/signing/build. Intended RED.

### Implementation and guard correction

Production commit `a3b3589700741b08358261a0aab352da29016307` changed only `CleanStartIndicator.kt` to the full-screen transparent host architecture.

Workflow run `34235156160`, job `102090848418` stopped at the source-contract stage because the new test incorrectly required `host.addView(card` on one physical line; Kotlin formatted the call over multiple lines. This was a test-formatting failure, not a production architecture failure. JVM tests remained green.

Test-only commit `607ac8f1177bfdfd98a1ba2309d34ccf63b66447` made that guard formatting-agnostic while preserving the same structural requirement. Workflow run `34235514995`, job `102092069734`, conclusion **success**: 68 JVM tests, source/API/security contracts, lint, signer/package/archive checks and nonvisual install/cold/warm launch/no-fatal smoke all passed.

### Stamp bookkeeping

During atomic release preparation an accidental empty root scratch file `__never_create__` was created at commit `f1ced430e15fa4343b72486be2cb32eba70b2809` and immediately removed normally at `873ebde08d10a0376b502bffcaccf459fdeace01`. The post-delete tree SHA returned exactly to the green tree `0a88a05221d45bb487dff005b2e1908857a9802c`. No app content survived this slip, no history was force-rewritten, and the root-only scratch path was outside the Turbo workflow path filter. Neither commit is a release checkpoint.

### Final v0.5.7 release

Final source `5f3b18fca5921e2a47f132c1a149d03d59d7f091` is an atomic version stamp on the exact green tree. Compare against its parent shows exactly two files changed (`shield-turbo/app/build.gradle` and `.github/workflows/shield-turbo.yml`), each only changing version assertions to versionCode 14 / versionName 0.5.7. No production code changed after the green implementation.

Workflow run `34236299335`, job `102094767133`, conclusion **success**:
- JVM tests: **68 passed**, 0 failures/errors/skips;
- source/API/security contracts: **passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10060096819`, ZIP `765768` bytes, SHA-256 `6c2098ff9ac4135ad105020567c098dd66deb4223c73da2ee6db51135fa6779e`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10060152208`, ZIP `100919` bytes, SHA-256 `902228e7d291dd16efdba06a6936efe8db674e67bc92567a98690361ee7437f2`;
- delivered APK `Shield-Turbo-v0.5.7.apk`, `2330782` bytes;
- APK SHA-256 `289db308bd387d5cf2249e44dd99a92a00cfae5e73b0e3ee54dca07e154321c0`;
- package `com.boop.shieldturbo`, versionCode 14, versionName 0.5.7, Leanback launchable;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- downloaded release/test ZIP hashes matched GitHub artifact digests exactly;
- APK ZIP integrity passed and exactly one APK was extracted;
- APK hash matched the workflow receipt;
- built-source receipt matched `5f3b18fc...`;
- APK v2 signing block was independently parsed after download and yielded `CN=BOOP Development,O=BOOP` with the permanent certificate SHA-256 above;
- nonvisual install/cold launch/process/Back/warm launch/no-fatal smoke passed;
- **no visual tests ran**. No screenshots, hierarchy dumps, golden/image/layout/focus/appearance/motion judgment.

## Next physical test

Install v0.5.7 over the current build and reboot normally. Report:
1. whether the static sign is finally visible;
2. whether Shield navigation remains quick;
3. the exact `STARTUP NOTICE DIAGNOSTIC:` line;
4. whether selected Kodi forks remain stopped after reboot, if convenient.

If v0.5.7 is visibly correct and motionless, record physical acceptance and freeze notice presentation. If it still remains invisible despite `FRAME_COMMITTED`, do not add timing hacks; reconsider whether any application-overlay notice is worth retaining.

This handoff update is documentation-only after exact built source `5f3b18fca5921e2a47f132c1a149d03d59d7f091`; it does not identify a different APK. `main` was not edited by Turbo. GitHub publication is not Windows sync or physical-device deployment.
