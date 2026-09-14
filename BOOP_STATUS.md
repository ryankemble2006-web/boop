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

## v172 bass capture canary installed (2026-09-14)

Controlled playback-capture probe preserved DIRECT44100 HDMI while delivering real PCM; pause/resume/one-minute cleanup verified. Temporary probe removed. v172 integrates explicit ten-minute Deezer35–120Hz capture with4msattack/90msrelease and16msviewupdates. Old fallback remains when capture is off; it is not bass-driven.

Source84a28c029f7bc0e5e77350e5e842b7ef19c78ea1, run34870194127success, artifact10358144269. Signed APK e0311e61b587fad35b3a20da0326ccce712d0eb774ef65bd7fa9fce52f81ecc0 installed/readback verified;16preferences unchanged, Androidscale0, phonev166. Live bass capture andDIRECT44100 verified. Ryan reports better response but too much movement for active trance; musical response rejected. Capture explicitly stopped; projection released. Actual HA-lab tile launch verified preserving DIRECT44100. See [receipt](docs/handoffs/2026-09-14-v172-bass-capture.md).

Correction: v170 covered original Johnny only. v171/v172 also exclude local.johnnycastaway.halab from BOOP audio switching. Neither Johnny app was edited; prior system screensaver test exercised originalJohnny, not HA lab.

## v170 Johnny launch exclusion installed (2026-09-14)

Supersedes the prior next-step priority: reproduced Johnny tile launch changing native mode0, then fresh Deezer stream using MIXER48 and original visualizer again. Ryan recognized the original movement with slight latency. Native44.1 was restored. Actual Johnny DreamService start/stop preserved it; only BOOP tile classification was wrong.

v170 excludes silent Johnny from audio-mode switching while preserving Deezer/video/Cast rules. Signed source82ae808f69686aea788e300430aee412293764ae, successful run34868133982, artifact10358455609. APK4874e00956ffdae05681eef9c4795a71f511d3dcf3c2971eb9714f0a6158bc60 installed/hash-verified on Shield; all16 preferences unchanged. Fixed tile launch preserves DIRECT44.1 with no mode0 write. Phone remainsv166; Johnny app unchanged. See [v170 receipt](docs/handoffs/2026-09-14-v170-johnny-audio-neutral.md).

This fixes Johnny routing interference, not native44.1 Visualizer rejection or bounce latency. Earlier pre-reboot route was not recorded, so exact historical causation remains unproven.

## Latest verdict and capture investigation (2026-09-14)

Ryan reports v169 is smoother but far off the beat. This is not final rhythm acceptance. He asks how to enable real capture for Deezer/Chromecast/direct output while preserving native music/video switching.

Read-only device evidence: BOOP already has RECORD_AUDIO and MODIFY_AUDIO_SETTINGS. Both installed Deezer and built-in Chromecast declare ALLOW_AUDIO_PLAYBACK_CAPTURE; Deezer's current MEDIA player flags do not include the no-projection/no-system-capture bits and no per-app override was listed. Live Chromecast capture policy has not been tested. Shield runs Android11 production firmware with ordinary ADB shell access.

The original Visualizer fails at AudioFlinger's DIRECT-output effect check, not an ordinary app permission. The next proposed canary is the separate AudioPlaybackCapture API, with actual MediaProjection consent, to test real-time samples and verify that direct44.1kHz output remains unchanged. This is not yet implemented or proven. No permissions/root/system/audio changes. See [Android playback capture documentation](https://developer.android.com/media/platform/av-capture). v169 remains installed; neither diagnostic onset dance nor independent playback rhythm met Ryan's beat-sync requirement.

# Latest: v169 native bounce with playback fallback installed

2026-09-14: Ryan rejected the v168 diagnostic dance timing. His authorized option2 is installed on Shield: original fast visualizer bounce where available, smooth independent playback dance on blocked routes. No diagnostic polling. Source `01f0053b922a8c344d5534b17cf342c1c657703c`, signed run `34866309663`. Installed hash/preferences verified; live fallback and pause/resume confirmed. Actual smoothness verdict pending; do not claim beat sync. Audio switching, independent clocks and voice repair preserved. [Full receipt](docs/handoffs/2026-09-14-v169-playback-dance.md).

---

# Latest: v168 music hops installed on Shield

2026-09-14: signed source `09a222291d6e097834d649a603b140b4f50140e9`, successful run `34865245148`. Installed/hash-verified on Shield only; saved preference hashes unchanged. Real direct-output levels work on Deezer's 44.1 kHz route. Ryan confirmed initial v167 VU-style motion; v168 now uses onset-driven hops, with visual rhythm verdict pending. Preserve automatic audio switching, Android-animation-independent BOOP clocks and v166 voice repair. Phone remains v166. Full receipt: [v168 music hops](docs/handoffs/2026-09-14-v168-music-hops.md).

---

# Current voice repair acceptance — 14 September 2026

v166 on `boop-voice-ack-v166` is signed, installed and command/spoken-reply USER-ACCEPTED on Shield and the explicitly substituted Pixel 10 Pro XL. Final tested source `982a2ddeed73d673be6fe0fe2251a74d290c07c5`, successful run `34861777001`, artifact `10355475599`. Preserve the first Shield NO_SPEECH_DETECTED attempt as historical failed input; the coordinated retest succeeded and the user explicitly confirmed hearing speech. Voice choices, colour, HOME/assistant and permission settings were preserved. Scope is the tested command/reply path, not long-running microphone reliability. Exact receipts: `docs/handoffs/2026-09-14-v166-voice-startup.md`.

---

## Prior branch history (historical)

# Unified v162: native lyrics user-accepted; standalone Lab removed

Updated 2026-09-13. Owner: `boop-unified-eye-sync-safe-v159`.
Package/version: `com.boop.alpha1`, `162 / 1.2.162-native-lyrics`.

**Physical acceptance:** Ryan replied "perfection" to the integrated Now Playing
-> Lyrics / Skip confirmation. The merged native feature is USER-ACCEPTED ON
SHIELD, not awaiting another confirmation. The approved footer/presentation and
all earlier accepted v161 colour/speed behavior remain protected.

**Conditional cleanup completed:** after that confirmation, only the separate
`com.boop.lyricslab` was uninstalled on Shield. ADB returned Success/exit 0 and a
fresh package query verified absence. Unified v162's version and installed APK
hash were identical before/after. No phone/emulator operation, playback input,
app launch, new install, manual permission/settings change or app-code edit.
The standalone source branch and historical test/build evidence are retained.

**Exact installed APK:** SHA256
`cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
Built source `1e0136ffa9732353035f88ca7a7cb131f7481557`, signed run `34773509395`,
artifact `BOOP-Unified` / `10322553107`. Permanent signer unchanged. PR #10 merge
`0908d6955c90978e97dcbae9031f3f1de638bd9d`. Earlier source/functional/package checks
passed; they were not rerun for this acceptance record. User acceptance is distinct
from the previous CI evidence and today's package-only cleanup verification.

**Next:** keep the accepted native feature inside current Unified. Do not reinstall
the retired Lab or reopen the confirmed integration gate. Unenumerated catalogue/
offline cases remain unclaimed coverage, not a new automatic testing backlog.
See `SESSION_HANDOFF.md`, `BOOP_UNIFIED_MEMORY.md`, and
`docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md`.
