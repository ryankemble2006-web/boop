# BOOP status — 2026-09-16

Owner: `boop-hand-colour-v191`.

## Latest device observation: storage maintenance complete

The requested Shield cleanup removed 204 obsolete installer/diagnostic files without uninstalling any app or clearing app data. Fresh ADB readings measured approximately 1.92 decimal GB recovered and 3.88 decimal GB free (3,784,840 KiB; 3.61 GiB), reducing usage from 85% to 69%. The same 21 third-party packages and unchanged Home/screensaver defaults were verified. Five unclassified ZIP backups and one original Forki installer were retained.

BOOP versionCode **200**, `1.2.200-uniform-tv-chrome-voice-demo`, was already installed when inspected and remained unchanged. Johnny versionCode 15, `1.14-reborn-ha-fast-poll`, also remained unchanged. This is device-identity evidence, not a new build/signing/deployment or physical feature-acceptance receipt. The originally blocked installer was not retried.

Full cleanup receipt: `docs/handoffs/2026-09-16-shield-storage-cleanup.md`.

The v197/v198 device statements below describe the earlier build session, not the current observed Shield installation. Do not downgrade from its historical install-next-step. Fetch the newest relevant feature receipt before any future explicitly requested installation.

## Earlier v198 build status
- Version: `198` / `1.2.198-voice-profile-controls`.
- Built source: `7a333039d127324d4de69d2a4b84e203fb61bbbc`.
- Focused v198 contract: run `35071143878`, PASS.
- Full signed workflow: run `35071278286`, SUCCESS.
- Artifact: `BOOP-Unified-v198-Voice-Profile-Controls`, id `10436755628`.
- Artifact digest: `sha256:3df9ce6956c6b94078c13fd9b14a4279a478a069aef9262a9af25b981667e7da`.
- Device state recorded at that earlier receipt: **not installed**. No physical v198 acceptance was claimed there.

## What v198 changed
Build a Boop gained Voice pitch and Voice speed sliders using the existing voice-tuning ranges. A separate opt-in `BOOP_VOICE_V1` Home Assistant profile carries selected supported natural voice, backend, pitch and speech rate between BOOP devices. The natural voice pack remains local to each device.

Natural Kokoro speech uses the shared rate. Natural Kokoro pitch remains intentionally unapplied to the PCM playback path pending separate physical proof; the pitch setting is still persisted/shared and Android TTS can use it.

## Preserved checkpoints
- **Earlier verified Shield behavior:** v197 Home media handoff. Its signed build/install and live native-Deezer ownership checks remain historical evidence; see the latest observed v200 installation above.
- **Accepted character:** v191 hand/appearance and sharing remain physically accepted.
- **Locked felt default:** tag `boop-felt-default-v189`.

Full v198 receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.
Historical release/acceptance details remain in their dated receipts rather than being duplicated here.

## Next
Storage maintenance is complete. No additional install is implied. For subsequent work, fetch the newest relevant source/build handoff and preserve the observed v200 installation unless Ryan explicitly requests a verified replacement. Jointly verify feature behavior with Ryan; keep CI/build evidence, install identity and physical acceptance separate.
