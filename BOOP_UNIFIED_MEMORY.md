# Unified memory: v164 music bounce from confirmed v162

Updated 2026-09-13. Ryan reported v163 had no movement and regressed his new Lyrics button. He then rolled back himself and explicitly requested a new signed v164 from confirmed v162, preserving confirmed features and delivering the APK for him to test. Do not reuse the v161 Music Lab tree or treat v163 as the baseline. Do not infer physical acceptance from a build or from source-only checks.

## Current task and protected baseline

Task branch `boop-unified-v164-music-bounce` starts directly at accepted `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`. That accepted branch remains unchanged; no main/owner/lab merge was made. Current candidate is normal Unified package `com.boop.alpha1`, version `164 / 1.2.164-music-bounce`, not another side-by-side lab. It has not been installed by this task.

v162 native Lyrics and Skip were user-accepted with "perfection". The internal ShieldLyricsActivity, matching button route, track observer, main lyric size and passive half-size bottom-right licence footer must remain. The retired standalone Lyrics Lab must not become a dependency or be reinstalled. v161 speed on Shield and Pixel 7, and automatic two-way eye colour, remain inherited accepted facts. Speed is device-local; colour is shared. Historical acceptance receipts and the complete original root memory remain at the v162 base and docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md.

## Music behavior now implemented

Actual device playback loudness drives vertical bounce height, while the existing animation-speed clock still controls blinks and expressions. Keep this separation. No BPM detection, inferred song beat, animation restart per drum hit or microphone fallback. Audio comes from Android Visualizer output mix only. Missing/stale/constant samples cannot generate pretend movement. Useful real Deezer/Shield waveform data still needs Ryan's test.

New MusicBounceSource polls waveform data on a short-lived worker while visible/playing/permitted, publishes only a transient level, drops stale data and releases when inactive. Native setup/sampling/release stay off the remote/UI thread. It does not change audio focus, volume or playback. MusicBounceEnvelope has DC-rejected level calculation and bounded fast-attack/soft-release motion. MusicBounceRenderer wraps the unchanged CanonicalEyeRenderer and shifts the GL viewport rather than changing eye pose, shaders or art. Existing authored clips and shared animation-speed binding are not rewritten.

The exact earlier MusicAudioPermissionActivity/Flow source is reused, not the earlier branch tree. A foreground-only once-off explanation is offered if permission is missing, with explicit Continue/Not now and Android's grant UI. There is no auto-grant or repeated denial loop. Launcher Settings > Now Playing > Music audio access remains the manual entry. Existing permission declarations already cover Visualizer; no additional uses-permission was added. No voice permission callback or microphone recording is introduced by this path.

## Candidate and verified scope

Source `f9f65569250b9dc02602101ef4d56195824e0380`; signed run `34778178916`, job `103780068294`; artifact `10323694771` / BOOP-Unified-v164-Music-Bounce. APK `BOOP-Unified-v164-Music-Bounce.apk`, SHA256 `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`. Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Exact downloaded artifact/build identity was rechecked without executing the app.

New tests were observed failing before production code, then passing with 332 real-envelope assertions. v164 preservation guard restricts existing source changes to four narrow adapters. All other accepted app inputs remain unchanged in source. Native lyrics routing/data/timing/transport/lifecycle checks, preserved animation timing/materialization, 10 owner/bay contracts, full signed build and actual packaged identity/signature/lyrics-and-bounce class checks passed. Two pre-existing v162-only source-freeze tests skip later versions; this was not hidden and the new v164 baseline guard passes. Full evidence: docs/handoffs/2026-09-13-v164-music-bounce.md. No independent review or full historical test-suite rerun is claimed.

## Delivery and safety boundaries

Give Ryan the signed file in chat. There was no installation, permission change, runtime playback test, autonomous emulator run or hosted visual judgement. His rollback is his report, not a new device readback. Keep development/build/signing/handoffs on GitHub and test behavior jointly with him. Do not silently merge or install this candidate over his restored app. A later requested install can use the normal verified APK path without clearing data, changing keys or automatically granting audio access. Leave physical Pixel 10 alone.

Laptop continuity files were read only; no app checkout was edited, built, cleaned or claimed synchronized. Keep Desktop Commander pinned to its working 0.2.47 command. Preserve other tasks, private device data and accepted settings. Public docs contain sanitized evidence only. Fetch LIVE task HEAD before continuation and update the task handoff/status/memory after material results.
