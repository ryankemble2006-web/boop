# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current physical evidence

CLEAN START remains physically accepted on Ryan's real Shield.

- v0.5.7 proved the static full-screen CLEAN START notice is physically visible.
- v0.5.8 measured the accepted cleanup job at `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact ANALYSE report is physically accepted as a readable one-screen 9sp monospace evidence sheet.
- v0.5.10 capability evidence: trusted local ADB (`ADB TURBO`) and Android thermal status are available; reviewed generic CPU/GPU sysfs controls and Android fixed-performance are not exposed.
- v0.5.11 Processor Mode Trace is physically accepted. Manual NVIDIA Processor Mode Optimized -> Max performance changed exactly:
  - `system:nv_power_mode`: `1 -> 0`;
  - `property:persist.vendor.sys.phs.cpufreq.boost`: `0 -> 5`;
  - `property:persist.vendor.sys.phs.gpufreq.boost`: `0 -> 5`;
  - `property:persist.vendor.sys.phs.frt.boost`: `0 -> 5`;
  - `property:persist.vendor.sys.phs.frt.min`: `15 -> 20`.

Durable interpretation: observed GUI mapping is `nv_power_mode=1` for Optimized and `0` for Max performance. `system:nv_power_mode` is the stock actuator candidate. The four `persist.vendor.sys.phs.*` properties are NVIDIA downstream evidence only and must never be direct write targets.

## Current machine-verified physical-test candidate

**v0.5.12 / versionCode 19**, exact release source:
`47ef633c1dde937ea9bd94a8e9a7182272c9b9b2`

Final release workflow:
- run `34266282069`, job `102196408374`, conclusion **success**;
- JVM tests: **88 passed**, 0 failures/errors/skips;
- source/API/security contracts: **46 passed**;
- lint: **0 errors / 26 warnings**;
- package `com.boop.shieldturbo`, versionCode 19, versionName 0.5.12, Leanback launchable;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`;
- APK ZIP integrity passed;
- APK SHA-256 `f219eff88054e9776c2b96af985dd68d94ef30ffc1a64a09d7cc4c3aadceb1a8`;
- APK size `2413082` bytes;
- signed artifact `SHIELD-TURBO`: ID `10072080338`, ZIP `795757` bytes, artifact SHA-256 `64e4ff80feaea554efa87ab4caeb4ccff8305dd1e9ddefc8e9d566ab79569f59`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10072132751`, ZIP `97617` bytes, artifact SHA-256 `d7bec2515be014b7814553e9906356a2b2f0cdde741733cec4fcfc0670914227`;
- emulator smoke: install success, cold launch 1338ms, warm launch 458ms, process remained alive, no package FATAL EXCEPTION;
- no screenshot/golden/layout/UI-hierarchy visual automation ran.

v0.5.12 is **machine verified; physical actuator-proof acceptance is pending**.

## v0.5.12 one-shot NVIDIA actuator proof

This is deliberately not persistent TURBO yet. The TURBO page exposes `PROVE NVIDIA MAX SWITCH` and requires an explicit confirmation.

The proof uses already-trusted local ADB and only one writable setting:
- `settings put system nv_power_mode 0` to request Max performance;
- `settings put system nv_power_mode 1` to restore Optimized.

The proof sequence is bounded and transactional:
1. Read baseline and require `nv_power_mode=1` plus downstream `0 / 0 / 0 / 15`. If not, do not change anything.
2. Write only `nv_power_mode=0`.
3. Read back and verify `0` plus NVIDIA downstream `5 / 5 / 5 / 20`.
4. Restore by writing only `nv_power_mode=1`, even if the Max verification failed.
5. Read back and verify final `1` plus downstream `0 / 0 / 0 / 15`.
6. Full-screen proof report shows baseline, Max observation, final observation, `DIRECT VENDOR WRITES • NONE`, and PASS/FAIL.

The actuator does not call `setprop` and never writes `persist.vendor.sys.phs.cpufreq.boost`, `gpufreq.boost`, `frt.boost`, or `frt.min`. It adds no service, receiver, foreground permission, boot reapply, watchdog, or persistent Turbo state. CLEAN START and brightness remain separate and untouched.

TDD receipt:
- final RED checkpoint `d947e6314c12c9c44c91fa9479b984216410e764` failed for the intended missing actuator model/probe/report/UI and exact-write source contracts;
- GREEN feature tree `c185f6beb9e0fb9d72f89568df3da7129081457f` cleared JVM tests, source contracts and lint;
- v0.5.12 release source `47ef633c1dde937ea9bd94a8e9a7182272c9b9b2` is the machine-verified physical-test candidate.

## Approved persistent stock TURBO design

Canonical design: `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`.

Locked future behavior after this actuator passes physically:
- TURBO persists across reboot until manually disabled or thermally auto-disabled;
- NVIDIA Max Performance enters the allowlist only after verified write/read-back/restore on the real Shield;
- any extra stock CPU/GPU/governor control requires explicit allowlisting, firmware-bounded values and complete save/write/read-back/restore semantics;
- exact pre-Turbo NORMAL values are saved before writes and preserved until restore verification succeeds;
- foreground thermal watchdog runs only while TURBO is active;
- boot reapply checks thermal state before performance writes;
- Android thermal status SEVERE or higher restores NORMAL and requires manual re-arm;
- no root, custom kernel, bootloader unlock, voltage modification, above-stock clocks, or thermal/throttling bypass.

## Existing locks

Preserve the physically proven CLEAN START current-user force-stop + verification core, safety exclusions, bounded opt-in 30/60/120s max-three boot scheduler, trusted loopback ADB/private key, static notice host, brightness 10-100%, APPS direct launch/remote behavior, compact diagnostic sheet and all no-data-deletion/no-broad-kill rules. Display & Sound and Accessibility remain parked.

## Next safe step

Ryan installs v0.5.12, puts NVIDIA Processor Mode on **Optimized**, runs `PROVE NVIDIA MAX SWITCH`, and returns the full-screen proof report. Only a physical PASS with final Optimized restoration allows `nv_power_mode` into persistent TURBO.

`main` remains separate at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375`.
