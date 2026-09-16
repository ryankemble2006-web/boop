# Current candidate: v198 voice profile controls — 2026-09-16

Owner: `boop-hand-colour-v191`.

## v198 status
- Version: `198` / `1.2.198-voice-profile-controls`.
- Built source: `7a333039d127324d4de69d2a4b84e203fb61bbbc`.
- Focused v198 contract: run `35071143878`, PASS.
- Full signed workflow: run `35071278286`, SUCCESS.
- Artifact: `BOOP-Unified-v198-Voice-Profile-Controls`, id `10436755628`.
- Artifact digest: `sha256:3df9ce6956c6b94078c13fd9b14a4279a478a069aef9262a9af25b981667e7da`.
- Device state: **not installed**. No physical v198 acceptance has been claimed.

## What changed
Build a Boop now has Voice pitch and Voice speed sliders using the existing voice-tuning ranges. A separate opt-in `BOOP_VOICE_V1` Home Assistant profile carries selected supported natural voice, backend, pitch and speech rate between BOOP devices. The natural voice pack remains local to each device.

Natural Kokoro speech uses the shared rate. Natural Kokoro pitch remains intentionally unapplied to the PCM playback path pending separate physical proof; the pitch setting is still persisted/shared and Android TTS can use it.

## Preserved checkpoints
- **Shield installed baseline:** v197 Home media handoff. Its signed build/install and live native-Deezer ownership checks remain valid; v198 has not replaced it on-device.
- **Accepted character:** v191 hand/appearance and sharing remain physically accepted.
- **Locked felt default:** tag `boop-felt-default-v189`.

Full current receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.
Historical release/acceptance details remain in their dated receipts rather than being duplicated here.

## Next
Wait for Ryan's explicit install request. Then install the signed v198 candidate on the authorized test devices and jointly verify Build a Boop slider feel and phone↔Shield profile sharing. Keep CI/build evidence, install identity and Ryan's physical acceptance as separate evidence levels.
