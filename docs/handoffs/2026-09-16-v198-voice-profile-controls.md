# BOOP v198 voice profile controls — 2026-09-16

Owner branch: `boop-hand-colour-v191`

## Goal
Put the existing puppet pitch/rate tuning into **Build a Boop** and let the selected natural voice profile follow BOOP between phone and Shield through the already paired Home Assistant, like the existing colour-sharing features.

## Implementation
- Build a Boop now exposes `Voice pitch` and `Voice speed` sliders using the existing `BoopVoiceTuning` ranges and `BoopVoiceController` persistence.
- A separate opt-in Home Assistant helper uses bounded `BOOP_VOICE_V1` payloads containing backend, supported natural voice key, pitch and speech rate.
- Supported natural choices remain Emma (`bf_emma`), Isabella (`bf_isabella`), George (`bm_george`) and Fable (`bm_fable`).
- `BoopVoiceController` refreshes the shared profile from preferences so an already-running BOOP can observe a profile received from another device.
- Natural voice model files remain local to each device. The large Kokoro pack is never copied through Home Assistant.
- Existing eye, felt and hand sharing are untouched; voice sharing has its own explicit opt-in and helper identity.

## Natural pitch boundary
Natural Kokoro speech already uses the shared rate. Natural pitch is still deliberately not applied to the Kokoro PCM playback path because that Android playback path has not yet been physically proven. The pitch value is saved and shared now; Android TTS can use it, and Kokoro pitch remains a separate follow-up rather than an unverified audio hack.

## TDD and CI
- Focused harness setup run `35070503480` failed because pytest was absent; this was fixed and was not counted as RED.
- Proper RED run `35070549095` failed on the intentionally missing v198 controls/sync contract.
- Focused GREEN run `35071143878` passed.
- First full run `35071143796` reached the inherited v169 production-change guard after its actual music worker passed 1,444 checks. The guard rejected only the six intentional v198 voice source files. Its allowlist was updated narrowly; no music implementation changed.
- Final built source: `7a333039d127324d4de69d2a4b84e203fb61bbbc`.
- Final signed workflow run: `35071278286`, SUCCESS.
- Artifact: `BOOP-Unified-v198-Voice-Profile-Controls`, id `10436755628`, artifact digest `sha256:3df9ce6956c6b94078c13fd9b14a4279a478a069aef9262a9af25b981667e7da`.
- Version: `198` / `1.2.198-voice-profile-controls`.

The full run passed the accepted character locks, lyrics, music/bass, startup lifecycle, existing natural-voice contract, eye/hand sharing, animation speed, Android compile, permanent signer, APK identity/bytecode checks and artifact upload.

## Preservation and verification limits
v197 Home/Now Playing ownership behavior is preserved. v191 hand/appearance remains the physically accepted character checkpoint and v189 remains the locked felt default. No emulator, device install or physical voice/share acceptance was performed for v198 in this work. CI/build evidence must not be described as device acceptance.

## Repository hygiene note
During connector probing, several trivial root scratch files were briefly created and immediately deleted. None remain in the tree and none contained secrets or project data. Their harmless create/delete commits remain in shared history because the branch was not force-rewritten.

## Next safe step
Only after Ryan explicitly requests installation, install the signed v198 artifact on the authorized test devices and jointly verify Build a Boop slider feel plus phone-to-Shield and Shield-to-phone natural voice/rate sharing. Treat natural Kokoro pitch playback as a separate follow-up unless and until its audio path is physically proven.
