# Current candidate: v198 voice profile controls — 2026-09-16

Owner branch: `boop-hand-colour-v191`. Live GitHub is authoritative.

v198 (`1.2.198-voice-profile-controls`) adds **Voice pitch** and **Voice speed** sliders to Build a Boop and a separate opt-in Home Assistant voice-profile channel. The bounded `BOOP_VOICE_V1` profile carries Android/natural backend choice, Emma/Isabella/George/Fable natural voice key, pitch and speech rate. Natural voice model files remain local to each device and are never transferred through Home Assistant.

Built source `7a333039d127324d4de69d2a4b84e203fb61bbbc`; focused GREEN run `35071143878`; final signed full run `35071278286` SUCCESS. Artifact `BOOP-Unified-v198-Voice-Profile-Controls`, id `10436755628`, artifact digest `sha256:3df9ce6956c6b94078c13fd9b14a4279a478a069aef9262a9af25b981667e7da`.

The full workflow passed inherited accepted-character, lyrics, music/bass, startup, natural-voice, colour-sharing, animation-speed, Android compile, permanent-signer and APK identity checks. The first full run exposed only an inherited v169 production-change allowlist; actual music worker checks passed 1,444 assertions and the allowlist was expanded only for the six intentional v198 voice source files.

Natural Kokoro rate uses the shared rate. Natural Kokoro pitch remains deliberately unapplied to PCM playback until that Android audio path is physically proven. The pitch value is nevertheless saved/shared now and remains usable by Android TTS.

**Verification boundary:** v198 is CI/build/signer green but has not been installed or physically accepted. The Shield's verified installed baseline remains v197. Preserve v197 Home/Now Playing ownership behavior, the physically accepted v191 character/hand checkpoint, and locked felt default tag `boop-felt-default-v189`.

Full receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.

Next safe step: only on Ryan's explicit install request, install the signed v198 candidate on authorized test devices and jointly verify slider feel plus phone↔Shield natural voice/rate sharing. Do not claim natural pitch playback until separately proven.
