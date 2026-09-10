# SHIELD TURBO durable decisions

Updated 2026-09-10. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is concise. Fresh physical Shield evidence beats CI and stale notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Use only the established secret-backed `boop-dev` signer, certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Before edits, read BOOP startup docs plus this branch handoff/status/memory, fetch/check live Turbo and `main`, preserve concurrent work and never force-push.

## Existing physical locks

- v0.5.8 proves CLEAN START itself is sub-second on Ryan's Shield: `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- Preserve the bounded 30/60/120s max-three boot scheduler, trusted loopback ADB/private key, target exclusions, current-app skip, HARD BLOCK separation and StartupLedger undo.
- **CLEAN START boot execution is now silent. Do not reintroduce a boot banner, overlay, popup, or presentation wait.**
- Explain startup behaviour once on the first real app launch: silent CLEAN START or persistent TURBO startup work may cause a brief apparent hang while saved startup work finishes.
- v0.5.7's physically visible static CLEAN START notice is historical evidence only and is superseded by the v0.6.1 UX decision.
- The thermal watchdog's Android-required foreground-service notification association is separate from CLEAN START UX. Do not remove it unless the watchdog is safely redesigned; thermal fallback must not be weakened merely to eliminate notification plumbing.
- Keep brightness 10-100%, APPS direct launch, labels, Cancel/Back and remote-first behavior unless Ryan explicitly changes those features.
- v0.5.10 compact diagnostic sheets are physically accepted: full-screen black, 9sp monospace, tight spacing, screenshot-friendly, Back closes.
- No uninstall, `pm clear`, cache/login/data deletion, broad kill-all, root/device-owner/bootloader work, third-party re-signing or fake RAM scores.

## NVIDIA stock actuator decision

v0.5.11 Processor Mode Trace physically mapped Optimized to Max performance:

- `system:nv_power_mode`: `1 -> 0`;
- `persist.vendor.sys.phs.cpufreq.boost`: `0 -> 5`;
- `persist.vendor.sys.phs.gpufreq.boost`: `0 -> 5`;
- `persist.vendor.sys.phs.frt.boost`: `0 -> 5`;
- `persist.vendor.sys.phs.frt.min`: `15 -> 20`.

v0.5.13 then physically proved the one-shot write/read-back/restore loop:

- Optimized baseline `1/0/0/0/15`;
- write only `settings put system nv_power_mode 0`;
- Max verified `0/5/5/5/20`;
- write only `settings put system nv_power_mode 1`;
- final Optimized verified `1/0/0/0/15`;
- `DIRECT VENDOR WRITES • NONE`.

Durable interpretation:

- `system:nv_power_mode` is a physically accepted stock actuator;
- mapping is `1=Optimized`, `0=Max performance`;
- the four `persist.vendor.sys.phs.*` values are NVIDIA downstream evidence only and MUST NOT be direct write targets.

## Persistent stock TURBO, physically accepted v0.6.0

Canonical design: `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`.
Implementation plan: `docs/superpowers/plans/2026-09-08-shield-turbo-persistent-stock-performance-mode.md`.

Accepted release source: `87feccaeba1c2c5fa2044aeeb572fad947aa985c`.
Version: v0.6.0 / code 21.
Signed artifact: `SHIELD-TURBO` ID `10077342574`.
APK SHA-256: `db0ca06c03a51e7985ca11c479b88becfd1471ca8ad5a7218a0710f7348deb43`.
Release run `34280026842`, job `102242293277`: success, 101 JVM tests, 62 source/API/security contracts, lint/build/signing/archive/emulator smoke green.

Ryan physically verified the complete normal operating loop on the real Shield:

1. Start from NVIDIA Processor Mode Optimized.
2. Enable TURBO in SHIELD TURBO. Shield Processor Mode changes to Max performance and the panel reports `TURBO MODE: ON`, Max performance verified, thermal state available, and Watchdog ON.
3. Reboot while TURBO is armed. Without manually enabling again, the panel returns `TURBO MODE: ON`, `Watchdog: ON`, and `Last change: TURBO retained after reboot`.
4. Turn TURBO OFF. NVIDIA Processor Mode returns to Optimized.

Therefore persistent TURBO v0.6.0 is the physical rollback checkpoint for normal enable to verified Max, persisted desired state across reboot, watchdog startup, boot thermal pre-check, trusted local ADB, and manual verified NORMAL restore.

## v0.6.1 silent-startup UX candidate

User decision 2026-09-10: replace CLEAN START boot presentation with one first-real-launch explanation, then remain silent during boot cleanup.

Machine-verified source: `b030cb44791aa75f8d3e11c50716acff1e1c48c3`.
Version: v0.6.1 / code 22.
Run `34418790720`, job `102689465660`: success.

Durable implementation:

- `CleanStartIndicator.kt` is deleted;
- CLEAN START no longer constructs/shows/waits for/hides a boot presentation surface;
- first real `MainActivity` launch shows `SILENT STARTUP` once using preference `first_install_startup_note_shown`;
- text warns that automatic CLEAN START or persistent TURBO can briefly make startup appear to hang while saved work finishes;
- old `last_indicator_diagnostic` presentation evidence is retired on app start;
- the old timing schema remains but `noticeMs` is recorded as `0`;
- thermal-watchdog foreground-service notification plumbing stays because it belongs to the safety service, not CLEAN START presentation.

Verification receipt: 101 JVM tests + 56 source contracts passed, Android lint/build/signing/archive/emulator smoke passed, APK SHA-256 `b9a94e46c90657bfcbf66d68fe36a25327193189c79ed92fbb9f6b7d45f10f9b`, signed artifact ID `10130188137`, test artifact ID `10130218493`.

v0.6.1 is machine verified only until Ryan physically accepts it on the Shield. v0.6.0 remains the physical rollback checkpoint.

## Persistent TURBO safety rules

Preserve these unless a new design is explicitly approved:

- stock-envelope tuning only;
- exact pre-TURBO NORMAL values saved before writes and preserved until restore verification succeeds;
- only physically proven, explicitly allowlisted stock actuators may be written;
- ambiguous state prefers NORMAL/recovery;
- controller operations stay serialized;
- UI lifecycle must never interrupt an in-flight performance transaction;
- foreground Android thermal watchdog only while verified TURBO is active;
- boot reapply checks thermal status before performance writes;
- Android thermal status SEVERE or higher restores NORMAL, disarms TURBO and requires manual re-arm;
- no above-stock clocks, voltage modification, thermal/throttling bypass, root, custom kernel, boot image or bootloader modification.

## Thermal fallback evidence boundary

SEVERE-or-higher auto-NORMAL is machine-tested but has NOT been physically validated by deliberately overheating the Shield. Do not use heat stress as an acceptance test. If physical-path evidence is wanted later, implement a safe injected/test trigger that exercises the same controller transition without changing the real thermal envelope.

## ADB transport lesson

v0.5.12 exposed `Unexpected ADB stream`. Root cause was stale already-in-flight traffic from older closed streams in the single-connection ADB client. Commit `1b632e09706652d2f2802c6ca4bb28963d9e7685` allows only stale `OKAY`, `WRTE`, or `CLSE` packets addressed to older positive local stream IDs to be ignored. Current, future and invalid stream IDs still fail closed. Do not broaden this exception without a reproduced protocol case.

## TURBO+ HEADROOM decision

Ryan approved a read-only `TURBO+ HEADROOM TEST` designed for physical Shield probing and easy phone photographs.

Durable UX:

- chunky TURBO+ control lives beside the normal TURBO button so it is one Right press away from the default focus;
- opening it immediately runs the read-only scan and shows a dedicated full-screen black page;
- result sections are CPU, GPU, MEMORY, COOLING, and EXTRA STOCK CONTROLS;
- visible evidence is labelled `FOUND`; unavailable evidence is labelled `BLOCKED`;
- failures become a giant `TURBO+ • STOP` page; missing trusted ADB specifically says `ADB NOT READY` and points to `ADVANCED → ENABLE ADB TURBO`;
- footer is `PHOTOGRAPH THIS • BACK TO CLOSE`;
- the report is allowed to scroll only if the physical Shield exposes more evidence than fits.

Probe scope is read-only only: CPU online/current/max/governor; GPU devfreq current/max/min/governor/frequency list; memory/EMC readable clues; thermal zones/cooling devices/fan-related properties; `nv_power_mode` and other surfaced NVIDIA/processor/performance/fan/power/EMC settings. It contains no `settings put`, `setprop`, chmod, root, sysfs writes, overclock, voltage, or thermal bypass.

TDD RED commit: `4d1fe343c9865b084642169975ddb5396065f556`; run `34422694164`, job `102701343392`; existing JVM tests passed and the new source contract failed because the feature was absent.

Machine-green source: `cb1a29fdf808f522b3089a325b2329b46ca1adc5`.
Version: **v0.6.2 / code 23**.
GREEN run `34423280461`, job `102703106376`: success.

Verification receipt:

- 101 JVM tests passed, 0 failures/errors/skips;
- 60 source/API/security contracts passed, including the four Headroom contracts;
- Android lint passed with warnings only;
- signed release build and APK integrity passed;
- package `com.boop.shieldturbo`, versionCode 23, versionName 0.6.2;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- APK SHA-256 `ad357dbfae8579bd5748b1463c11ce75b35ce6adcc721a8cc428da0da1523e99`;
- signed artifact ID `10131769734`, ZIP SHA-256 `33cd6b41b0de11286965920ab1bbc0359bb96b9b1c362a28a5d9b2b66db0ac16`;
- test artifact ID `10131801514`, ZIP SHA-256 `c251260ce2c109d487a5f47afc15025ed795c259c815f6b97994d7be7b43cfb8`;
- emulator API 30 install passed, cold launch `1806ms`, process remained alive with PID `2237`, second launch stayed alive, and no package fatal exception was found.

v0.6.2 is machine-green only until Ryan runs it on the real Shield. The physical photograph is the authority for deciding whether any additional genuine stock-envelope actuator is worth proving.

## Future performance expansion

There is no accepted above-stock clock path. Future TURBO work should look for **additional stock-envelope controls only**, read-only first. Add at most one independently evidenced actuator at a time, and require save, write, read-back, restore and thermal-safe semantics before it can join persistent TURBO.

Presence on generic Tegra Linux does not prove Shield TV writability. The physical TURBO+ photo evidence should drive the next decision.

Treat v0.6.0 source `87feccaeba1c2c5fa2044aeeb572fad947aa985c` and artifact `10077342574` as the physical rollback point until a newer build is physically accepted.
