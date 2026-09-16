# v200 voice controls — recovered build and human-test handoff

Date: 2026-09-16. Owner: `boop-hand-colour-v191`.
Reconciled with `fe9ee549ee9bccf1761750f8d4a85b0cd2f1b9f0`, preserving the concurrent voice-blocker and storage-cleanup handoffs.

## Current task

Resume BOOP Voice Ideas directly in chat at the human-testing phase. The ADB bridge is already connected; no Work-mode or bridge-start request is needed. The immediate blocker is large felt eyes covering Voice Settings. Do not restart voice design or downgrade to the historical v198 candidate.

The approved v200 design and implementation plan remain at `docs/superpowers/specs/2026-09-16-v200-uniform-tv-chrome-voice-demo-design.md` and `docs/superpowers/plans/2026-09-16-v200-uniform-tv-chrome-voice-demo.md`.

Requested outcomes: readable Home-style TV menu selectors; no full-screen eyes in Voice Settings; TEST VOICE below pitch/cadence controls using the current backend and slider values; natural pitch/rate; preserve phone/Shield voice-profile sharing and device-local voice models.

## Recovered GitHub build evidence

- Built source: `572e8d6cb4726951df0604e18a9a8e614d93a543`.
- Focused UI run: `35076396763`, success.
- Full signed run: `35076396821`, success; build job `104729721325`.
- All reported job steps succeeded, including inherited character/music/lyrics/startup/sharing gates, Android compile, permanent signer and APK bytecode/identity checks.
- Artifact: `BOOP-Unified-v200-Uniform-TV-Chrome-Voice-Demo`, id `10438621229`.
- Artifact ZIP digest: `sha256:f709996c3ad19fc55dabe6d7f4e7a639eac03a472e9b28aeaf289de5cf891ed2`; ZIP size 76,157,484 bytes. This is not the APK-file hash.

v200 implements global TV chrome, TEST VOICE and natural PlaybackParams pitch. The v198 statement that natural pitch is deliberately unapplied is historical; audible operation of the newer path remains unaccepted.

## Fresh device evidence and limits

ADB readback confirms Shield package `com.boop.alpha1`, versionCode 200 and versionName `1.2.200-uniform-tv-chrome-voice-demo`. A fresh screenshot of the already-open Voice screen shows large felt eyes obscuring controls, with Pitch, Cadence and TEST VOICE behind them. Surface inspection reports a MainActivity SurfaceView. Screenshot appearance alone does not establish the cause.

This recovery has not installed an APK, changed data/permissions, altered the natural pack, compared the installed APK hash with the artifact, or touched physical Pixel 10. Captures and raw device output remain private. Device identity is not proof of hidden eyes, uniform selectors, audible natural pitch/rate, or cross-device sharing.

## Next safe step

Preserve the already-reached joint-test phase. Diagnose the actual canonical renderer/materialization path, not merely the old FaceView or a matching source string. Repair only the confirmed ownership failure, with a focused regression before the full GitHub signed build. Do not redo completed slider/backend work or change artwork. A later installation requires Ryan's explicit authorization; memory publication alone does not authorize it.

Current detailed visual-blocker receipt: `docs/handoffs/2026-09-16-voice-human-test-overlay-blocker.md`. Preserve the same-day storage receipt, v197 Home/media checkpoint, accepted v191 hand/appearance sharing, locked felt default `boop-felt-default-v189`, and separate v199 favourites lab. Do not split Wall/Shield or merge another task's branch here.
