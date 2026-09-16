# BOOP Unified durable memory — current only

Updated 2026-09-16. Live GitHub branch `boop-hand-colour-v191` is authoritative for this branch's source/build receipts; dated receipts retain detailed history. Fresh device identity and source/build provenance are separate evidence.

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
