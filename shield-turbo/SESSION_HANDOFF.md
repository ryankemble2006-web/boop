# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current physical evidence

CLEAN START remains physically accepted on Ryan's real Shield.

- v0.5.7 proved the brightness-style transparent full-screen CLEAN START notice is physically visible.
- v0.5.8 measured the accepted cleanup job at `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact ANALYSE report is physically accepted. Ryan's Shield screenshot showed the full 9sp monospace report in one readable frame.
- The v0.5.10 real-Shield performance probe established trusted local ADB (`ADB TURBO`), Android thermal status support, no reviewed CPU/GPU stock sysfs controls, no allowlisted performance-write path, and no Android fixed-performance command. Generic `low_power` / power-key matches are noise, not proof of NVIDIA Processor Mode control.

## Current machine-verified physical-test candidate

**v0.5.11 / versionCode 18**, exact release source:
`8ba8402fdcc970adddcb1acc97b758f5fce76db4`

Final release workflow:
- run `34261526060`;
- job `102180473668`;
- conclusion **success**;
- JVM tests: **82 passed**, 0 failures/errors/skips, verified from the uploaded final test XML;
- source/API/security contracts: all passed, including the new processor-trace no-write/no-resident-component guards;
- lint: **0 errors / 26 warnings**;
- package `com.boop.shieldturbo`, versionCode 18, versionName 0.5.11;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- signer DN `CN=BOOP Development,O=BOOP`;
- APK ZIP integrity passed;
- APK SHA-256 `8b452d1acb6210b6af0c400500edd609026d166a74b71e89b64a27baf180dd6b`;
- APK size `2391774` bytes;
- signed artifact `SHIELD-TURBO`: ID `10070181954`, ZIP `788607` bytes, artifact SHA-256 `0fbf8aafd5a236463babb6ff90e060e72381eec37ac4a1a560eadb4ba2094f46`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10070244960`, ZIP `102653` bytes, artifact SHA-256 `316d4182c68bffe0b995eafc3d84d067a942c51a7121cfeb194f4585c8c57bda`;
- nonvisual emulator install/launch smoke passed through completion;
- no screenshot/golden/layout/UI-hierarchy visual automation ran.

v0.5.11 is **machine green, physical processor-trace acceptance pending**.

## v0.5.11 Processor Mode Trace

Purpose: discover the real no-root interface behind NVIDIA SHIELD `Settings > System > Processor mode` before any performance write is considered.

The TURBO page now has `PROCESSOR MODE TRACE`.

Flow:
1. Put SHIELD Processor mode on **Optimized**.
2. In SHIELD TURBO open TURBO, close the normal ANALYSE report, choose `PROCESSOR MODE TRACE`, then `CAPTURE OPTIMIZED`.
3. Wait for the explicit baseline-saved confirmation.
4. Manually change SHIELD `Settings > System > Processor mode` to **Max performance**.
5. Return to SHIELD TURBO, choose `PROCESSOR MODE TRACE`, then `CAPTURE MAX`.
6. The app compares the two snapshots and opens a compact full-screen diff. Ryan should screenshot that diff and return it as physical evidence.

Trace implementation is deliberately read-only with respect to the Shield. It reads exactly:
- `settings list global`;
- `settings list secure`;
- `settings list system`;
- `getprop`.

The Optimized baseline is stored only in SHIELD TURBO's private `SharedPreferences` under `processor_mode_trace` / `optimized_snapshot`. The trace does not issue `settings put/delete`, `setprop`, `cmd power set`, sysfs writes, fixed-performance enable commands, or any other performance mutation. It adds no service, receiver, foreground permission, watchdog, or boot component. It does not touch CLEAN START or brightness.

Diffs include every changed key/property while ranking NVIDIA/power/performance/mode/profile/Tegra/CPU/GPU-looking candidates first. A key name is still only a clue until physical before/after evidence gives it unambiguous meaning.

TDD receipts for this feature:
- first RED `45fe090d48da577c96e10bb7c6bb5c83b46e24f5` required the parser/diff model;
- model/policy/capture/codec/report slices followed through `0d1d3f549fa1f42fd074c481f5398122052cfc64` with intended RED→GREEN gates;
- final integration RED `d4b9b469a1e4c7a13a5b3b4fc6aea6be020fc494` had 82 JVM tests passing and failed only because the trusted probe/store/UI integration was intentionally absent;
- integration GREEN source `7babe9f8493c8a766ed9a1bc1b3cae8e838dc908` cleared JVM tests, source contracts, and lint;
- release stamp `8ba8402fdcc970adddcb1acc97b758f5fce76db4` changed only version assertions after that green tree.

## Approved persistent stock TURBO design

Canonical design:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Locked behavior once a real writable stock lever is proven:
- TURBO persists across reboot until manually disabled or thermally auto-disabled;
- NVIDIA Max Performance is included only when a genuine interface with read-back is proven on the target firmware;
- any additional CPU/GPU/governor controls must be stock-exposed, reviewed/allowlisted, inside firmware limits, and fully reversible;
- exact pre-Turbo NORMAL values are saved before writes and preserved until restore is verified;
- a foreground thermal watchdog exists only while TURBO is active;
- thermal state is checked before boot-time reapply;
- Android thermal status **SEVERE or higher** immediately restores NORMAL and requires manual re-arm;
- no root, custom kernel, bootloader unlock, voltage modification, above-stock clocks, or thermal/throttling bypass.

**No performance write exists yet.** v0.5.11 is still discovery-only.

## Existing locks

Preserve unless Ryan explicitly changes the relevant feature:

- CLEAN START current-user `am force-stop` + process/stopped/enabled read-back verification core.
- Target safety exclusions for BOOP/Android/NVIDIA/Google-core/system/updated-system packages.
- AUTO CLEAN START opt-in, bounded roughly 30/60/120s, maximum three attempts, no periodic cleaner.
- Boot cleanup uses only already-trusted loopback ADB and cannot request fresh RSA approval.
- ADB key remains private in `noBackupFilesDir`.
- No `pm clear`, uninstall, cache/login/data deletion, broad kill-all or fake RAM scores.
- Static CLEAN START notice: top-centre, non-focusable/non-touchable, no animation or artificial dwell, full-screen transparent host, maximum 500ms presentation fail-open.
- Brightness 10–100% behavior remains physically proven and untouched.
- APPS direct launch, labels, Cancel/Back and remote-first navigation remain.
- Compact ANALYSE report stays full-screen black, 9sp monospace, tight, one logical line per reading, Back closes it.
- Display & Sound and Accessibility remain parked.

## Next safe step

Ryan physically runs the v0.5.11 Optimized→Max trace and returns the compact diff screenshot. Interpret only the actual changed keys/properties. Do **not** add a write until a candidate has unambiguous state meaning, read-back semantics, and a complete restore path.

`main` remains separate and was last verified at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` during this feature cycle.
