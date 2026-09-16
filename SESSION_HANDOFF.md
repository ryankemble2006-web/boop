## Current Voice handoff - v201 built, Shield verification pending

Updated 2026-09-16. Owner remains `boop-hand-colour-v191`. Current verified-build receipt: `docs/handoffs/2026-09-16-v201-voice-verified-build.md`; preserve the collaborating install continuation and independent review alongside it.

**v201 is built and independently artifact-verified, not yet physically accepted by this recovery.** Built source `eff4b004176747bfa489f566a34357b52e8c0557`; full signed run `35080910395` SUCCESS; artifact `10439409429`, `BOOP-Unified-v201-Voice-Surface-Ownership`. APK SHA-256: `527907407d60787860cf9e1c1cd13b464db91f015f657eec92590f995d735c8e`. Direct APK verification confirms version 201, permanent BOOP signer and unchanged permission declarations versus v200.

The failing Voice screenshot was traced to a GONE canonical wrapper with a VISIBLE child eye surface. The narrow correction propagates existing modal visibility to that surface. The new regression failed before the change and passed afterward, including against the materialized source. Artwork, voice tuning/backends, Home/media code and saved settings are not changed by the correction. The inherited scope guard now explicitly allows only that reviewed renderer file; music checks were not weakened.

The earlier v200 installed-source gap is closed: live installed APK and downloaded signed artifact both hash to `bd0b80ab96e858ed12bcaa1c43b132f4b55a43c078630a7cebb80a9db34d5bf5`, built from `572e8d6cb4726951df0604e18a9a8e614d93a543`. Last direct installed-version read in this recovery still reported v200. Re-read before any installation to avoid racing the collaborating session; do not treat that observation as timeless.

Continue the already-reached joint human-test phase, not a voice redesign. The collaborating install handoff records Ryan's prior in-place Shield install request. After that coordinated update, confirm version/hash and capture Voice Settings: eyes absent on entry and through TEST VOICE, correct restoration on exit. Audible pitch/rate, selector consistency and cross-device propagation still need Ryan's acceptance. This documentation publication itself makes no installation or permission change. Keep source/builds on GitHub, use the already-connected bridge, and leave physical Pixel 10 untouched.

---

# Unified session handoff - 2026-09-16

Owner branch: `boop-hand-colour-v191`. Live GitHub is authoritative for the branch; distinguish its build receipts from fresh device observations.

## Latest joint-test observation: Voice screen still obstructed

Ryan resumed BOOP Voice Ideas at the **human-testing phase**, not a new design/build phase. His requested live Shield screenshot confirms full-size felt eyes covering the Voice name controls and much of the Pitch/Cadence slider area; TEST VOICE remains visible at the bottom. A read-only identity check confirms foreground `com.boop.alpha1/.MainActivity` and installed v200 (`1.2.200-uniform-tv-chrome-voice-demo`). The oversized-eyes blocker is still present; no fix, voice audio, selector behavior or cross-device acceptance is claimed.

Continue from `docs/handoffs/2026-09-16-voice-human-test-overlay-blocker.md`. Locate the actual v200 source/build receipt before any correction; the older v198 receipt below is not v200 provenance. Keep the human-test phase and the requirement to clear the visual obstruction before further voice work. This continuation captured/read only and updated documentation: no app code, settings, permissions, installs, signing, phones or local worktrees changed. Private screenshots/raw output were not published.

## Latest device maintenance: Shield storage cleanup

Ryan requested removal of old Shield clutter. Removed 103 obsolete downloaded APK copies, three hash-verified duplicate Forki installers, 84 old BOOP/Johnny/Rally captures and 14 old temporary diagnostics. Recovered approximately 1.92 decimal GB; final free space was 3,784,840 KiB, approximately 3.88 decimal GB (3.61 GiB), with 69% used. All 21 third-party package names and the Home/screensaver defaults were preserved. Five unclassified backup ZIPs and one original Forki installer remain on the Shield.

Direct inspection before and after cleanup showed **BOOP v200** (`1.2.200-uniform-tv-chrome-voice-demo`) and Johnny versionCode 15 (`1.14-reborn-ha-fast-poll`) already installed and unchanged. This task did not build, sign, install or physically accept v200. It did not modify app data, permissions, phones, source or local worktrees.

Full maintenance receipt: `docs/handoffs/2026-09-16-shield-storage-cleanup.md`.

Storage cleanup is complete; the originally blocked install was not retried. The v197/v198 installation statements and install-next-step below belong to the earlier build session. They are superseded for current Shield identity by this live v200 observation. Fetch the current feature receipt before any later explicitly requested install; **do not downgrade to an older candidate from this historical next-step paragraph**. No new v200 source/hash provenance or feature acceptance is claimed.

## Earlier build-session receipt: v198 voice profile controls

v198 (`1.2.198-voice-profile-controls`) adds **Voice pitch** and **Voice speed** sliders to Build a Boop and a separate opt-in Home Assistant voice-profile channel. The bounded `BOOP_VOICE_V1` profile carries Android/natural backend choice, Emma/Isabella/George/Fable natural voice key, pitch and speech rate. Natural voice model files remain local to each device and are never transferred through Home Assistant.

Built source `7a333039d127324d4de69d2a4b84e203fb61bbbc`; focused GREEN run `35071143878`; final signed full run `35071278286` SUCCESS. Artifact `BOOP-Unified-v198-Voice-Profile-Controls`, id `10436755628`, artifact digest `sha256:3df9ce6956c6b94078c13fd9b14a4279a478a069aef9262a9af25b981667e7da`.

The full workflow passed inherited accepted-character, lyrics, music/bass, startup, natural-voice, colour-sharing, animation-speed, Android compile, permanent-signer and APK identity checks. The first full run exposed only an inherited v169 production-change allowlist; actual music worker checks passed 1,444 assertions and the allowlist was expanded only for the six intentional v198 voice source files.

Natural Kokoro rate uses the shared rate. Natural Kokoro pitch remains deliberately unapplied to PCM playback until that Android audio path is physically proven. The pitch value is nevertheless saved/shared now and remains usable by Android TTS.

**Verification boundary recorded in that earlier session:** v198 was CI/build/signer green but had not been installed or physically accepted. The Shield's then-verified installed baseline was v197. Preserve v197 Home/Now Playing ownership behavior, the physically accepted v191 character/hand checkpoint, and locked felt default tag `boop-felt-default-v189`.

Full v198 receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.

The earlier next step was an explicitly requested v198 install followed by joint verification of slider feel and phone↔Shield natural voice/rate sharing. That install instruction is now historical; use the latest-device warning above. Do not claim natural pitch playback until separately proven.
