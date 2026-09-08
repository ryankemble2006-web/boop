# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

Accepted real-Shield evidence remains:
- v0.5.7 static CLEAN START notice visible;
- v0.5.8 CLEAN START job `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`;
- v0.5.10 compact 9sp ANALYSE report fits one readable screenshot;
- v0.5.11 Processor Mode Trace physically mapped Optimized -> Max performance as:
  - `system:nv_power_mode` `1 -> 0`;
  - CPU boost `0 -> 5`;
  - GPU boost `0 -> 5`;
  - FRT boost `0 -> 5`;
  - FRT min `15 -> 20`.

`nv_power_mode` is the stock actuator candidate. The four `persist.vendor.sys.phs.*` values are downstream NVIDIA evidence only, never direct write targets.

## Current candidate

**v0.5.12 / code 19**, exact release source `47ef633c1dde937ea9bd94a8e9a7182272c9b9b2`.

Release run `34266282069`, job `102196408374`, conclusion **success**:
- **88 JVM tests passed**;
- **46 source/API/security contracts passed**;
- lint **0 errors / 26 warnings**;
- package/version/Leanback/signer/archive checks passed;
- nonvisual emulator install, cold/warm launch and no-fatal smoke passed;
- APK SHA-256 `f219eff88054e9776c2b96af985dd68d94ef30ffc1a64a09d7cc4c3aadceb1a8`;
- APK size `2413082` bytes;
- `SHIELD-TURBO` artifact ID `10072080338`, ZIP SHA-256 `64e4ff80feaea554efa87ab4caeb4ccff8305dd1e9ddefc8e9d566ab79569f59`;
- `SHIELD-TURBO-TESTS` artifact ID `10072132751`, ZIP SHA-256 `d7bec2515be014b7814553e9906356a2b2f0cdde741733cec4fcfc0670914227`.

Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`.

v0.5.12 is machine verified. **Physical actuator-proof acceptance is pending.**

## v0.5.12 actuator proof

`PROVE NVIDIA MAX SWITCH` is a one-shot test, not persistent TURBO.

Starting from Optimized, it:
1. requires baseline `nv_power_mode=1` and downstream `0/0/0/15`;
2. writes only `settings put system nv_power_mode 0`;
3. verifies `0` and downstream `5/5/5/20`;
4. restores only `settings put system nv_power_mode 1`;
5. verifies final `1` and downstream `0/0/0/15`;
6. shows a compact proof report including `DIRECT VENDOR WRITES • NONE` and PASS/FAIL.

If baseline is wrong, nothing changes. If the Max observation fails, restore is still attempted. No `setprop`, no vendor-property writes, no service/receiver/watchdog/boot reapply/persistent Turbo were added. CLEAN START and brightness remain untouched.

## Approved future TURBO design

After a physical actuator PASS, `nv_power_mode` may enter the persistent TURBO allowlist. Persistent TURBO must preserve exact NORMAL state, persist across reboot, use a foreground thermal watchdog while active, check thermal state before boot reapply, and restore/stay NORMAL at Android SEVERE or higher until manual re-arm. No root, custom kernel, bootloader unlock, voltage changes, above-stock clocks, or thermal bypass.

## Locked existing behavior

Freeze the accepted CLEAN START overlay/core/timing, trusted local ADB and target exclusions, bounded 30/60/120s max-three scheduler, brightness 10-100%, APPS direct launch/remote navigation, and compact diagnostic layout. Display & Sound and Accessibility remain parked.

## Next step

Install v0.5.12, put SHIELD Processor Mode on **Optimized**, run `PROVE NVIDIA MAX SWITCH`, and return the full-screen proof report. The final Optimized read-back decides whether the actuator is physically accepted.
