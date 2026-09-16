# BOOP Unified durable memory — current only

Updated 2026-09-16. Live GitHub branch `boop-hand-colour-v191` is authoritative for the combined app; dated receipts retain detailed history.

## Current voice/personality candidate
v198 (`1.2.198-voice-profile-controls`) is signed and CI-green but not installed or physically accepted. Built source `7a333039d127324d4de69d2a4b84e203fb61bbbc`; final run `35071278286`; artifact `BOOP-Unified-v198-Voice-Profile-Controls` id `10436755628`.

Build a Boop exposes Voice pitch and Voice speed using the existing `BoopVoiceTuning` ranges. Optional voice-profile sharing uses its own bounded `BOOP_VOICE_V1` Home Assistant helper and carries backend, Emma/Isabella/George/Fable natural voice key, pitch and speech rate. Voice model files stay local to each device and are never transferred through Home Assistant.

Natural Kokoro rate follows the shared speech-rate value. Natural Kokoro pitch is intentionally not applied to the PCM playback path until that Android audio path is physically proven. Preserve this boundary rather than pretending the natural pitch slider is audible today.

Receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.

## Preserved device and character checkpoints
- v197 remains the verified installed Shield baseline for Home/Now Playing ownership. It hides/releases the large Home GL puppet while Now Playing owns the media puppet and restores it when media disappears. Receipt: `docs/handoffs/2026-09-16-v197-home-media-handoff.md`.
- v191 hand appearance and cross-device hand sharing remain physically accepted.
- The photographic felt default remains locked at tag `boop-felt-default-v189`; unrelated work must not regenerate or replace it.
- Preserve current lyrics, playback controls, music/bass behavior, animation speed and shared eye/felt/hand choices unless Ryan explicitly changes them.

## Working evidence rule
Keep BOOP source edits, non-visual tests, builds, signing and durable handoffs on GitHub. Use the smallest relevant regression first, then the full GitHub Actions build. Installation identity is separate from runtime behavior, and both are separate from Ryan's physical acceptance. Do not install a candidate until Ryan explicitly requests it.
