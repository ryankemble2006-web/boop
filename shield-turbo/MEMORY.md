# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is the concise view. Fresh physical Shield evidence wins over stale pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP.

Use only the established secret-backed signer `boop-dev`. Permanent certificate SHA-256:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Before Turbo edits, read BOOP startup docs plus this branch handoff/status/memory, fetch/check live Turbo and `main`, preserve concurrent work and never force-push.

## CLEAN START physical decision

The real current-user force-stop + read-back verification core is physically positive and must not be changed casually.

Stale Recents/task-manager cards may remain even though target apps are actually stopped and reload only when deliberately focused/launched. Normal manual launch remains allowed.

v0.5.8 physical timing established that CLEAN START itself is **sub-second** on Ryan's Shield:

`notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`

This disproves the hypothesis that Turbo's cleanup job caused the earlier subjectively felt ~10-second reboot disturbance. Do not parallelize, remove verification, weaken safety checks or otherwise optimize the 573ms path without new evidence. If the longer overall reboot/launcher settle matters later, instrument outside the job boundary.

## Static notice lock

Exact notice text remains:
- `SHIELD TURBO · CLEAN START`
- `Tidying startup apps`

It must remain top-centre, static, non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation, focus effect, artificial dwell or movement.

Presentation failure must fail open into cleanup.

### Physically accepted host architecture

v0.5.7 finally made the notice visible on Ryan's real Shield. He saw the blue/cyan notice for roughly one second; diagnostic reported permission YES, `DISPLAY_WINDOW_CONTEXT`, add `ADDED`, `FRAME_COMMITTED`, about 236ms.

Freeze the brightness-style host:
- transparent `FrameLayout`;
- `MATCH_PARENT x MATCH_PARENT` window;
- `TYPE_APPLICATION_OVERLAY`;
- `FLAG_NOT_FOCUSABLE`;
- `FLAG_NOT_TOUCHABLE`;
- `FLAG_LAYOUT_IN_SCREEN`;
- `FLAG_LAYOUT_NO_LIMITS`;
- `FLAG_HARDWARE_ACCELERATED`;
- static card as top-centre child;
- attach-gated Android 10+ frame-commit diagnostics;
- maximum 500ms presentation wait/fail-open.

Do not return to the old small `WRAP_CONTENT` overlay and do not add sleep/preroll timing hacks.

Presentation history:
- v0.5.1 flashed only at the end;
- v0.5.2 invisible;
- v0.5.3 invisible and nearly eight seconds;
- v0.5.4 invisible but fast navigation restored;
- v0.5.5 invisible, `DRAWN` ~54ms exposed false-positive draw evidence;
- v0.5.6 invisible despite `FRAME_COMMITTED` ~103ms, small-window architecture rejected;
- v0.5.7 full-screen host physically visible, `FRAME_COMMITTED` ~236ms;
- v0.5.8 unchanged host plus timing diagnostics, whole Turbo job 573ms.

## CLEAN START safety retained

- AUTO CLEAN START remains opt-in and bounded.
- Non-exported boot receiver plus one-shot JobService only when enabled and targets exist.
- Attempts remain roughly 30s, 60s and 120s after boot, maximum three.
- No periodic/resident cleaner, foreground service or indefinite retry.
- Boot cleanup uses only already-trusted loopback ADB and never requests a fresh RSA approval.
- ADB key stays private in `noBackupFilesDir`.
- Only eligible non-system user apps are targets.
- Preserve BOOP/Android/NVIDIA/Google-core/system and updated-system exclusions.
- Current resumed app is skipped.
- Background-only playback is not independently detected.
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
- Android fixed-performance mode is not automatically enabled in v1 because it is not guaranteed to be maximum dynamic performance;
- TURBO persists across reboot;
- the exact pre-Turbo NORMAL snapshot is saved before any write, survives reboot reapply unchanged, and is preserved until restore verification succeeds;
- a foreground thermal watchdog runs only while TURBO is active;
- boot reapply must verify current thermal state before any performance write;
- SEVERE or higher thermal status causes immediate transactional restore to NORMAL, persists NORMAL and never auto-reenables TURBO;
- ambiguous or failed state prefers NORMAL or explicit recovery and must not blindly reapply TURBO;
- no frequency above firmware-exposed limits, voltage modification, thermal/throttling bypass, root, custom kernel, boot image or bootloader modification.

The design is approved in chat and self-reviewed in the committed spec. Implementation is still pending Ryan's written-spec review. After approval, implementation begins with a read-only capability build and adds writes only from physical evidence one control at a time.

## Latest candidate and verification

Latest candidate: **v0.5.8 / code 15**, exact source `ea2c290b5ca66c6a88f1967db91b741e278007b7`.

Release run `34242340853`, job `102115431784`, success:
- 69 JVM tests, 0 failures/errors/skips;
- 29 source/API/security contracts passed;
- lint 0 errors / 24 warnings;
- package/version/signer/archive checks passed;
- nonvisual cold/warm launch/no-fatal smoke passed;
- no visual tests ran.

Artifact `SHIELD-TURBO` ID `10062615402`, ZIP SHA-256 `de9d9788992c65de8e665e23d850c97d59d6a9206b9046c8ba05f65bf975c468`.
Test artifact `SHIELD-TURBO-TESTS` ID `10062679313`, SHA-256 `1d36483400561023311e9f9823ae631ce699063117c8c26ca91063f345fd78f3`.
APK `Shield-Turbo-v0.5.8.apk`, `2337506` bytes, SHA-256 `e8d61d98fc5b4603c810246babbdb1c1937ae0942b2ea5aad0a8ce44e5fdcb66`.

v0.5.8 TDD lineage:
- RED timing contract `a35660fb2d52aacecf402d3b062cf065b943019c`;
- store round-trip test `d9b3798c90c6bc78655f5b7b6d914be7a748f470`;
- corrected RED test setup `1dba6db8990936812f0be1da2cc51cf0b0e2e658`;
- GREEN instrumentation `89a6d4bc6e3a9878c7f8c20f2b7902740684fe12`;
- atomic v0.5.8 stamp `ea2c290b5ca66c6a88f1967db91b741e278007b7`.

## Testing boundary

Ryan owns all real-device visuals, remote behavior and physical timing. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment. Allowed gates remain functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

## Next safe decision

Ryan reviews the written persistent stock-TURBO spec. If approved, create the implementation plan and begin TDD with Stage 1 read-only capability discovery. No performance write is justified before capability evidence.

A temporary one-word placeholder file was accidentally created during earlier documentation preparation and immediately deleted in normal history. Commit `c777ea2f14b7b7cd26a9f397e25d29fcba07a052` restores the exact v0.5.8 release tree SHA `1c3320acc6921fb1140a8c5997f284f06444f24f`. No app code/private data was involved and no history was rewritten.
