# SHIELD TURBO status

Updated 2026-09-10. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Accepted physical state

- v0.5.7 historically proved the old static CLEAN START notice could be displayed. That presentation is superseded by v0.6.1 and is no longer the desired behaviour.
- v0.5.8 CLEAN START job: `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact 9sp ANALYSE report physically accepted.
- v0.5.11 manual NVIDIA Processor Mode mapping: Optimized `1/0/0/0/15` to Max `0/5/5/5/20`.
- v0.5.13 one-shot NVIDIA actuator proof physically PASSED using only `system:nv_power_mode` writes, with `DIRECT VENDOR WRITES • NONE`.

`system:nv_power_mode` is the physically accepted stock actuator. The four `persist.vendor.sys.phs.*` values remain read-only downstream evidence.

## Persistent TURBO v0.6.0 physically accepted

**v0.6.0 / code 21**, exact release source `87feccaeba1c2c5fa2044aeeb572fad947aa985c`.

Ryan physically verified enable to Max, watchdog ON, persistence across reboot, and manual restore to Optimized. v0.6.0 remains the physical rollback checkpoint.

## v0.6.1 silent-startup candidate

**v0.6.1 / code 22**, source `b030cb44791aa75f8d3e11c50716acff1e1c48c3`.

- automatic CLEAN START is silent at boot;
- old CLEAN START banner/overlay/popup implementation is deleted;
- user gets a one-time `SILENT STARTUP` explanation on first real app launch;
- mandatory thermal-watchdog foreground-service notification plumbing remains for safety.

Machine verification run `34418790720`, job `102689465660`: 101 JVM tests, 56 source contracts, lint, signed release and emulator smoke all passed. v0.6.1 is machine verified but not yet physically accepted.

## v0.6.2 TURBO+ HEADROOM candidate

**v0.6.2 / code 23**, exact machine-green source `cb1a29fdf808f522b3089a325b2329b46ca1adc5`.

Approved physical flow:

- normal TURBO button and chunky `TURBO+ HEADROOM TEST` button sit side by side;
- from default TURBO focus, press Right then OK once;
- dedicated black full-screen report is designed for a phone photograph;
- sections: CPU, GPU, MEMORY, COOLING, EXTRA STOCK CONTROLS;
- each section shows large `FOUND` or `BLOCKED` evidence;
- failures use `TURBO+ • STOP`; missing trusted ADB uses giant `ADB NOT READY` plus the recovery instruction;
- footer: `PHOTOGRAPH THIS • BACK TO CLOSE`;
- the probe is read-only only. No settings writes, setprop, sysfs writes, root, above-stock clocks, voltage, or thermal bypass.

TDD RED: commit `4d1fe343c9865b084642169975ddb5396065f556`, run `34422694164`, job `102701343392`. Existing JVM tests passed and the new source contract failed because the feature did not exist yet.

GREEN verification: run `34423280461`, job `102703106376`, conclusion success:

- 101 JVM tests passed, 0 failures/errors/skips;
- 60 source/API/security contracts passed, including all four TURBO+ headroom contracts;
- Android lint completed successfully with warnings only;
- release build/signing/archive passed;
- package `com.boop.shieldturbo`, versionCode 23, versionName 0.6.2;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- APK ZIP integrity passed;
- APK SHA-256 `ad357dbfae8579bd5748b1463c11ce75b35ce6adcc721a8cc428da0da1523e99`;
- signed artifact `SHIELD-TURBO` ID `10131769734`, artifact ZIP SHA-256 `33cd6b41b0de11286965920ab1bbc0359bb96b9b1c362a28a5d9b2b66db0ac16`;
- test artifact ID `10131801514`, ZIP SHA-256 `c251260ce2c109d487a5f47afc15025ed795c259c815f6b97994d7be7b43cfb8`;
- emulator API 30 install passed, cold launch `1806ms`, process remained alive with PID `2237`, second launch remained alive, and no package fatal exception was found.

v0.6.2 is **machine verified, not yet physically accepted**. The next evidence is Ryan's photograph of the real Shield result or STOP page.

## Current state

- v0.6.0 / code 21: physical rollback checkpoint.
- v0.6.1 / code 22: machine-green silent-startup candidate.
- v0.6.2 / code 23: newest machine-green TURBO+ candidate, physical Shield test pending.

At the start of this session, live `main` was `5179f95961c9c43b4939dd1ea4349a32eb7f99d1`.
