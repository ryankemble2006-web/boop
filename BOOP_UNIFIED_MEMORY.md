## Recovered v200 voice context - 2026-09-16

Current receipt: `docs/handoffs/2026-09-16-v200-uniform-tv-chrome-voice-demo.md`. This resolves the missing v200 source/build reference in the earlier notes without changing their failing visual verdict. Built source `572e8d6cb4726951df0604e18a9a8e614d93a543`; focused run `35076396763` and full signed run `35076396821` succeeded; artifact `10438621229`. Fresh Shield readback confirms version 200; installed APK/artifact hash equality has not yet been checked in this recovery.

**Current phase: joint human testing, blocked by large eyes over Voice Settings.** TEST VOICE and tuning controls exist; visible surface ownership is not fixed/accepted. v200 implements natural pitch playback, superseding the historical v198 implementation limit; audible pitch/rate, selector consistency and phone-to-Shield profile propagation still need acceptance. Diagnose and narrowly repair the blocker rather than restarting design, redoing completed voice work or downgrading.

Ryan requested continuation directly in chat with the already-connected ADB bridge. Follow current main `BOOP_START_HERE.md`: GitHub source/tests/build/signing; agreed runtime work through the existing bridge; no Work-mode prompt, emulator gate, permission changes, data clear, signing substitution or physical Pixel 10 work. Preserve the sister session's visual-blocker handoff, storage cleanup and all content below. This update is documentation-only; it does not authorize an installation or app split.

---

# BOOP Unified durable memory - current only

Updated 2026-09-16. Live GitHub branch `boop-hand-colour-v191` is authoritative for this branch's source/build receipts; dated receipts retain detailed history. Fresh device identity and source/build provenance are separate evidence.

## Current Voice continuation: human-testing phase

Ryan resumed BOOP Voice Ideas after the chat malfunctioned. The work had already reached human testing. His requested screenshot on the existing Shield ADB connection confirms large photographic felt eyes covering the Voice name controls and much of the Pitch/Cadence sliders; TEST VOICE remains visible below. Fresh read-only identity confirms `com.boop.alpha1/.MainActivity`, versionCode 200, `1.2.200-uniform-tv-chrome-voice-demo`.

Preserve the prior requirement to clear the full-screen eyes before continuing other voice work, use readable Home-style selectors across Shield/Android TV menus, and expose TEST VOICE with the tuning controls. None of those behavior checks is newly accepted here. This is a failing visual checkpoint, not proof of the renderer/service responsible or of input interception.

Continuation/context receipt: `docs/handoffs/2026-09-16-voice-human-test-overlay-blocker.md`. Locate the actual v200 source/build receipt before a correction; the historical v198 receipt below does not supply v200 provenance. Do not restart design, downgrade, ask for an already-connected bridge or push this task into Work mode. The continuation performed captures/identity reads and documentation updates only, with no app source, settings, permissions, installs, signing or phone operations. Screenshots and raw device output remain private.

## Latest Shield observation: storage cleanup

Ryan requested old Shield clutter be removed after a storage-blocked install. The cleanup removed 103 obsolete downloaded APK copies, three hash-identical duplicate Forki installers, 84 old BOOP/Johnny/Rally captures and 14 temporary diagnostic files. Approximately 1.92 decimal GB was recovered; the final measurement was 3,784,840 KiB free, approximately 3.88 decimal GB (3.61 GiB), with 69% used. All 21 third-party package names and the Home/screensaver defaults were unchanged. Five unclassified ZIP backups and one original Forki installer were retained on the Shield.

The live Shield already had **BOOP v200** (`1.2.200-uniform-tv-chrome-voice-demo`) and Johnny versionCode 15 (`1.14-reborn-ha-fast-poll`) when inspected; their version/update metadata remained unchanged through cleanup. This maintenance task did not build, sign, install or physically accept v200, and did not alter app data, permissions, phones or local source/worktrees. The blocked installer was not retried.

Receipt: `docs/handoffs/2026-09-16-shield-storage-cleanup.md`.

This direct device observation supersedes the earlier v197/v198 statements below for current installed identity only. Do not downgrade using an old receipt's next step. Read the newest relevant feature/source receipt before a later explicitly requested install; do not invent v200 source/hash provenance from its version string.

## Earlier voice/personality build receipt
v198 (`1.2.198-voice-profile-controls`) was signed and CI-green but not installed or physically accepted at the earlier build-session receipt. Built source `7a333039d127324d4de69d2a4b84e203fb61bbbc`; final run `35071278286`; artifact `BOOP-Unified-v198-Voice-Profile-Controls` id `10436755628`.

Build a Boop exposes Voice pitch and Voice speed using the existing `BoopVoiceTuning` ranges. Optional voice-profile sharing uses its own bounded `BOOP_VOICE_V1` Home Assistant helper and carries backend, Emma/Isabella/George/Fable natural voice key, pitch and speech rate. Voice model files stay local to each device and are never transferred through Home Assistant.

Natural Kokoro rate follows the shared speech-rate value. Natural Kokoro pitch is intentionally not applied to the PCM playback path until that Android audio path is physically proven. Preserve this boundary rather than pretending the natural pitch slider is audible today.

Receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.

## Preserved device and character checkpoints
- v197 is the earlier verified Shield behavior checkpoint for Home/Now Playing ownership. It hides/releases the large Home GL puppet while Now Playing owns the media puppet and restores it when media disappears. Receipt: `docs/handoffs/2026-09-16-v197-home-media-handoff.md`. This historical checkpoint is not a claim that v197 is still installed; see the v200 observation above.
- v191 hand appearance and cross-device hand sharing remain physically accepted.
- The photographic felt default remains locked at tag `boop-felt-default-v189`; unrelated work must not regenerate or replace it.
- Preserve current lyrics, playback controls, music/bass behavior, animation speed and shared eye/felt/hand choices unless Ryan explicitly changes them.

## Working evidence rule
Keep BOOP source edits, non-visual tests, builds, signing and durable handoffs on GitHub. Use the smallest relevant regression first, then the full GitHub Actions build. Installation identity is separate from runtime behavior, and both are separate from Ryan's physical acceptance. Do not install a candidate until Ryan explicitly requests it.
