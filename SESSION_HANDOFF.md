# v174 accepted for now

Ryan's physical verdict: "yeh it will do for now at least ... shows hes listening at least." Keep installed v174 relaxed simulatedgroove. This is provisional acceptance of its playback indication, not beat synchronization. No further dance changes requested. Capture remains off; phone untouched.

# v174 relaxed playback groove installed — visual verdict pending

Updated2026-09-14. Owner branch `boop-dance-levels-v167`.

Ryan explicitly chose relaxed, varied simulated groove after v173 was sharper but unfocused on musical accents. PlaybackGroove provides gentle bounded sway/lift and occasional flourish from its own clock. No beat-sync claim.700ms entrance,350ms pause settle; hidden/detached resets. NowPlaying no longer starts audio sampling or prompts for audio access; temporary bass-test Settings entry removed. Canonical art/headphones/expressions/colour and existing speed scope preserved. Android animation0 independence retained. No audio routing or voice changes.

Signed source `e375800a58839df466e155476e2a6729b082545a`; run34874180697 SUCCESS; artifact10360545388 `BOOP-Unified-v174-Relaxed-Groove`.
Packagecom.boop.alpha1 version174 /1.2.174-relaxed-groove.
APK SHA256 `4448a2881f54e9f0fade089434bbba72a3358d340524941f58d19b80c896112d`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` verified.

RED34874091551 observed missinggroove implementation. Finalbounds/continuity/pause/restart/reversal tests passed, plus inherited DSP/worker/voice/routing/lyrics/animation checks; two inherited historicalskips remain skipped. Source review found no blocking issue; physical appeal belongs to Ryan.

Shield update succeeded; installed hash matches. All16preferences unchanged; animator_duration_scale0. No MediaProjection, noBOOP-MusicLevels orBOOP-BassCapture worker. LiveDeezer DIRECT44100 HDMI active. ReturnedHome and asked Ryan to judge gentlegroove; verdict pending. Phone untouched. v173 rollback retained privately; earlier installer recovery backups retained.

---

# v173 musical verdict — capture stopped

Ryan reports v173 is sharper but not focused on the desired musical accents; varied music makes universal beat matching unsuitable. This is NOT physical rhythm acceptance. User is considering simulated dance. Proposed next direction: varied, non-metronomic sways/nods/flourishes gated by playback; awaiting preference before implementation. Do not restore the rejected regular clock-hop unchanged or describe it as beat-synchronized.

Stopped capture explicitly after verdict and confirmed MediaProjection null. v173 remains installed; old fallback may move outside capture. No v174 changes yet.

# v173 bass attack canary installed — physical verdict pending

Updated 2026-09-14. Branch `boop-dance-levels-v167`.

Ryan authorized Velo-inspired bass attack experiment. Independent implementation (no GPL source copied) uses35–120Hz unclippedenergy, adaptive relative-rise gating,200ms repeat suppression,100ms pulse,256-frame stereo44100 reads and urgent-audio scheduling. Audio sample count drives detection; uptime drives freshness/display. Review caught wall-clock batching issue and confirmed final correction, no remaining blocker. No universal kick separation or end-to-end latency claim.

Source `fb6855422f072392edab7bd25376f14969768237`; run34873273139 SUCCESS; artifact10359554377 `BOOP-Unified-v173-Bass-Attacks`.
Version173 /1.2.173-bass-attacks; APK SHA256 `f32e40078aa5192616407cc3b97d73b4fadb0164c812e49b6749d45eff4983f7`; permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` verified locally.

Test-first missing detector RED34873058675. Final CI passed steady PCM40/60/100Hz, pulsed60Hz at two gains/phases with one timely hit per synthetic kick, ideal onset/noise/silence/gap tests,410priorDSP/state assertions,1444workerchecks and inherited voice/routing/lyrics/animation checks. Two inherited historical skips remain skipped. Physical trance timing is unverified.

Initial install failed INSUFFICIENT_STORAGE with819MB free. Three historical BOOP download installers backed up privately to laptop and hashes verified before exact device copies removed, freeing about406MiB. No installed apps/settings removed. Retry succeeded; installed APK hash matches; all16preferences unchanged. Phone untouched.

Actual Android consent granted; v173 capture active18:17, about860–863reads/5sec,13–17detectedattacks/5sec on current music. SAME direct44100HDMI output retained, separate44100remote submix. Returned BOOPHome for Ryan test. Capture remains explicit ten-minute canary with auto-stop; old fallback remains outside capture, do not confuse it with new detector. Physical verdict pending.

Prior v172 rejection and Johnny HA-lab verification retained below.

---

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
