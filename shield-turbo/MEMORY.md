# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is concise. Fresh physical Shield evidence beats CI and stale notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Use only the established secret-backed `boop-dev` signer, certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Before edits, read BOOP startup docs plus this branch handoff/status/memory, fetch/check live Turbo and `main`, preserve concurrent work and never force-push.

## Existing physical locks

- v0.5.8 proves CLEAN START itself is sub-second on Ryan's Shield: `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`. Do not optimize away verification without new physical evidence.
- Keep the physically accepted static top-centre full-screen transparent CLEAN START notice, non-focusable/non-touchable, no animation/artificial dwell, maximum 500ms presentation fail-open.
- AUTO CLEAN START stays opt-in and bounded around 30/60/120s, maximum three attempts, using already-trusted loopback ADB only. Preserve target exclusions, private ADB key, current-app skip, HARD BLOCK separation and StartupLedger undo.
- No uninstall, `pm clear`, cache/login/data deletion, broad kill-all, root/device-owner/bootloader work, third-party re-signing or fake RAM scores.
- Keep brightness 10-100%, APPS direct launch, labels, Cancel/Back and remote-first behavior unless Ryan explicitly changes those features.
- v0.5.10 compact diagnostic sheets are physically accepted: full-screen black, 9sp monospace, tight spacing, screenshot-friendly, Back closes.

## Persistent stock TURBO decision

Ryan approved persistent no-root performance mode for demanding workloads such as Dolphin. Canonical design: `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`.

Durable rules:
- stock-envelope tuning only;
- exact pre-Turbo NORMAL values saved before writes and preserved until restore verification succeeds;
- NVIDIA Max Performance only through a physically proven stock actuator with read-back and restore;
- extra CPU/GPU/governor controls require stock exposure, explicit allowlisting, firmware-bounded values and complete save/write/read-back/restore semantics;
- persistent across reboot once a real actuator is accepted;
- foreground thermal watchdog only while TURBO is active;
- boot reapply checks thermal state before writes;
- Android thermal status SEVERE or higher restores NORMAL, persists NORMAL and requires manual re-arm;
- ambiguous state prefers NORMAL/recovery;
- no above-stock clocks, voltage modification, thermal/throttling bypass, root, custom kernel, boot image or bootloader modification.

## Processor Mode physical decision

v0.5.11 Processor Mode Trace is physically accepted. Manual Optimized -> Max performance changed exactly:
- `system:nv_power_mode`: `1 -> 0`;
- `persist.vendor.sys.phs.cpufreq.boost`: `0 -> 5`;
- `persist.vendor.sys.phs.gpufreq.boost`: `0 -> 5`;
- `persist.vendor.sys.phs.frt.boost`: `0 -> 5`;
- `persist.vendor.sys.phs.frt.min`: `15 -> 20`.

Durable interpretation:
- observed mapping is `nv_power_mode=1` Optimized, `0` Max performance;
- `system:nv_power_mode` is the leading stock actuator candidate;
- the four `persist.vendor.sys.phs.*` values are downstream NVIDIA evidence and MUST NOT be directly written by SHIELD TURBO.

## v0.5.12 physical FAIL and ADB transport lesson

The first physical actuator proof on v0.5.12 began from the exact expected Optimized baseline `1 / 0 / 0 / 0 / 15` but both Max and restore verification ended with `Unexpected ADB stream`. The report retained `DIRECT VENDOR WRITES • NONE` and could not verify final Optimized state.

Durable interpretation:
- do not classify this as NVIDIA rejecting `nv_power_mode`;
- it was a SHIELD TURBO ADB transport failure before state verification;
- after any proof whose final restore is not verified, manually put the Shield back on **Optimized** before another test.

Systematic debugging reproduced the physical failure exactly in `AdbWireStreamLifecycleTest`:
- RED commit `60ea18c70c7f167ba9cab7f4835201f534312cdb`;
- run `34269551454`, job `102207364057`;
- 89 JVM tests with exactly one failure, `java.io.IOException: Unexpected ADB stream` in the new lifecycle regression;
- all 46 source/security contracts remained green.

Root cause: the custom single-connection ADB client treated any stream packet addressed to a stream other than the current one as fatal. Legitimate already-in-flight traffic for an older closed stream can arrive after a later stream starts.

Minimal fix commit `1b632e09706652d2f2802c6ca4bb28963d9e7685`:
- ignore only `OKAY`, `WRTE` or `CLSE` packets whose positive local stream ID is lower than the current local stream ID;
- current, future and invalid stream IDs retain strict validation/failure;
- no NVIDIA proof command, safety allowlist, vendor-property rule, CLEAN START behavior, brightness behavior or permissions changed.

Do not broaden this transport exception without a new reproduced protocol case.

## Current machine candidate

**v0.5.13 / code 20**, exact release source `ca11a45c0db170a9a2031193b9250663c6b5d914`.

Machine evidence:
- run `34270157522`, job `102209457841`, success;
- 89 JVM tests, 0 failures/errors/skips, including stale closed-stream regression;
- 46 source/API/security contracts passed;
- lint 0 errors / 26 warnings;
- package/version/signer/archive and nonvisual emulator install/cold/warm/no-fatal checks passed;
- APK SHA-256 `592d012d187d0089b269a48cf5a33065d74d3fd284839995ff6aa4e2662507b0`;
- APK size `2413118` bytes;
- signed artifact ID `10073578805`, ZIP SHA-256 `928be6e7bdfccdda47fc6a3c478d49cb597f6cce64623bf143192a9d653d7496`;
- test artifact ID `10073625187`, ZIP SHA-256 `f7a4e93f385533dbba2c675ffe44502a60c487014e73f8ed3273e49e5a8427f7`.

v0.5.13 is machine verified. Physical actuator proof remains pending.

## One-shot actuator proof boundary

The proof remains non-persistent and may write only:
- `settings put system nv_power_mode 0`;
- `settings put system nv_power_mode 1`.

It must verify downstream values, never write the four `persist.vendor.sys.phs.*` properties, always attempt restore after a Max attempt, and require final Optimized verification for PASS. No `setprop`, watchdog, boot reapply or persistent TURBO state is allowed until the proof passes physically.

## Next safe decision

Manually confirm Processor Mode is Optimized, install v0.5.13, rerun `PROVE NVIDIA MAX SWITCH`, and return the proof report. Only a physical PASS with verified final Optimized state allows `nv_power_mode` into persistent TURBO.
