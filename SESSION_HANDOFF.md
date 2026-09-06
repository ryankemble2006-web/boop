# Shield handoff — 2026-09-07

## Cancelled: voice-to-Deezer Cast bridge

Ryan rejected the proposed Music Assistant / Deezer / Cast route before any app or
Home Assistant configuration work began: it requires provider installation,
Deezer authorization and a target-binding setup, so it fails the project's
zero-setup, dad-friendly bar. Do not revive it as a hidden setup wizard, a PC
service, a Google Assistant fallback or a generic-Cast workaround. The temporary
design and implementation-plan documents were deliberately removed. No source,
APK, Android permission, device setting, Home Assistant configuration, Deezer
account or playback state changed. BOOP's existing Deezer observer/puppet remains
unchanged.

The durable product requirement is now: BOOP must provide useful music control
without requiring a separate music-server installation, account authorization or
per-user destination setup. Google Assistant is being removed; do not depend on
it or silently delegate to it.

Owner: Ryan's laptop task. Branch:boop-shield-media-puppetry.
Latest application code:4fe28a490d4a6a6954cb94e3cf8a2bbaf8ccb075.
Signed APK build: https://github.com/ryankemble2006-web/boop/actions/runs/34014467071
APK SHA256:DF1458FD2B2C6139B92EFC10895FA8A03189A841E8CB140C6E06D9CCE033781A.
This sync adds context and the existing approved motion preview, not new runtime
behaviour. Generated debug APKs are local verification, not stable update APKs.

## Physical versus automated evidence

Ryan confirmed H1 dances, pauses with Deezer and the lower placement is
"awesome placement". Latest pivot25% across/50% down, same art/size/nod.
This does not seal all lifecycle/HDR/soak/RTL/layout/skip cases.
Existing Home/Routines physical checkpoints remain protected.
Fresh42 source tests,154 Android tests (forced rerun) and local build PASS.
Preview clock and four RGBA asset checks PASS. Lint not rerun/claimed green:
previously accepted baseline3 errors/22 warnings remains documented.

## Fanart investigation — paused, not solved

MediaSession field/URI presence did not distinguish a confirmed paused
curtain/fanart pair. Album cover is not full-screen fanart.
Private narrow asset-reference inspection found an internal nullable background
image model and an associated Player background picture description. Two scoped
external UI-tree reads of the user-prepared paused fanart screen traversed72
Deezer nodes; no exact or combined description contained that label.
Do not infer curtains from absence, or impossibility of all other approaches.
No usable app-level fanart signal, automatic positioning or foreground minimise.
Temporary readers were removed; no lasting new permissions/settings from them.
Third-party APKs/decompiled files, raw diagnostics and personal media stay local.

## Next work, when requested

- Settings UI / missing living-room inventory: user raised it, not investigated.
- Foreground-aware minimise/return while Deezer plays behind Kodi: unimplemented.
- Minimal-button first-start access guidance: unimplemented; consent retained.
- P1 popcorn is approved preview only. Armin hand puppets and any-Queen Freddie
  are ideas; do Deezer checkpoint work before adding them.
- Respect the user's choice to keep Android animations enabled.
- Read BOOP_START_HERE.md; Wall voice work is on its own branch, not this
  branch's inherited Alpha companion source.
