# Unified v162 native lyrics integration

**Goal:** Apply Ryan's tested native lyrics feature and smaller bottom-right credit to accepted Unified v161 as v162; the existing Now Playing Lyrics button must open the native view, not Deezer's menu macro or the separate Lab.
**Base:** `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`, owner `boop-unified-eye-sync-safe-v159`, accepted app v161 built at `0b6ee6f91e05f00138a94ec2c9fd846117020754`.
**Feature source:** `5ce581be6f1da10eb47640c4c11636a4bfa9e330`, `boop-lyrics-lab-side-by-side-v157`, lab v159 footer source `e6f9bbb736ac90287815bea3a9c48496505675ed`.
**Owner:** `boop-unified-native-lyrics-v162`.
**Architecture:** Copy the exact tested renderer, timed-data parser/client/loader and native entry implementation by Git blob. Use the existing Unified MediaSession manager and state bus. Its Binding callbacks already survive transient playback states, so do not transplant the Lab's separate observer or notification listener. Add the private internal activity to the existing manifest. No launch of or dependency on com.boop.lyricslab.
**Tech stack:** Existing Java/Android views and media session APIs, existing GitHub non-visual tests/build/permanent signer. No new dependency.
**User specification:** "roll this into 161 to become 162, swap the Lyrics button in now playing to open our new passed code test. bin the lab once confirmed."

## Constraints and evidence

Current main workflow at `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80` governs: source, tests and builds stay on GitHub; jointly test the installed candidate with Ryan. No automatic local emulator or hosted visual acceptance gate. No permission changes, data clears, key changes or phone access for this Shield lyrics delivery. Preserve v161 colour and device-local speed, original 1x, all artwork and unrelated code byte-for-byte.
Ryan confirmed the Lab's real-Shield Skip refresh, player controls and approved presentation. The footer change is the existing small credit at bottom right, no Back-to-Now-Playing link. This acceptance does not automatically certify the Unified integration.
Only install com.boop.alpha1 on Shield after checks. Retain com.boop.lyricslab until Ryan confirms the integrated screen works. Its removal is conditional, not authorized before that confirmation. Preserve the source branch/history for recovery.

## Steps

- [ ] Publish a failing native-route/integration assertion on the v161 base.
- [ ] Copy tested shared lyrics source/tests by immutable blob; add private ShieldLyricsActivity and version162. Preserve the v161 manager, bus, launcher and animation code.
- [ ] Exercise the real internal activity with the real state bus against controlled Android/service boundaries, including next-track replacement, late results, lifecycle and transport forwarding. Keep this functional coverage distinct from device acceptance.
- [ ] Run the existing full signed Unified pipeline and additional native checks on GitHub; review the allowlisted diff and package/signer/provenance.
- [ ] Download verified artifact, perform Shield-only update and verify installed identity. No playback inputs or visual claims without joint testing.
- [ ] Update owning handoff/status/Unified memory, source routing on main if ownership changes, and verify live heads. Keep Lab installed awaiting integrated acceptance.

## References checked

Android MediaController callbacks deliver metadata/playback events separately from session list changes: https://developer.android.com/reference/android/media/session/MediaController
Activity visibility subscriptions may run between onStart/onStop: https://developer.android.com/reference/android/app/Activity
Source review found Unified reconcileOnce binds all active tokens without playback-eligibility filtering; selection is separate. That existing design avoids the Lab regression without replacing the accepted manager.
