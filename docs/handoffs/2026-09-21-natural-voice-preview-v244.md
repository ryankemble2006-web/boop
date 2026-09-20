# Natural voice preview latency: Shield244 / Wall211

## Current: fast natural voice previews installed on Shield244 / Pixel7 Wall211

Ryan requested usable puppety natural-voice tuning after a roughly 12-second delay from voice names or TEST VOICE. The old installed Shield243 reproduced 16,128 ms to the first Emma playback (9,414 ms model load plus 6,672 ms synthesis); warm TEST VOICE took 5,266 ms. The new fixed demos play immediately from neutral PCM generated with the same pinned Kokoro voices. Both demos and newly generated natural replies apply saved pitch and cadence at playback. Slider ranges and stored settings are unchanged.

Signed source `c857e677cd7c4594a90d5efd88bde44a89dfde34`; successful run `35543446156`; artifact `10614884729` (`BOOP-Shield-v244-Wall-v211-Signed`). 125 local checks and 15 speech lifecycle scenarios passed, followed by the complete inherited/signed CI. Independent review caught a natural-speech pause/resume wake-state bug; a new test reproduced it, then passed after the fix. Native model release is serialized after inference; preview playback does not queue behind it. Interrupted or stale previews cannot select the wrong voice.

Both permanent-signed APKs installed with `adb install -r`; versions244/211 read back. Profiles immediately before/after installation matched exactly. Official optional voice packs, initially absent in these split installs, are now installed through the existing verified download flow. All18 prior assets and16 native libraries per APK remain byte-identical; additions are eight demo PCM clips and their provenance/license files. APKs and receipts are retained in task outputs and Desktop/APKBOOP;243/210 rollbacks remain.

Actual named-button and TEST VOICE checks covered all four voices on both devices, plus rapid replacement. First post-install Shield Emma started playback in49 ms and TEST VOICE in13 ms; phone first TEST VOICE in21 ms. Final traces contain17 Shield playback starts at10-49 ms and27 phone starts at15-55 ms, with no neural synthesis, natural-voice failure or AndroidRuntime error in those preview traces. Rapid-switch test sequences ended at Emma; subsequent live user tuning is retained. Shield also returned through Home and played TEST VOICE in13 ms. These are request-to-AudioTrack-start measurements, not microphone-to-command or acoustic acceptance. Temporary device helpers were removed.

Ordinary new reply text still requires local neural synthesis and may incur cold model-loading latency; this change does not claim that arbitrary Kokoro speech is now real time. Cross-device voice-profile sharing and physical sound preference remain separate acceptance topics. Details: `docs/handoffs/2026-09-21-natural-voice-preview-v244.md`.

- `BOOP-Wall-v211.apk`: 161163401 bytes; SHA-256 `982489b21f9193f01db3e7ae0543c36a904ddf94be3b605b8742368370406762`.
- `BOOP-Shield-v244.apk`: 161163517 bytes; SHA-256 `80cabc534dd246e17cbbae9845479bd162ca0f170cdb4fc588c58fa4c87d39dd`.

Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

[Successful signed build](https://github.com/ryankemble2006-web/boop/actions/runs/35543446156).
