# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Accepted physical state

- v0.5.7 CLEAN START notice visible.
- v0.5.8 CLEAN START job: `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact 9sp ANALYSE report physically accepted.
- v0.5.11 manual NVIDIA Processor Mode mapping: Optimized `1/0/0/0/15` to Max `0/5/5/5/20`.
- v0.5.13 one-shot NVIDIA actuator proof physically PASSED:
  - baseline Optimized `1/0/0/0/15`;
  - Max `0/5/5/5/20` verified after writing only `system:nv_power_mode=0`;
  - final Optimized `1/0/0/0/15` verified after writing only `system:nv_power_mode=1`;
  - `DIRECT VENDOR WRITES • NONE`.

`system:nv_power_mode` is the physically accepted stock actuator for persistent TURBO v1. The four `persist.vendor.sys.phs.*` values remain read-only downstream evidence.

## Persistent TURBO v0.6.0 physically accepted

**v0.6.0 / code 21**, exact release source:
`87feccaeba1c2c5fa2044aeeb572fad947aa985c`

Ryan physically verified the full normal operating loop on the real Shield:

1. Starting from NVIDIA Processor Mode Optimized, pressing `TURBO MODE: OFF` enabled TURBO and the Shield settings changed to Max performance.
2. The TURBO panel reported `TURBO MODE: ON`, `Processor mode: Max performance verified`, `Thermal state: NONE`, and `Watchdog: ON`.
3. After reboot, without manually enabling TURBO again, the panel still reported TURBO ON and `Last change: TURBO retained after reboot`; therefore the saved state, boot receiver, foreground watchdog service, thermal pre-check, trusted local ADB path, and boot retain/reapply path all executed successfully on hardware.
4. Pressing TURBO OFF restored NVIDIA Processor Mode to Optimized on the real Shield.

This is physical acceptance of persistent TURBO's normal enable, reboot persistence/watchdog startup, and manual NORMAL restore path.

The only writable performance lever remains `system:nv_power_mode`. No root, voltage changes, above-stock clocks, direct vendor-property writes, thermal bypass, arbitrary sysfs tuning, kernel, boot-image, or bootloader work is present.

## Machine verification for accepted build

Final release run `34280026842`, job `102242293277`, conclusion success:

- 101 JVM tests passed;
- 62 source/API/security contracts passed;
- Android lint completed successfully with warnings only;
- package `com.boop.shieldturbo`, versionCode 21, versionName 0.6.0, Leanback launchable;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- APK ZIP integrity passed;
- APK SHA-256 `db0ca06c03a51e7985ca11c479b88becfd1471ca8ad5a7218a0710f7348deb43`;
- signed artifact ID `10077342574`, artifact ZIP SHA-256 `0f63b6ecc36df91731d1f3ab91041142fa1f58ae740a1d2f4cc949f73b88aced`;
- test artifact ID `10077384107`, ZIP SHA-256 `3aa92aad10e4bcd5f0906cfa54fe4fcf38d7cd2699eb87fcecb0bbace539ea4f`;
- emulator install passed, cold launch `1541ms`, warm launch `387ms`, process remained alive, no package fatal exception.

A post-build lifecycle regression also proved and fixed that leaving the TURBO UI must not cancel an in-flight performance transaction. RED commit `5603246265d30993177a83be649318d25bfc4887`; GREEN source `87feccaeba1c2c5fa2044aeeb572fad947aa985c`.

## Thermal fallback boundary

The Android thermal watchdog logic is machine-tested to restore NORMAL at `SEVERE` or higher and require manual re-arm. This path has NOT been physically validated by intentionally overheating the Shield, and it should not be. Any further physical validation should use a safe injected/test mechanism rather than heat stress.

## Current state

Persistent SHIELD TURBO v0.6.0 is the current physically accepted checkpoint for normal operation. Preserve it as the rollback point before any new performance work.

`main` remains separate at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375`.