# v164 music bounce: signed candidate and verification receipt

Updated 2026-09-13. Ryan reported v163 did not move and regressed his accepted Lyrics button, rolled back himself, then requested a fresh signed v164 from confirmed v162 with actual music bounce for his manual test. The old permission-only candidate is not a completed bounce. This task implements the actual audio-level path and does not merge or install it automatically.

## Exact provenance

Base: `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`, independently fetched and still unchanged after the build. Accepted v162 source was `1e0136ffa9732353035f88ca7a7cb131f7481557`, APK `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`. This task did not start from Music Lab or v163, and did not diagnose the exact runtime cause of the reported v163 lyrics failure.

Task: `boop-unified-v164-music-bounce`. Signed source: `f9f65569250b9dc02602101ef4d56195824e0380`. Run `34778178916`, job `103780068294`, completed SUCCESS. Artifact `BOOP-Unified-v164-Music-Bounce`, ID `10323694771`.

Package/version: `com.boop.alpha1`, `164 / 1.2.164-music-bounce`.
File: `BOOP-Unified-v164-Music-Bounce.apk`, 155290450 bytes.
APK SHA256: `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`.
Artifact ZIP: 71281985 bytes, SHA256 `0a08fcbd7ebb4144bd1ea275b33e92b4be42534981c1953671b39295eb856aca`.
Permanent certificate SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Implementation and review

Six added Java classes provide waveform sampling, smoothing, a GL viewport wrapper, the foreground permission offer and the exact reused permission Activity/flow. Four existing app inputs are modified: two version fields; one private Activity manifest line; six settings-entry lines; the Now Playing puppet adapter. In-session review checked these changes and the native worker lifetime, permission/voice separation, stale-data rejection and shared-renderer delegation. No independent reviewer is claimed.

Android Visualizer session zero is sampled every 33 ms on a background HandlerThread. The level calculation removes DC before RMS and maps valid amplitude to a bounded height. Attack is 28 ms, release 140 ms, maximum lift 0.14 of the surface height. The zero-lift viewport is the original viewport. Blink speed and authored expressions remain owned by the unchanged canonical animation controller. Visibility/playing/permission gates stop native sampling; hide/detach also clear the envelope, and playback pause allows the lift to settle. Native operations and release are serialized on the worker. No room-microphone recorder, media projection, audio-focus, volume or transport operation is added.

The old conditional permission implementation is copied as exact source blobs, not merged as an old manifest or complete app. The v162 ShieldLyricsActivity registration remains present. No v162 lyrics file, Lyrics-button callback, media manager, launcher route, startup/voice feature, dependency, existing materialization script, approved art, shader or shared animation-engine file is changed. The settings row offers Music audio access; absent permission can also receive a once-only foreground explanation. Ryan must grant or decline through the real Android UI. No shell permission grant occurs.

## Test-first evidence and final checks

RED commit `975fa269effc1c52b106443ba0da03c2a6efcbe3`, run `34777983896`, job `103779494026`: all four new test groups failed for absent v164 version, audio source, permission entry and envelope. Logs were read before production implementation. Build/signing were skipped.

GREEN run `34778178916` at the signed source above, full completed job logs read:

- Four new v164 test groups passed. The actual pure-Java envelope ran 332 assertions for silence/DC/missing data, amplitude response, fast rise, softer decay, exact settling, nonfinite input, bounded height, reset and independent clock inputs. Source checks confirm actual Visualizer APIs and connected view/renderer wiring.
- New accepted-v162 guard passed: all previously tracked app/build inputs outside the four declared adapters are unchanged; the build configuration differs only in the two version fields.
- Native lyrics integration: routing and private registration/no-lab checks passed. Two inherited one-time v161-to-v162 source-freeze checks deliberately skip versions beyond162; the source-preservation check above supplies a current v162 baseline guard. These skips are not claimed as executed passes.
- Native lyrics harnesses passed: 61 data/timing/ownership, 35 transport, 2 incomplete-timing fallback, 7 entry control-flow and 28 Activity/state/lifecycle/transport assertions. Android boundary doubles are not physical Shield execution.
- Six existing animation-speed functions passed. Logged numerical checks: 160720 timing, 1157272 edge and 20920 Lab callback checks for each of original and materialized source. The 26 authored clips remain unchanged.
- Materialized speed/colour/master consistency passed; 10 canonical-owner/notification/face/bay pytest contracts passed. Source-to-generated-file comparisons passed for MusicBounceSource and ShieldNowPlayingPuppetView.
- Full `:app:assembleDebug` succeeded, 104 tasks executed. Packaged ID164/version/name, both Lyrics and permission Activity registrations, permanent signature and archive integrity passed. Actual APK DEX content includes MusicBounceSource, MusicBounceEnvelope, MusicBounceRenderer and ShieldLyricsActivity.
- Artifact upload and temporary signer removal succeeded. Existing action/Android/Gradle deprecation warnings remain nonfatal and are not claimed fixed. The complete historical test suite was not rerun.

The artifact ZIP was downloaded through the GitHub connector. Its hash matches the returned GitHub artifact digest. Extracted APK hash matches apk-sha256.txt, embedded source matches f9f65569, badging and public signer receipt match the expected identity. ZIP integrity and the actual APK's four class descriptors were independently rechecked in the artifact sandbox. No APK execution occurred there.

## Acceptance and delivery boundary

This is an implemented and signed candidate, NOT physically accepted music bounce. No Android device or emulator was launched or operated. No installation, grant, user-data clear, playback command, setting reset or merge was performed. Main, the existing Unified owner, accepted v162 and all labs were left untouched. Laptop continuity reads did not modify or build app source.

Deliver the direct APK in chat for Ryan's test. Usable real Deezer/Shield waveform data, permission-dialog behavior and the visible bounce remain unproven until that test. Silent or unsupported vendor output must not be reported as a successful beat connection. Preserve v162 acceptance separately, and keep the user's observed v163 regression and rollback distinct from our nonvisual checks. Final root handoff/status/memory and this receipt are documentation-only updates after the signed source commit.
