# Unified v162 native lyrics integration

**Goal:** Integrate the tested native lyrics and smaller footer into accepted Unified161 as162; replace the old Now Playing Lyrics macro, then retire the Lab only after confirmation.
**Unified base:**593ad609ff87f651d5273bd17f5a2c0ca3ef5198.
**Lyrics input:**5ce581be6f1da10eb47640c4c11636a4bfa9e330, lab footer sourcee6f9bbb736ac90287815bea3a9c48496505675ed.
**Integration branch:**boop-unified-native-lyrics-v162.
**Current owner:**boop-unified-eye-sync-safe-v159 after PR10 merge0908d6955c90978e97dcbae9031f3f1de638bd9d.
**Architecture:** exact copied native shared path and internal Activity; use Unified's existing manager/state bus, which already retains active-token callbacks through transient states. No separate Lab observer or listener. No new dependency or permission.
**User specification:** "roll this into 161 to become 162, swap the Lyrics button in now playing to open our new passed code test. bin the lab once confirmed."

## Completed

- [x] Verified live main, accepted v161 owner and Lab inputs; preserved their source branches.
- [x] Added failing native route/transplant checks on v161:34773211530.
- [x] Imported eight exact source blobs; private Activity declaration; version162; existing button's browser adapter now owns native route.
- [x] Passed four source/routing guards,61 timed-data,35 transport,2 incomplete-timing,7 entry and28 internal Activity/state/lifecycle checks.
- [x] Full signed pipeline34773509395 succeeded; original235 Unified/68 Shield tests and colour/speed tests retained.
- [x] Reviewed scoped diff in-session on PR10 and merged into the current owner without overwriting concurrent work.
- [x] Verified GitHub artifact10322553107, installed only on Shield, and matched installed162/base APK digest.
- [x] Retained com.boop.lyricslab159 with unchanged before/after hash.
- [x] Updated current handoff/status/Unified memory and installation receipt.
- [ ] Ryan confirms the integrated Unified Lyrics route and refresh behavior.
- [ ] After that confirmation, remove only the standalone Lyrics Lab from Shield and verify its absence.

No phone/emulator operations, local source builds, new grants, data resets or visual acceptance sweeps. Preserve all accepted v161 code/artwork and user settings. GitHub owns source/tests/build/signing; runtime acceptance is joint with Ryan.
Receipt:docs/handoffs/2026-09-13-unified-v162-lyrics.md.
References checked:https://developer.android.com/reference/android/media/session/MediaController and https://developer.android.com/reference/android/app/Activity .
