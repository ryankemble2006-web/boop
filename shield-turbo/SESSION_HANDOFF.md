# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current physically informed state

CLEAN START presentation and cleanup remain evidence-backed on Ryan's real Shield.

- v0.5.7 proved the brightness-style transparent full-screen overlay host is physically visible. Ryan saw the blue/cyan static CLEAN START notice for roughly one second. Diagnostic: permission YES, `DISPLAY_WINDOW_CONTEXT`, add `ADDED`, present `FRAME_COMMITTED`, about 236ms.
- v0.5.8 physical timing proved the CLEAN START job itself is sub-second: `notice=62ms`, `adbReady=113ms`, `resumed=56ms`, `stops=332ms`, `slowest=com.fork2.app:268ms`, `total=573ms`.
- v0.5.10 changed only the ANALYSE result presentation plus its tests/version stamp. Ryan's physical Shield screenshot shows the full report in one frame with the 9sp monospace compact sheet and the text remains readable. Treat this one-screen report layout as physically accepted unless new evidence says otherwise.

The v0.5.10 physical Stage-1 capability report is also authoritative evidence for this Shield/firmware:
- trusted local ADB is working and the app reports capability tier `ADB TURBO`;
- Android thermal status is exposed and reported `None` / `currentThermalStatus=0` during the test;
- the reviewed allowlisted CPU sysfs controls were not exposed;
- the reviewed allowlisted GPU sysfs controls were not exposed;
- no allowlisted performance-write path was exposed;
- Android fixed-performance mode was not advertised by `cmd power help`;
- the processor-mode clue scan returned generic settings such as `low_power`, `nvidia_ranger_enabled`, `power_sounds_enabled` and `sys_uidcpupower`; these are clues/noise only and are NOT evidence of NVIDIA Processor Mode control;
- the observed CPU0 frequency of 1224 MHz is a point-in-time read, not a maximum-frequency claim.

Conclusion: Stage 1 did its job. The obvious generic Android/sysfs performance levers are not available through the current reviewed shell surface. The next useful read-only investigation is specifically to identify how SHIELD's own **Settings > System > Processor mode > Max performance** state is stored/applied on this firmware, then prove read-back semantics before any write is considered.

## Approved persistent stock TURBO design

Ryan approved a no-root persistent performance mode aimed at demanding workloads such as Dolphin. Canonical design:

`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Durable behavior:
- TURBO persists across reboot until manually disabled or thermally auto-disabled;
- NVIDIA Max Performance is included only when a real read/write interface is proven on the target firmware;
- additional CPU/GPU/governor controls are allowed only when stock-exposed, allowlisted, within firmware-reported limits, and fully reversible with read-back;
- Android fixed-performance mode is diagnostic-only unless later physical/device evidence justifies otherwise;
- a foreground thermal watchdog runs only while TURBO is active;
- Android thermal status is checked before boot-time reapply;
- SEVERE or higher immediately restores NORMAL, persists NORMAL, and never auto-reenables TURBO;
- the original NORMAL snapshot is captured once before the first Turbo write, survives reboot reapply unchanged, and is preserved through failed restore attempts;
- no root, custom kernel, bootloader unlock, voltage changes, above-stock clocks, or thermal/throttling bypass.

Stage 1 of this design is now implemented as a read-only capability probe. **No performance write has been implemented yet.**

## Current physical-test candidate: v0.5.10 compact report

Latest signed machine-verified source: **v0.5.10 / versionCode 17** at exact commit `d7ed55d5437b2fb534b57a0015b3f79341d1eeee`.

Release workflow run `34256065457`, job `102162210965`, conclusion **success**:
- JVM tests: **75 passed**, 0 failures/errors/skips;
- source/API/security contracts: **34 passed**;
- lint: **0 errors, 26 warnings**;
- package `com.boop.shieldturbo`, versionCode 17, versionName 0.5.10, Leanback launchable;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- signer DN `CN=BOOP Development,O=BOOP`;
- APK ZIP integrity passed;
- APK SHA-256 `e4cadc57006d344c2ef7cad557a4a2535b8e1d493b3a80f239cb991c15480b88`;
- APK size `2364850` bytes;
- signed artifact `SHIELD-TURBO`: ID `10068029156`, ZIP `778497` bytes, artifact SHA-256 `30ce88f727272dd78c9bfdea41d1427041450b48232be78f7040215dbc88ad04`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10068084283`, ZIP `96825` bytes, artifact SHA-256 `1d8e76038a2d5cc5887d5ef9186df7a6f22cd3d9e1184244446db30cff437512`;
- emulator smoke: install success, cold launch TotalTime 1198ms, warm launch TotalTime 290ms, process remained alive, no package FATAL EXCEPTION;
- no screenshot/golden/layout automation ran. Ryan's physical Shield screenshot owns visual acceptance.

TDD receipts for the compact report:
- RED source/formatter tests at `28e4f1852752efab3ff0738e64152be3c1d92f79` and `7041fdeb06b63fa63b778d76670ef99bcbed2714` failed for the intended missing compact formatter/report call;
- implementation at `3559c31f60d7228cafc5c3cd9aa7c374dbf7ef54` exposed one compile issue;
- root cause was the invalid `ScrollView.LayoutParams` type; one-line fix at `2c9f0261d659219f5e5216025d7f117f3f6d2c5d` restored green tests/contracts/lint;
- final v0.5.10 stamp at `d7ed55d5437b2fb534b57a0015b3f79341d1eeee` changed only version assertions after the green feature tree.

## Accepted CLEAN START mechanism and safety boundaries

Preserve these unless Ryan explicitly changes the feature:

- `STOP + VERIFY NOW` uses current-user `am force-stop` on one validated selected package and verifies package processes are gone, Android stopped state is true, and package remains enabled.
- CLEAN START target list is private/reviewed and limited to eligible non-system user apps.
- BOOP, Android, NVIDIA, Google core/system and system/updated-system packages remain excluded.
- AUTO CLEAN START is opt-in.
- Non-exported boot receiver plus one-shot JobService only when enabled and targets exist.
- Boot attempts remain roughly 30s, 60s and 120s, maximum three. No periodic cleaner or indefinite retry.
- Boot cleanup uses trusted loopback ADB only and cannot trigger a fresh RSA approval.
- ADB key remains private in `noBackupFilesDir`.
- Current resumed app is skipped. Background-only playback is not independently detected.
- Manual launch releases stopped state.
- HARD BLOCK stays separate and explicit.
- Old StartupLedger undo records remain preserved.
- No root, device owner, bootloader changes, third-party re-signing, uninstall, `pm clear`, cache/login/data deletion, broad kill-all or fake RAM scores.
- Stock-envelope performance tuning is allowed only under the approved TURBO design; above-stock clocks, voltage changes and thermal/throttling bypass remain forbidden.

Keep the physically proven 10-100% brightness behavior unchanged. Keep APPS direct launch, labels, Cancel/Back and remote-first UI. Display & Sound and Accessibility remain parked.

## Static CLEAN START notice lock

Exact notice text remains:
- `SHIELD TURBO · CLEAN START`
- `Tidying startup apps`

It must remain top-centre, static, non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation, focus effect, artificial dwell or movement.

Accepted host geometry:
- transparent `FrameLayout`;
- `MATCH_PARENT x MATCH_PARENT` window;
- `TYPE_APPLICATION_OVERLAY`;
- `FLAG_NOT_FOCUSABLE`, `FLAG_NOT_TOUCHABLE`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_HARDWARE_ACCELERATED`;
- static card as top-centre child;
- attach-gated Android 10+ frame-commit diagnostics;
- maximum 500ms presentation wait/fail-open.

## Compact ANALYSE report lock

Current physically accepted report behavior from v0.5.10:
- opens automatically when ANALYSE completes;
- full-screen black sheet;
- 9sp monospace report body with tight spacing;
- one ProbeResult per logical report line; embedded evidence newlines are flattened with ` | `;
- all evidence is retained;
- Back closes the report;
- underlying normal Shield UI remains chunky and remote-first.

Do not re-expand this report into large cards if the goal is screenshot evidence. The previous card UI remains available underneath for normal navigation/details.

## Next safe step

Run a deeper **read-only** NVIDIA Processor Mode discovery pass. Focus on the exact SHIELD settings/service/property path that changes when the user flips Optimized/Max Performance in NVIDIA Settings. Do not add a write merely because a name contains `power` or `performance`. A candidate interface must have unambiguous state meaning and read-back before it can enter the allowlist.

`main` was verified separately at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` and is unchanged by this Turbo work.
