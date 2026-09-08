# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is concise. Fresh physical Shield evidence beats CI and stale notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP.

Use only the established secret-backed `boop-dev` signer. Permanent certificate SHA-256:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Before edits, read BOOP startup docs plus this branch handoff/status/memory, fetch/check live Turbo and `main`, preserve concurrent work and never force-push.

## CLEAN START durable physical decisions

The current-user force-stop + read-back verification core is physically accepted. v0.5.8 measured the job itself at:

`notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`

Do not parallelize, weaken verification, or optimize that path without new physical evidence.

The CLEAN START notice remains static top-centre on the physically proven full-screen transparent overlay host. No animation, artificial dwell, focus/touch capture, or visual CI judgment. Presentation remains fail-open within 500ms.

AUTO CLEAN START remains opt-in and bounded at roughly 30/60/120s, maximum three attempts, using only already-trusted loopback ADB. Preserve target exclusions, private ADB key, current-app skip, HARD BLOCK separation, StartupLedger undo, and the prohibition on uninstall, `pm clear`, cache/login/data deletion, broad kill-all, root/device-owner/bootloader work, third-party re-signing and fake RAM scores.

Keep brightness 10–100%, APPS direct launch, labels, Cancel/Back and remote-first behavior unchanged unless Ryan explicitly changes those features.

## Persistent stock TURBO decision

Ryan approved a persistent no-root performance mode for demanding workloads such as Dolphin.

Canonical design:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Durable rules:
- stock-envelope performance tuning is allowed;
- NVIDIA Max Performance is the preferred first lever only when the target firmware exposes a genuine interface with unambiguous read-back and restore;
- extra CPU/GPU/governor controls require stock exposure, review/allowlisting, firmware-bounded values, and full save/write/read-back/restore/read-back semantics;
- Android fixed-performance is not enabled without positive device evidence;
- TURBO persists across reboot once a real write path exists;
- exact pre-Turbo NORMAL values are saved before writes and preserved until restore verification succeeds;
- foreground thermal watchdog runs only while TURBO is active;
- boot reapply checks thermal state before any performance write;
- Android thermal status SEVERE or higher immediately restores NORMAL, persists NORMAL and requires manual re-arm;
- ambiguous state prefers NORMAL/recovery;
- no above-stock frequency, voltage modification, thermal/throttling bypass, root, custom kernel, boot image or bootloader modification.

## Stage-1 physical evidence

v0.5.10 real-Shield report established:
- trusted local ADB works, capability tier `ADB TURBO`;
- Android PowerManager thermal status is exposed and reported status 0 / None in the captured test;
- no reviewed CPU stock sysfs control was exposed;
- no reviewed GPU stock sysfs control was exposed;
- no allowlisted performance-write path was exposed;
- `cmd power help` did not expose Android fixed-performance mode;
- generic matches such as `low_power`, `nvidia_ranger_enabled`, `power_sounds_enabled` and `sys_uidcpupower` are not NVIDIA Processor Mode evidence;
- observed CPU frequency values are point-in-time diagnostics, not maximum-clock proof.

Therefore do not perform random sysfs or generic power-key experimentation.

## Processor Mode Trace decision

v0.5.11 adds the next targeted discovery pass. It is still **read-only with respect to the Shield**.

The two-step flow deliberately compares the same real Shield around one manual change:
1. Ryan sets NVIDIA Processor mode to **Optimized** and captures the baseline.
2. Ryan manually sets Processor mode to **Max performance** and captures again.
3. SHIELD TURBO displays all changed settings/properties, ranking plausible NVIDIA/performance/mode keys first.

The tracer reads exactly:
- `settings list global`;
- `settings list secure`;
- `settings list system`;
- `getprop`.

It must not issue `settings put/delete`, `setprop`, `cmd power set`, sysfs writes, fixed-performance enable commands or other performance mutations. The only write is private app state storing `optimized_snapshot` in `SharedPreferences("processor_mode_trace")` using the tested codec. It adds no service, receiver, foreground-service permission, watchdog or boot component. It does not touch CLEAN START or brightness.

A changed key remains a candidate, not permission to write it. No performance write enters the allowlist until physical before/after evidence establishes unambiguous meaning and a complete read-back/restore path.

## Compact diagnostic presentation

v0.5.10 physically solved evidence capture. Preserve full-screen black diagnostic sheets, 9sp monospace, tight line spacing, Back-to-close, and screenshot-friendly presentation. Ryan owns all real-device visual acceptance; no screenshot/golden/layout/UI-hierarchy/focus judgment belongs in CI.

## Latest candidate

**v0.5.11 / code 18**, exact release source `8ba8402fdcc970adddcb1acc97b758f5fce76db4`.

Release run `34261526060`, job `102180473668`, success:
- 82 JVM tests, 0 failures/errors/skips;
- all source/API/security contracts passed, including processor-trace no-write/no-resident guards;
- lint 0 errors / 26 warnings;
- package/version/signer/archive checks passed;
- nonvisual emulator install/launch/no-fatal smoke passed;
- APK SHA-256 `8b452d1acb6210b6af0c400500edd609026d166a74b71e89b64a27baf180dd6b`;
- APK size `2391774` bytes;
- signed artifact `SHIELD-TURBO` ID `10070181954`, artifact SHA-256 `0fbf8aafd5a236463babb6ff90e060e72381eec37ac4a1a560eadb4ba2094f46`;
- test artifact `SHIELD-TURBO-TESTS` ID `10070244960`, artifact SHA-256 `316d4182c68bffe0b995eafc3d84d067a942c51a7121cfeb194f4585c8c57bda`.

v0.5.11 is machine verified. Processor Mode Trace is **not physically accepted until Ryan runs the Optimized→Max comparison on the real Shield**.

## Next safe decision

Use the v0.5.11 physical diff to identify the actual NVIDIA Processor Mode state path. Ignore unrelated transient changes. No write before a candidate has unambiguous state meaning, read-back and restore semantics.
