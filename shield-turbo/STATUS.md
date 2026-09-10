# SHIELD TURBO status

Updated 2026-09-10. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Accepted physical state

- v0.5.7 historically proved the old static CLEAN START notice could be displayed. That presentation is superseded by v0.6.1 and is no longer the desired behaviour.
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

## v0.6.1 silent-startup candidate

**v0.6.1 / code 22**, source `b030cb44791aa75f8d3e11c50716acff1e1c48c3`.

User-requested behaviour:

- automatic CLEAN START is silent at boot;
- the old CLEAN START banner/overlay/popup implementation is deleted;
- the user gets a one-time `SILENT STARTUP` explanation on the first real app launch;
- the explanation says silent CLEAN START or persistent TURBO startup work can cause a brief apparent startup hang;
- the mandatory Android foreground-service notification plumbing for the separate thermal watchdog remains intact for safety.

Machine verification run `34418790720`, job `102689465660`, conclusion success:

- 101 JVM tests passed;
- 56 source safety contracts passed;
- Android lint completed successfully with warnings only;
- release build/signing/archive passed;
- package `com.boop.shieldturbo`, versionCode 22, versionName 0.6.1;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- APK ZIP integrity passed;
- APK SHA-256 `b9a94e46c90657bfcbf66d68fe36a25327193189c79ed92fbb9f6b7d45f10f9b`;
- signed artifact ID `10130188137`, artifact ZIP SHA-256 `5dde6432cfbd0f285d6e334a9cf99833e3b4cad7f68e6d5fea6bbce955f9cc30`;
- test artifact ID `10130218493`, ZIP SHA-256 `abcc2cb5caee391ef513e68d7276aa5e240492664711908d2d2000ca2b147166`;
- emulator API 30 install passed, cold launch `1151ms`, process remained alive with PID `2151`, no package fatal exception.

v0.6.1 is **machine verified but not yet physically accepted** on the real Shield.

## v0.6.2 TURBO+ HEADROOM candidate

Implementation is in progress on top of the v0.6.1 candidate. The approved UX is intentionally simple:

- the TURBO panel exposes a chunky `TURBO+ HEADROOM TEST` control beside the normal TURBO button;
- pressing it opens a dedicated full-screen black read-only report;
- the test reads CPU online/frequency/governor state, GPU devfreq state, memory/EMC clues, cooling/thermal clues, and extra NVIDIA stock-performance settings;
- no performance values are written by the headroom probe;
- each section reports large `FOUND` or `BLOCKED` evidence;
- ADB/setup failures use a full-screen `TURBO+ • STOP` page with a giant `ADB NOT READY` or failure message so Ryan can photograph it;
- footer says `PHOTOGRAPH THIS • BACK TO CLOSE`;
- v0.6.0 remains the physical rollback point and v0.6.1 remains the latest completed machine-green candidate until v0.6.2 CI is fully green.

TDD RED receipt: commit `4d1fe343c9865b084642169975ddb5396065f556`, workflow run `34422694164`, job `102701343392`. Existing JVM unit tests passed; the new Headroom source contract then failed because the approved probe/activity/button did not yet exist.

## Thermal fallback boundary

The Android thermal watchdog logic is machine-tested to restore NORMAL at `SEVERE` or higher and require manual re-arm. This path has NOT been physically validated by intentionally overheating the Shield, and it should not be. Any further physical validation should use a safe injected/test mechanism rather than heat stress.

## Current state

- v0.6.0 / code 21 remains the current physically accepted rollback checkpoint for TURBO performance behaviour.
- v0.6.1 / code 22 is the newest completed machine-verified candidate and changes startup presentation only.
- v0.6.2 / code 23 is the active TURBO+ HEADROOM candidate pending full CI verification.
- Future performance work must stay inside the stock envelope unless a completely new design is explicitly approved.

At the start of this session, live `main` was `5179f95961c9c43b4939dd1ea4349a32eb7f99d1`.
