# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is the concise view. Fresh physical Shield evidence wins over stale pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP.

Use only the established secret-backed signer `boop-dev`. Permanent certificate SHA-256:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Before Turbo edits, read BOOP startup docs plus this branch handoff/status/memory, fetch/check live Turbo and `main`, preserve concurrent work and never force-push.

## CLEAN START physical decision

The current-user force-stop + read-back verification core is physically positive and must not be changed casually.

v0.5.8 physical timing established that CLEAN START itself is **sub-second** on Ryan's Shield:

`notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`

Do not parallelize, remove verification, weaken safety checks or otherwise optimize that path without new physical evidence.

### Static notice lock

Exact notice text remains:
- `SHIELD TURBO · CLEAN START`
- `Tidying startup apps`

It must remain top-centre, static, non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation, focus effect, artificial dwell or movement.

Physically accepted host:
- transparent full-screen `FrameLayout`;
- `MATCH_PARENT x MATCH_PARENT` window;
- `TYPE_APPLICATION_OVERLAY`;
- non-focusable/non-touchable/in-screen/no-limits/hardware-accelerated flags;
- static card as top-centre child;
- attach-gated Android 10+ frame-commit diagnostics;
- maximum 500ms presentation wait/fail-open.

## CLEAN START safety retained

- AUTO CLEAN START remains opt-in and bounded.
- Non-exported boot receiver plus one-shot JobService only when enabled and targets exist.
- Attempts remain roughly 30s, 60s and 120s after boot, maximum three.
- No periodic cleaner or indefinite retry.
- Boot cleanup uses only already-trusted loopback ADB and never requests a fresh RSA approval.
- ADB key stays private in `noBackupFilesDir`.
- Only eligible non-system user apps are targets.
- Preserve BOOP/Android/NVIDIA/Google-core/system and updated-system exclusions.
- Current resumed app is skipped.
- HARD BLOCK stays separate and explicit.
- Preserve old StartupLedger undo records.
- No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing or fake RAM score.

Keep the physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and remote-first behavior unchanged. Display & Sound and Accessibility remain parked.

## Persistent stock TURBO mode decision

Ryan explicitly approved a persistent no-root performance mode for demanding workloads such as Dolphin.

Canonical design:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Durable rules:
- stock-envelope performance tuning is allowed;
- NVIDIA Max Performance is the preferred first lever when the target firmware exposes a verifiable read/write interface;
- additional CPU/GPU/governor controls are allowed only when stock-exposed, reviewed/allowlisted, within firmware-reported supported limits, and have complete save/write/read-back/restore/read-back semantics;
- Android fixed-performance is not automatically enabled without evidence that it is useful and exposed;
- TURBO persists across reboot once a real write path exists;
- exact pre-Turbo NORMAL snapshot is saved before any write and preserved until restore verification succeeds;
- foreground thermal watchdog runs only while TURBO is active;
- boot reapply verifies thermal state before any performance write;
- SEVERE or higher thermal status causes immediate transactional restore to NORMAL, persists NORMAL and never auto-reenables TURBO;
- ambiguous or failed state prefers NORMAL/recovery and must not blindly reapply TURBO;
- no frequency above firmware-exposed limits, voltage modification, thermal/throttling bypass, root, custom kernel, boot image or bootloader modification.

## Stage-1 physical capability decision

v0.5.10 implemented the first read-only capability pass and Ryan physically captured the report on the target Shield.

Treat these as current device facts until newer physical evidence replaces them:
- trusted local ADB works; capability tier is `ADB TURBO`;
- Android PowerManager thermal status is exposed and reported status 0 / None in the captured test;
- no reviewed allowlisted CPU stock sysfs control was exposed;
- no reviewed allowlisted GPU stock sysfs control was exposed;
- no allowlisted performance-write path was exposed;
- `cmd power help` did not expose Android fixed-performance mode;
- generic processor-mode clue matches such as `low_power`, `nvidia_ranger_enabled`, `power_sounds_enabled` and `sys_uidcpupower` are NOT sufficient evidence of NVIDIA Processor Mode control;
- observed CPU frequency readings are point-in-time diagnostics, not maximum-clock proof.

Therefore the next stage is not random sysfs experimentation. It is targeted **read-only** discovery of how SHIELD Settings represents/applies **Processor mode → Max performance**. No performance write is allowed until a candidate interface has unambiguous meaning, read-back, and a complete restore path.

## Compact ANALYSE report decision

v0.5.10 physically solved the evidence-capture problem. Ryan's real-Shield screenshot shows the entire report in one frame while remaining readable.

Preserve for the diagnostic report:
- full-screen black sheet;
- 9sp monospace report body and tight line spacing;
- one ProbeResult per logical line;
- embedded evidence newlines flattened to ` | ` without dropping evidence;
- Back closes the report;
- normal Shield UI underneath remains chunky/remote-first.

Do not re-expand the screenshot report into large result cards unless Ryan explicitly asks to abandon the one-screen evidence format.

## Latest candidate and verification

Latest candidate: **v0.5.10 / code 17**, exact source `d7ed55d5437b2fb534b57a0015b3f79341d1eeee`.

Release run `34256065457`, job `102162210965`, success:
- 75 JVM tests, 0 failures/errors/skips;
- 34 source/API/security contracts passed;
- lint 0 errors / 26 warnings;
- package/version/signer/archive checks passed;
- nonvisual emulator install/cold/warm launch/no-fatal smoke passed;
- APK SHA-256 `e4cadc57006d344c2ef7cad557a4a2535b8e1d493b3a80f239cb991c15480b88`;
- APK size `2364850` bytes;
- artifact `SHIELD-TURBO` ID `10068029156`, ZIP SHA-256 `30ce88f727272dd78c9bfdea41d1427041450b48232be78f7040215dbc88ad04`;
- test artifact `SHIELD-TURBO-TESTS` ID `10068084283`, ZIP SHA-256 `1d8e76038a2d5cc5887d5ef9186df7a6f22cd3d9e1184244446db30cff437512`;
- Ryan physically accepted the compact report as one-screen/readable. No automated visual acceptance was used.

## Testing boundary

Ryan owns all real-device visuals, remote behavior and physical timing. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment. Allowed gates remain functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

## Next safe decision

Deeper read-only NVIDIA Processor Mode discovery, specifically comparing the real Shield state around Optimized versus Max Performance. Ignore generic power-key name matches. No write before unambiguous state/read-back/restore semantics exist.
