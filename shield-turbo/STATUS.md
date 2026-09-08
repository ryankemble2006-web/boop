# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START remains physically accepted:
- v0.5.7 full-screen static notice visible on the real Shield;
- v0.5.8 job timing `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.

v0.5.10 compact ANALYSE presentation is physically accepted. The real-Shield screenshot fit the complete report in one frame at 9sp monospace and remained readable.

Physical Stage-1 performance evidence from v0.5.10:
- trusted local ADB works, tier `ADB TURBO`;
- Android thermal status is exposed;
- reviewed CPU/GPU stock sysfs controls are not exposed;
- no allowlisted performance-write path is exposed;
- Android fixed-performance command is not exposed;
- generic power-key clues are not evidence of NVIDIA Processor Mode control.

## Current candidate

**v0.5.11 / code 18**, exact release source `8ba8402fdcc970adddcb1acc97b758f5fce76db4`.

Release run `34261526060`, job `102180473668`, conclusion **success**:
- **82 JVM tests passed**, 0 failures/errors/skips;
- all source/API/security contracts passed, including processor-trace read-only guards;
- lint **0 errors / 26 warnings**;
- permanent signer/package/version/archive checks passed;
- nonvisual emulator install/launch/no-fatal smoke passed;
- APK SHA-256 `8b452d1acb6210b6af0c400500edd609026d166a74b71e89b64a27baf180dd6b`;
- APK size `2391774` bytes;
- `SHIELD-TURBO` artifact ID `10070181954`, artifact SHA-256 `0fbf8aafd5a236463babb6ff90e060e72381eec37ac4a1a560eadb4ba2094f46`;
- `SHIELD-TURBO-TESTS` artifact ID `10070244960`, artifact SHA-256 `316d4182c68bffe0b995eafc3d84d067a942c51a7121cfeb194f4585c8c57bda`.

Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`.

v0.5.11 is machine green and the **Processor Mode Trace is now physically accepted** on Ryan's Shield.

## Processor Mode Trace physical result

Ryan captured Optimized first, manually changed NVIDIA Processor Mode to Max performance, then captured Max. The physical before/after diff showed exactly:
- `system:nv_power_mode`: `1 -> 0`;
- `property:persist.vendor.sys.phs.cpufreq.boost`: `0 -> 5`;
- `property:persist.vendor.sys.phs.gpufreq.boost`: `0 -> 5`;
- `property:persist.vendor.sys.phs.frt.boost`: `0 -> 5`;
- `property:persist.vendor.sys.phs.frt.min`: `15 -> 20`.

This is authoritative physical evidence that NVIDIA Processor Mode has a real stock state transition on this firmware. The leading actuator candidate is `system:nv_power_mode`, where the observed manual GUI mapping is `1 = Optimized` and `0 = Max performance`. The four `persist.vendor.sys.phs.*` values are treated as NVIDIA downstream effects, not direct write targets.

The trace remains read-only. No performance write has been implemented yet.

## Approved persistent stock TURBO design

Written spec:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Locked future behavior after a genuine writable stock lever is proven:
- persistent across reboot;
- NVIDIA Max Performance only with verified state/read-back/restore;
- additional stock controls only when allowlisted, within firmware limits, reversible and verified;
- foreground thermal watchdog only while active;
- boot-time thermal check before reapply;
- auto-restore NORMAL at SEVERE or higher and stay NORMAL until manual re-arm;
- exact original NORMAL snapshot preserved through reboot and failed restore;
- no root, custom kernel, bootloader unlock, voltage modification, above-stock clocks or thermal bypass.

## Locked existing behavior

Freeze the physically proven CLEAN START overlay/core/timing path, trusted loopback ADB, target exclusions, bounded 30/60/120s max-three scheduler, brightness 10–100%, APPS direct launch/remote navigation, and v0.5.10 compact ANALYSE layout. Display & Sound and Accessibility remain parked.

## Next step

Build a bounded reversible actuator proof that writes **only** `system:nv_power_mode` through trusted local ADB. Starting from the physically observed Optimized state, verify `1 -> 0` and that NVIDIA itself moves the four downstream properties to `5 / 5 / 5 / 20`; then restore `nv_power_mode` to `1` and verify the downstream properties return to `0 / 0 / 0 / 15`. Do not write the vendor properties directly. If any read-back or restore check fails, prefer NORMAL/recovery and report the exact mismatch.
