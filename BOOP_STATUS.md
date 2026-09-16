## Current Voice handoff - v201 built, Shield verification pending

Updated 2026-09-16. Owner remains `boop-hand-colour-v191`. Current verified-build receipt: `docs/handoffs/2026-09-16-v201-voice-verified-build.md`; preserve the collaborating install continuation and independent review alongside it.

**v201 is built and independently artifact-verified, not yet physically accepted by this recovery.** Built source `eff4b004176747bfa489f566a34357b52e8c0557`; full signed run `35080910395` SUCCESS; artifact `10439409429`, `BOOP-Unified-v201-Voice-Surface-Ownership`. APK SHA-256: `527907407d60787860cf9e1c1cd13b464db91f015f657eec92590f995d735c8e`. Direct APK verification confirms version 201, permanent BOOP signer and unchanged permission declarations versus v200.

The failing Voice screenshot was traced to a GONE canonical wrapper with a VISIBLE child eye surface. The narrow correction propagates existing modal visibility to that surface. The new regression failed before the change and passed afterward, including against the materialized source. Artwork, voice tuning/backends, Home/media code and saved settings are not changed by the correction. The inherited scope guard now explicitly allows only that reviewed renderer file; music checks were not weakened.

The earlier v200 installed-source gap is closed: live installed APK and downloaded signed artifact both hash to `bd0b80ab96e858ed12bcaa1c43b132f4b55a43c078630a7cebb80a9db34d5bf5`, built from `572e8d6cb4726951df0604e18a9a8e614d93a543`. Last direct installed-version read in this recovery still reported v200. Re-read before any installation to avoid racing the collaborating session; do not treat that observation as timeless.

Continue the already-reached joint human-test phase, not a voice redesign. The collaborating install handoff records Ryan's prior in-place Shield install request. After that coordinated update, confirm version/hash and capture Voice Settings: eyes absent on entry and through TEST VOICE, correct restoration on exit. Audible pitch/rate, selector consistency and cross-device propagation still need Ryan's acceptance. This documentation publication itself makes no installation or permission change. Keep source/builds on GitHub, use the already-connected bridge, and leave physical Pixel 10 untouched.

---

# BOOP status - 2026-09-16

Owner: `boop-hand-colour-v191`.

## Latest feature status: Voice human testing blocked

The requested live Shield screenshot confirms oversized full-screen felt eyes over Voice settings, obscuring the name controls and much of Pitch/Cadence. TEST VOICE is visible below. Read-only device identity confirms `com.boop.alpha1` versionCode 200, `1.2.200-uniform-tv-chrome-voice-demo`, with MainActivity foreground.

This is a confirmed failing visual observation, not physical acceptance. No fix, voice playback, selector or sync test was performed. Resume the already-reached human-testing phase and address the obstruction before further voice work. Locate the actual v200 source/build receipt before corrections; do not infer it from the older v198 receipt. Details/context: `docs/handoffs/2026-09-16-voice-human-test-overlay-blocker.md`. This continuation only captured/read the device and updated documentation; no code, settings, permissions, installs or signing changed.

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
Voice human testing is currently blocked by the oversized eyes; use the latest feature-context receipt above rather than restarting design or using an old install-next-step. Storage maintenance is complete and no additional install is implied. Before any correction, fetch the actual v200 source/build handoff and preserve the installed app unless Ryan explicitly requests a verified replacement. Jointly verify behavior with Ryan; keep CI/build evidence, install identity and physical acceptance separate.
