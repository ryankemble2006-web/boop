# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Accepted physical state

- v0.5.7 CLEAN START notice visible.
- v0.5.8 CLEAN START job `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact 9sp ANALYSE report fits one readable screenshot.
- v0.5.11 manual NVIDIA Processor Mode Optimized -> Max mapped:
  - `system:nv_power_mode` `1 -> 0`;
  - CPU/GPU/FRT boosts `0/0/0 -> 5/5/5`;
  - FRT minimum `15 -> 20`.

`nv_power_mode` is the stock actuator candidate. The four `persist.vendor.sys.phs.*` values are downstream NVIDIA evidence only and are never direct write targets.

## v0.5.12 physical result

The one-shot proof started from the correct physical baseline `mode=1 cpu=0 gpu=0 frt=0 min=15`, then failed at both Max verification and restore verification with `Unexpected ADB stream`. `DIRECT VENDOR WRITES • NONE` remained true. Final state was not verified, so Processor Mode must be manually set to **Optimized** before another proof.

The failure was reproduced in CI as a stale closed-stream ADB lifecycle bug, not as a Shield permission rejection:
- RED commit `60ea18c70c7f167ba9cab7f4835201f534312cdb`;
- run `34269551454`, job `102207364057`;
- 89 JVM tests total, exactly the new stream lifecycle regression failed with `Unexpected ADB stream`;
- all 46 source/security contracts stayed green.

Minimal transport fix commit `1b632e09706652d2f2802c6ca4bb28963d9e7685` ignores only stale `OKAY` / `WRTE` / `CLSE` packets addressed to older already-closed local streams. Current/future/invalid stream IDs still fail closed. NVIDIA proof logic and safety boundaries are unchanged.

## Current candidate

**v0.5.13 / code 20**, exact release source `ca11a45c0db170a9a2031193b9250663c6b5d914`.

Release run `34270157522`, job `102209457841`, conclusion **success**:
- **89 JVM tests passed**;
- **46 source/API/security contracts passed**;
- lint **0 errors / 26 warnings**;
- package/version/Leanback/signer/archive checks passed;
- emulator install, cold/warm launch and no-fatal smoke passed;
- APK SHA-256 `592d012d187d0089b269a48cf5a33065d74d3fd284839995ff6aa4e2662507b0`;
- APK size `2413118` bytes;
- `SHIELD-TURBO` artifact ID `10073578805`, ZIP SHA-256 `928be6e7bdfccdda47fc6a3c478d49cb597f6cce64623bf143192a9d653d7496`;
- `SHIELD-TURBO-TESTS` artifact ID `10073625187`, ZIP SHA-256 `f7a4e93f385533dbba2c675ffe44502a60c487014e73f8ed3273e49e5a8427f7`.

Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`.

v0.5.13 is machine verified. **Physical actuator-proof retest is pending.**

## One-shot actuator boundary

`PROVE NVIDIA MAX SWITCH` remains non-persistent. Starting from Optimized it writes only `settings put system nv_power_mode 0`, verifies Max plus downstream `5/5/5/20`, restores only `settings put system nv_power_mode 1`, and verifies final Optimized plus `0/0/0/15`. No direct vendor-property writes, `setprop`, performance watchdog, boot reapply or persistent TURBO state exist yet.

## Approved future TURBO design

Only after a physical actuator PASS may `nv_power_mode` enter persistent TURBO. Persistent TURBO must preserve exact NORMAL state, persist across reboot, run a foreground thermal watchdog while active, check thermal state before boot reapply, and restore/stay NORMAL at Android SEVERE or higher until manual re-arm. No root, custom kernel, bootloader unlock, voltage changes, above-stock clocks, or thermal bypass.

## Next step

Manually set Processor Mode to **Optimized**, install v0.5.13, run `PROVE NVIDIA MAX SWITCH` without manually changing mode during the proof, and return the full-screen result.
