# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START remains physically accepted on Ryan's real Shield:
- v0.5.7 full-screen transparent notice host visible for roughly one second;
- v0.5.8 real job timing `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.

v0.5.10 compact ANALYSE presentation is now physically accepted too. Ryan supplied a real-Shield screenshot showing the entire report in one frame with the 9sp monospace sheet still readable. Do not revert the screenshot report to the old large-card presentation.

## Current candidate

**v0.5.10 / code 17**, exact built source `d7ed55d5437b2fb534b57a0015b3f79341d1eeee`.

Release run `34256065457`, job `102162210965`, conclusion **success**:
- **75 JVM tests passed**;
- **34 source/API/security contracts passed**;
- lint **0 errors / 26 warnings**;
- permanent signer/package/version/archive checks passed;
- nonvisual emulator install/cold/warm launch/no-fatal smoke passed;
- APK SHA-256 `e4cadc57006d344c2ef7cad557a4a2535b8e1d493b3a80f239cb991c15480b88`;
- APK size `2364850` bytes;
- `SHIELD-TURBO` artifact ID `10068029156`, ZIP SHA-256 `30ce88f727272dd78c9bfdea41d1427041450b48232be78f7040215dbc88ad04`;
- `SHIELD-TURBO-TESTS` artifact ID `10068084283`, ZIP SHA-256 `1d8e76038a2d5cc5887d5ef9186df7a6f22cd3d9e1184244446db30cff437512`.

Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`.

## Stage-1 performance evidence from the real Shield

The physical v0.5.10 report established:
- capability tier `ADB TURBO` and trusted local ADB read works;
- Android thermal status is exposed and was `None` / status 0 during the test;
- reviewed allowlisted CPU stock sysfs controls: not exposed;
- reviewed allowlisted GPU stock sysfs controls: not exposed;
- performance write access: no allowlisted control path exposed;
- Android fixed-performance command: not exposed;
- processor-mode clue output currently contains generic `low_power`/power keys and is not evidence of NVIDIA Max Performance control.

Stage 1 remains read-only. No performance write, watchdog service or boot reapply component has been added yet.

## Approved persistent stock TURBO design

Written spec:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Locked behavior:
- persistent across reboot once real Turbo writes exist;
- NVIDIA Max Performance when genuinely exposed/writable/read-back verified;
- additional stock CPU/GPU controls only when allowlisted, within firmware limits, reversible and read-back verified;
- foreground thermal watchdog only while active;
- boot-time thermal check before reapply;
- auto-restore NORMAL at SEVERE or higher and stay NORMAL;
- exact original NORMAL snapshot preserved across reboot and failed restore;
- no root, custom kernel, bootloader unlock, voltage changes, above-stock clocks or thermal bypass;
- Android fixed-performance remains diagnostic-only unless later evidence changes that decision.

## Locked existing behavior

Freeze:
- v0.5.7 full-screen static CLEAN START host;
- 500ms presentation fail-open;
- trusted loopback ADB;
- force-stop + verification core;
- target safety exclusions;
- opt-in bounded 30/60/120s max-three CLEAN START scheduler;
- brightness 10-100% behavior;
- APPS direct launch and remote navigation behavior;
- v0.5.10 compact ANALYSE report for screenshot evidence.

Stock-envelope performance tuning is allowed only under the approved TURBO design. Above-stock clocks, voltage changes, thermal/throttling bypass, root and bootloader/kernel modification remain excluded.

Display & Sound and Accessibility remain parked.

## Next step

Deeper **read-only** NVIDIA Processor Mode discovery. Identify the exact SHIELD Settings/service/property state that changes between Optimized and Max Performance. Ignore generic power-key name matches. No write until a candidate interface has unambiguous meaning and read-back semantics.
