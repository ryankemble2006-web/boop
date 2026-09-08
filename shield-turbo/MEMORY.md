# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is concise. Fresh physical Shield evidence beats CI and stale notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Use only the established secret-backed `boop-dev` signer, certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Before edits, read BOOP startup docs plus this branch handoff/status/memory, fetch/check live Turbo and `main`, preserve concurrent work and never force-push.

## Existing physical locks

- v0.5.8 proves CLEAN START itself is sub-second on Ryan's Shield: `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- Preserve the accepted static CLEAN START notice, bounded 30/60/120s max-three boot scheduler, trusted loopback ADB/private key, target exclusions, current-app skip, HARD BLOCK separation and StartupLedger undo.
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

Therefore persistent TURBO v0.6.0 is the physical rollback checkpoint for:

- normal enable to verified Max;
- persisted desired state across reboot;
- boot receiver and foreground watchdog startup;
- thermal pre-check before boot retain/reapply;
- trusted local ADB use during boot path;
- manual verified restore to original Optimized NORMAL state.

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

## Next safe decision

Treat v0.6.0 source `87feccaeba1c2c5fa2044aeeb572fad947aa985c` and artifact `10077342574` as the accepted rollback point. Any future performance expansion should add one independently evidenced stock control at a time with save, write, read-back, restore and thermal-safe semantics.
