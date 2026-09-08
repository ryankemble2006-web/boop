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

## v0.5.12 one-shot actuator proof decision

Current machine-verified candidate is **v0.5.12 / code 19**, exact release source `47ef633c1dde937ea9bd94a8e9a7182272c9b9b2`, run `34266282069`, job `102196408374`.

Machine evidence:
- 88 JVM tests, 0 failures/errors/skips;
- 46 source/API/security contracts passed;
- lint 0 errors / 26 warnings;
- package/version/signer/archive checks passed;
- emulator install/cold/warm launch/no-fatal smoke passed;
- APK SHA-256 `f219eff88054e9776c2b96af985dd68d94ef30ffc1a64a09d7cc4c3aadceb1a8`;
- signed artifact ID `10072080338`, ZIP SHA-256 `64e4ff80feaea554efa87ab4caeb4ccff8305dd1e9ddefc8e9d566ab79569f59`;
- test artifact ID `10072132751`, ZIP SHA-256 `d7bec2515be014b7814553e9906356a2b2f0cdde741733cec4fcfc0670914227`.

v0.5.12 exposes `PROVE NVIDIA MAX SWITCH`. It is a bounded one-shot proof, not persistent Turbo:
- require baseline `1` + downstream `0/0/0/15`;
- write only `settings put system nv_power_mode 0`;
- verify `0` + downstream `5/5/5/20`;
- restore only `settings put system nv_power_mode 1`;
- verify final `1` + downstream `0/0/0/15`;
- report exact evidence and `DIRECT VENDOR WRITES • NONE`.

No `setprop`, vendor-property direct writes, service, receiver, watchdog, boot reapply or persistent Turbo state is permitted in this proof. If baseline mismatches, nothing changes. If Max verification fails, restore is still attempted. A final restore mismatch is a physical FAIL.

Only after Ryan physically gets PASS with final Optimized restoration may `nv_power_mode` enter the persistent TURBO allowlist.

## Next safe decision

Ryan installs v0.5.12, sets Processor Mode to Optimized, runs `PROVE NVIDIA MAX SWITCH`, and returns the proof report. Do not implement persistent TURBO until that real-device evidence is positive.
