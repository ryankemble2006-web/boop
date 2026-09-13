# Unified v162 native lyrics integration and Shield installation receipt

Date:2026-09-13. User request: integrate the tested lyrics feature into161 as162,
replace Now Playing's Lyrics route, and remove the standalone lab once confirmed.

## Provenance and merge

Accepted Unified base:593ad609ff87f651d5273bd17f5a2c0ca3ef5198.
Lyrics source branch:boop-lyrics-lab-side-by-side-v157 at5ce581be6f1da10eb47640c4c11636a4bfa9e330.
Lab footer application source:e6f9bbb736ac90287815bea3a9c48496505675ed.
Integration application/build source:1e0136ffa9732353035f88ca7a7cb131f7481557.
PR10 head:fad4ab24bc234e383f7b9ba22a22080eaf0f616d.
Merge into boop-unified-eye-sync-safe-v159:0908d6955c90978e97dcbae9031f3f1de638bd9d.
The base was rechecked live immediately before merging; no concurrent advance.
GitHub compare confirms the merged tree differs from the build only by12 test
lines, with no app/build-input difference. The later lines scope the one-time
v161 preservation guard to162, without skipping any check for this candidate.

Eight source files were copied by immutable Git blob: DeezerLyricsBrowser,
DeezerLyricsDocument, DeezerTimedLyricsClient, LyricsLinesView, LyricsRequestGate,
NativeLyricsLoader, ShieldLyricsActivity and ShieldLyricsView.
The only other app changes are the private Activity declaration and version bump.
Unrelated v161 source, manager, launcher, state bus, colour/speed and artwork are
byte-identical. The existing build workflow gains one additive native-test step.
No Lab application/listener/observer, dependency, permission or account data imported.

## Checks

Test-first run34773211530 at73fe0ee0 failed on v161's old macro/missing native path.
Native checks34773471412 and34773700715:SUCCESS. Four routing/source tests,
61 data/timing/ownership,35 transport,2 incomplete-timing,7 entry and28 internal
Activity/state/lifecycle/transport assertions passed. Controlled Android/service/
renderer boundaries in the Activity tests are not physical-device evidence.
Full signed build34773509395:SUCCESS;235 Unified and68 Shield tests with zero
failures/errors/skips; retained colour, speed and other non-visual pipeline passed.
PR10 review5191705196 is an in-session review, not an independent reviewer.

## Artifact and physical installation

Artifact BOOP-Unified /10322553107; built source1e0136ffa9732353035f88ca7a7cb131f7481557.
GitHub-reported artifact ZIP digest:112b5ba62aa365227f419a0e9e825ca648991f6fab936e8e90b1a083ad453bc9.
APK SHA256:cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171.
Permanent signer:f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
Package/version:com.boop.alpha1 /162 /1.2.162-native-lyrics.

The artifact was downloaded to a task-specific temporary cache. Built-source
receipt, APK hash, aapt package/version and apksigner verification were checked.
The packaged ShieldLyricsActivity exists and android:exported is false.
Shield baseline read:161 /1.2.161-lab-scale-independent with exact accepted hash
c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6.
Ordinary adb install -r returned Success; no downgrade flag or reset was used.
Post-install162/name/base-APK hash matched the signed candidate exactly.

Lab before and after:com.boop.lyricslab /159 /0.1.159-lyrics-footer, hash
fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503.
Lab was not uninstalled. No app launches, playback inputs, permission changes,
phone commands, emulator runs, local app source edits/builds or settings resets.

## Next joint test and conditional cleanup

Ryan should use Now Playing -> Lyrics in Unified, not open the standalone Lab,
and confirm the integrated result. His prior real-Shield Skip result remains
accepted for the Lab but does not prove this separate integration automatically.
After his confirmation remove ONLY com.boop.lyricslab from Shield, verify absence,
and retain its source history. Do not remove it merely because this build passed.
No physical visual/audio/no-lyrics/offline/natural-completion acceptance is invented.
Main routing remains correct because the Unified owner branch did not change.
