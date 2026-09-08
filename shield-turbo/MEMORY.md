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
- observed CPU frequency values are point-in-time diagnostics, not maximum-clock proof.

Therefore do not perform random sysfs or generic power-key experimentation.

## Processor Mode physical decision

v0.5.11 Processor Mode Trace is physically accepted on the target Shield.

Ryan captured the Shield in NVIDIA Processor Mode **Optimized**, manually switched to **Max performance**, then captured again. The exact physical diff was:
- `system:nv_power_mode`: `1 -> 0`;
- `property:persist.vendor.sys.phs.cpufreq.boost`: `0 -> 5`;
- `property:persist.vendor.sys.phs.gpufreq.boost`: `0 -> 5`;
- `property:persist.vendor.sys.phs.frt.boost`: `0 -> 5`;
- `property:persist.vendor.sys.phs.frt.min`: `15 -> 20`.

Durable interpretation:
- the observed GUI mapping is `system:nv_power_mode=1` for Optimized and `0` for Max performance;
- `system:nv_power_mode` is the leading stock actuator candidate;
- the four `persist.vendor.sys.phs.*` properties are downstream NVIDIA effects and must **not** be written directly by SHIELD TURBO;
- this physical trace establishes read-side meaning but does not yet prove SHIELD TURBO can safely write/restore the setting and cause NVIDIA to apply/revert its dependent state.

The next proof must write only `system:nv_power_mode`, read back both the setting and the four downstream properties, then restore the exact original state. Vendor-property direct writes remain forbidden.

Only after a successful physical actuator proof may `nv_power_mode` enter the persistent TURBO allowlist.

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

v0.5.11 is machine verified and its read-only Optimized→Max trace is physically verified.

## Next safe decision

Build a bounded actuator proof around **only** `settings put system nv_power_mode`:
- require/read Optimized baseline `1` and downstream `0/0/0/15`;
- request Max by writing `0`;
- verify read-back `0` plus downstream `5/5/5/20`;
- restore by writing `1`;
- verify read-back `1` plus downstream `0/0/0/15`;
- if any check fails, prefer restore-to-Optimized/recovery and retain exact diagnostics;
- never write the four vendor properties directly.

No persistent Turbo, boot reapply or watchdog should be enabled until this actuator proof passes physically.
