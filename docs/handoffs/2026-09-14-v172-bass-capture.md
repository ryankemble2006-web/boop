# v172 native-rate bass capture canary

Updated 2026-09-14. Owner branch: `boop-dance-levels-v167`.

## Device feasibility and scope

Ryan requested a faster response to bass below about120Hz while preserving automatic native44.1/video48 switching and independent BOOP clocks with Android animations disabled.

Separate temporary probe source8a10a8f9504d0079c58bb76c968f22163e79d5c8, run34869220604, APKfa87769bb4f63541387a22ab1a9ea3ba685277e608606654ecc742a06a83385e. Ryan approved Android's actual consent dialogs. First capture obtained PCM but the HDMI route had changed to48 before capture; that attempt was inconclusive. The controlled second test preserved the SAME DIRECT44100 HDMI output before/during/after, with a separate44100 remote submix feeding capture. About100 requested10ms reads/sec, maximum blocking-read durations21.3–21.6ms. These are not end-to-end latency measurements. Pause produced zeros; resume restored PCM;60second automatic stop released projection. Temporary probe removed afterward.

## HA lab correction

v170 excluded only original `local.johnnycastaway.shield`. Ryan clarified the HA lab is `local.johnnycastaway.halab`; v171 added that exact package to IGNORE. Red34869443465 reproduced the missing exclusion; green34869568218 passed at33f3a77dd5c52f9ddfe3427ade66103ca4fcaa62. v171 was built/signer-checked but not installed separately; v172 inherits it. Neither Johnny APK was changed. The system-selected screensaver was the original Johnny, so earlier actual DreamService testing was NOT an HA-lab screensaver test.

## New bass canary

Explicit Launcher Settings entry: Deezer bass bounce test (10 minutes). Requires fresh MediaProjection consent. Captures only installed Deezer UID plus MEDIA usage. No microphone source, video frames, saved audio, playback output or audio-routing write. The foreground service stops after10minutes, manual Stop or projection revocation; it is not automatically enabled after reboot and is not a permanent production capture policy.

Stereo35Hz high-pass and120Hz low-pass filters estimate bass energy separately per channel. Capture samples bypass the old native poller and independent fallback while active. Movement stays visibility/playback/permission-gated; stale samples expire after150ms and session ownership blocks stale workers. Capture attack4ms/release90ms, UI sampling16ms. Other authored animation clocks remain independent of Android scales. Native visualizer path and old blocked-route fallback remain when explicit capture is off; do not mistake that fallback for bass response.

## Build and install evidence

Signed source `84a28c029f7bc0e5e77350e5e842b7ef19c78ea1`.
Run `34870194127` SUCCESS; artifact `10358144269`, `BOOP-Unified-v172-Bass-Capture`.
Package `com.boop.alpha1`,172 / `1.2.172-bass-capture`.
APK SHA256 `e0311e61b587fad35b3a20da0326ccce712d0eb774ef65bd7fa9fce52f81ecc0`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

RED34869904428 failed missing bass implementation. Green410 DSP/state/fast-attack assertions and1444 native-worker/capture-priority/curve assertions passed, along with routing, voice14scenarios twice, natural-voice, lyrics, renderer and independent animation checks. Two inherited one-time source-freeze checks remain skipped, not passed. The old lyrics permission freeze was narrowed to the exact authorized foreground-capture service declaration. Read-only source review found no blocker; service lifecycle is not comprehensively unit-tested, so device checks remain required.

Shield install succeeded and installed APK hash matches. All16 saved preference hashes unchanged. Phone remains acceptedv166. Android animator scale remains0. Live BOOP log reports Deezer bass capture active,44100stereo,35–120Hz; about501reads/5seconds. Only bass worker exists during capture; old music poller absent. HDMI remainsDIRECT44100 with separate44100remote submix. Capture started after actual consent and Home was restored for Ryan's visual test.

## User verdict and final device checks

Ryan's report of no-bass/slow dancing DURING the build referred to still-installedv170 fallback, confirmed by device version; it is not a v172 verdict. Requested fresh visual timing/no-bass test only AFTER verifying v172 and actual live capture. Ryan reports it is better, but picks up too much and is unsuitable for active trance. This is a rejected musical-response result, not final acceptance. The implementation measures bass energy; rolling bass lines can therefore drive it between kicks. A kick-attack detector is only a possible next experiment, not implemented or accepted.

Stopped the integrated service explicitly after the verdict; dumpsys confirmed no active MediaProjection. Launched the actual Johnny HA Lab tile from BOOP Apps and verified the resumed package local.johnnycastaway.halab. HDMI remained DIRECT44100 and there was no BOOP native-mode0 write. Returned Home with Deezer playing. Removed temporary device UI captures. No Johnny app edits or phone operations.

v172 remains installed, with opt-in bass capture OFF. Its old fallback can still animate without music-band information when capture is off; do not present that as the bass test. The ten-minute setting is a canary, not a permanent user-approved dance solution.
