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

## v0.5.12 physical actuator result: FAIL at transport layer

Ryan physically ran `PROVE NVIDIA MAX SWITCH` on v0.5.12 from the correct Optimized baseline. The on-screen report showed:

- `RESULT • FAIL`
- `BASELINE • mode=1 cpu=0 gpu=0 frt=0 min=15`
- `REQUEST MAX • settings put system nv_power_mode 0`
- `MAX • NOT VERIFIED • Unexpected ADB stream`
- `RESTORE • settings put system nv_power_mode 1`
- `RESTORE • NOT VERIFIED • Unexpected ADB stream`
- `DIRECT VENDOR WRITES • NONE`
- `FINAL STATE • NOT VERIFIED • SET SHIELD PROCESSOR MODE TO OPTIMIZED MANUALLY`

This is authoritative physical evidence that the baseline was correct, but the v0.5.12 proof did **not** establish whether the Shield accepted or rejected the `nv_power_mode` writes. The failure occurred inside SHIELD TURBO's custom ADB stream transport before read-back verification. Because final restore was not verified, Ryan must manually ensure NVIDIA Processor Mode is **Optimized** before re-testing.

## Root cause and TDD fix

Systematic debugging traced `Unexpected ADB stream` to `AdbWire.execute()`. The single-connection ADB client treated every packet whose local stream ID did not equal the current command's stream ID as fatal. ADB permits traffic already in flight for a just-closed older stream to arrive after the next stream begins, so valid stale `OKAY` / `WRTE` / `CLSE` packets could trigger the exact physical failure.

Regression RED:
- test file `shield-turbo/app/src/test/java/com/boop/shieldturbo/power/AdbWireStreamLifecycleTest.kt`;
- commit `60ea18c70c7f167ba9cab7f4835201f534312cdb`;
- run `34269551454`, job `102207364057`;
- result: 89 JVM tests total with exactly one failure, `AdbWireStreamLifecycleTest`, throwing `java.io.IOException: Unexpected ADB stream`;
- all 46 existing source/API/security contracts remained green.

Minimal GREEN fix:
- implementation commit `1b632e09706652d2f2802c6ca4bb28963d9e7685`;
- `AdbWire.execute()` now ignores only ADB stream packets (`OKAY`, `WRTE`, `CLSE`) addressed to a positive local stream ID lower than the current stream ID;
- current/future/invalid stream IDs still fail closed;
- no NVIDIA actuator logic, command allowlist, vendor-property policy, CLEAN START logic, brightness behavior, boot logic or permissions changed.

The regression passed after the fix together with the existing suite and contracts.

## Current machine-verified physical-test candidate

**v0.5.13 / versionCode 20**, exact release source:
`ca11a45c0db170a9a2031193b9250663c6b5d914`

Final release workflow:
- run `34270157522`, job `102209457841`, conclusion **success**;
- JVM tests: **89 passed**, 0 failures/errors/skips, including the stale closed-stream regression;
- source/API/security contracts: **46 passed**;
- lint: **0 errors / 26 warnings**;
- package `com.boop.shieldturbo`, versionCode 20, versionName 0.5.13, Leanback launchable;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`;
- APK ZIP integrity passed;
- APK SHA-256 `592d012d187d0089b269a48cf5a33065d74d3fd284839995ff6aa4e2662507b0`;
- APK size `2413118` bytes;
- signed artifact `SHIELD-TURBO`: ID `10073578805`, ZIP `795711` bytes, artifact SHA-256 `928be6e7bdfccdda47fc6a3c478d49cb597f6cce64623bf143192a9d653d7496`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10073625187`, ZIP `106724` bytes, artifact SHA-256 `f7a4e93f385533dbba2c675ffe44502a60c487014e73f8ed3273e49e5a8427f7`;
- emulator smoke: install success, cold launch 1600ms, warm launch 365ms, process remained alive, no package FATAL EXCEPTION;
- no screenshot/golden/layout/UI-hierarchy visual automation ran.

The downloaded v0.5.13 APK was independently extracted and re-checked locally against the CI sidecars: package/version, built commit, signer fingerprint, APK SHA-256 and ZIP integrity all match.

**v0.5.13 is machine verified; physical actuator-proof acceptance is pending.**

## One-shot NVIDIA actuator proof boundary

The proof remains deliberately non-persistent. It uses already-trusted local ADB and only these two writes:
- `settings put system nv_power_mode 0` to request Max performance;
- `settings put system nv_power_mode 1` to restore Optimized.

It never calls `setprop` and never directly writes the four `persist.vendor.sys.phs.*` evidence properties. It adds no performance service, receiver, watchdog, boot reapply or persistent TURBO state.

Sequence:
1. Require/read Optimized baseline `1 / 0 / 0 / 0 / 15`.
2. Write only `nv_power_mode=0`.
3. Verify `0 / 5 / 5 / 5 / 20`.
4. Restore only `nv_power_mode=1`, even if Max verification fails.
5. Verify final `1 / 0 / 0 / 0 / 15`.
6. Report exact evidence plus `DIRECT VENDOR WRITES • NONE` and PASS/FAIL.

## Approved persistent stock TURBO design

Canonical design: `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`.

Only after the physical one-shot proof passes may `nv_power_mode` enter the persistent TURBO allowlist. Then preserve the approved rules: exact NORMAL snapshot, reboot persistence, foreground thermal watchdog while active, thermal check before boot reapply, auto-NORMAL at Android SEVERE or higher until manual re-arm, and no root/custom kernel/bootloader/voltage/above-stock clock/thermal bypass.

## Existing locks

Preserve the physically proven CLEAN START current-user force-stop + verification core, target exclusions, bounded opt-in 30/60/120s max-three boot scheduler, trusted loopback ADB/private key, static notice host, brightness 10-100%, APPS direct launch/remote behavior, compact diagnostic sheet and all no-data-deletion/no-broad-kill rules. Display & Sound and Accessibility remain parked.

## Next safe step

1. Manually verify NVIDIA Processor Mode is **Optimized** because v0.5.12 could not verify its restore.
2. Install v0.5.13.
3. Run `PROVE NVIDIA MAX SWITCH` without manually switching Processor Mode during the proof.
4. Return the full-screen result.

Only a physical PASS with final Optimized restoration allows persistent TURBO implementation to proceed.

`main` remains separate at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375`.
