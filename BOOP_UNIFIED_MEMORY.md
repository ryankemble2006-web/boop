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

# Unified memory: v162 native lyrics accepted, standalone Lab retired

## Latest result and completed conditional cleanup

2026-09-13. Ryan replied "perfection" after the request to confirm Now Playing ->
Lyrics and Skip in integrated Unified v162. Treat this as USER ACCEPTANCE of the
integrated native lyrics feature on Shield. It supersedes the earlier awaiting-
confirmation state; do not ask him to repeat that already-accepted gate.
This is Ryan's physical report, not a new assistant-run UI/audio/lyrics test.

Ryan had explicitly said to bin the Lab once confirmed. That condition is now
satisfied and cleanup was performed: only com.boop.lyricslab was uninstalled from
Shield. ADB returned Success/exit0 and a fresh package-list read verified absence.
Before removal the Lab was159 /0.1.159-lyrics-footer with APK
fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503.
Unified was162 with the exact signed hash before AND after; it was not changed.
No other Lab/Deezer/Unified/source history was removed. No phones, emulators,
playback keys, app launches, new installs, manual permission/settings changes,
app-source edits or builds accompanied this cleanup/documentation continuation.

## Current delivery and ownership

Owner boop-unified-eye-sync-safe-v159. Integration branch boop-unified-native-lyrics-v162.
PR10 merged the approved feature at0908d6955c90978e97dcbae9031f3f1de638bd9d.
Main is the shared-context hub and already points to this owner. The v159 suffix
is NOT the app version. Do not restart from the older standalone Lab base.

Shield: com.boop.alpha1 /162 /1.2.162-native-lyrics.
Build source1e0136ffa9732353035f88ca7a7cb131f7481557; signed run34773509395;
artifact10322553107; APK cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171.
Signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
The prior install upgraded exact accepted v161 and checked source/package/signature/
hash. The cleanup above independently rechecked its installed identity unchanged.
Neither phone was queried or updated in this lyrics task.

## Native lyrics, retained UI and observation

Now Playing's existing Lyrics button enters internal unexported ShieldLyricsActivity,
not Deezer menus or another app. It has no runtime dependency on the retired Lab.
Shared data/renderer/loader and footer came from exact reviewed Lab source blobs.
The passive licence/copyright credit sits bottom-right at half its previous size;
main song lyrics retain their size. Back to Now Playing footer action is removed;
physical Back remains the Activity's path. Keep this accepted design.

Unified's own unchanged manager/state bus retains active-token callbacks separately
from displayable playback state. The Lab-specific observer/listener/package was not
imported. Its earlier skip failure was real, fixed, and user-confirmed before the
integration; retain that provenance, not the obsolete physical package.
No new permission, account credential, dependency or persistent lyric store.

## Prior test provenance, not rerun for acceptance

Routing/transplant red34773211530; green34773471412 and latest test-only fad4ab24
in34773700715. Four source/route checks plus61 timed,35 transport,2 incomplete,
7 entry and28 Activity/state/lifecycle assertions passed. The full pipeline passed
235 Unified and68 Shield tests with no failures/errors/skips, preserving colour,
speed and artwork source checks. Android/view boundaries in new tests were doubles.
Scoped PR10 review was in-session, not independent. No hosted visual acceptance.
The merge differed from built app1e0136ff only by12 later test lines permitting
future releases beyond162 to evolve source; no compiled inputs changed.

Ryan's new acceptance must not be expanded into individually unreported catalogue,
offline, natural-track-completion or all-hardware test results. Those limits do not
justify re-opening the integrated feature he just accepted or resurrecting the Lab.

## Accepted v161 foundation and workflow

Base593ad609 / build0b6ee6f9 retains speed acceptance on both Shield and Pixel7 and
automatic colour both ways. Speed is local, colour shared. Unrelated v161 source/
artwork was preserved during integration. Do not reset choices or reopen repairs.

GitHub owns source/non-visual tests/build/permanent signing and documentation;
device tests are joint with Ryan. No autonomous emulator gate/local source-build
loop or connection upgrades. Desktop Commander0.2.47 remains pinned; ADB is fine
per Ryan. Physical Pixel10 remains excluded. Keep other tasks/worktrees intact.

Receipt: docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md.
Earlier integration: docs/handoffs/2026-09-13-unified-v162-lyrics.md.
Pre-acceptance docs remain at978bf7df5e0759512b8a69807d9badeee7a8bb95.
Standalone branch boop-lyrics-lab-side-by-side-v157 remains history/recovery source,
not an app to reinstall. No private captures, raw dumps, addresses, keys or APKs
belong in the public repository. No local checkout synchronization is claimed.
