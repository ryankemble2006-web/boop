# Current: v187 felt review lab — 2026-09-15

Owner `boop-felt-review-lab-v187`; reviewed/built `b8308f4a18237b6d97ecab38ee3fd9de64ff7b6a`, run34941872719 passed. Permanent-signed187 installed and actual APK-hash verified on Shield and Pixel7Pro.
**Eyes and animation → Felt animation lab** now exposes current felt art with Pause/Resume, Slow review, a timeline, one-ms steps, Half blink and hide/show controls. Phone held50%/50% at36.6ms; stepping and landscape rotation preserved state in the actual joint-device check. Device rotation and saved appearance settings preserved.
**V186's few stationary pixels remain unresolved. Ryan requested this lab to freeze and identify them; this build does not change the rig or artwork.** Original standalone reference lab and rollback APKs preserved.
See [v187 receipt](docs/handoffs/2026-09-15-v187-felt-review-lab.md). Earlier entries are historical.

# Current: v186 inner photographic fringe — 2026-09-15

Owner `boop-inner-fringe-v186`; built `be42043a818d17b0af63bc549613e276efe95c0f`, run34940304639 passed. Permanent-signed186 installed and APK-hash verified on Shield and Pixel7Pro.
Thin previously unrigged bright fragments inherit nearby cap motion; every v185 opacity byte and valid body geometry value is preserved.
**Ryan reports a few stationary pixels remain. V186 is not fully visually accepted. His requested next step is the current felt renderer in an accessible animation lab, with pause/half-blink inspection.** Preserve accepted vivid palette, sharing and preview. Production/reference speeds and phone rotation restored.
See [v186 receipt](docs/handoffs/2026-09-15-v186-inner-fringe.md). Earlier entries are historical.

# Current: v185 moving photographic corners — 2026-09-15

Owner `boop-moving-corners-v185`; built/reviewed `a4df9e4154a7e1fd0fda843af7af09106d6691a2`, run34939256214 passed. Signed185 installed and APK-hash verified on Shield and Pixel7Pro.
Connected cap boundaries and solid corner masks remove the larger stationary strips; v184 central RGBA is verified unchanged.
**User reports much better; tiny stationary green remnants remain on both inner eye edges. Next investigate those pixels while preserving this improved baseline.** Accepted vivid palette, sharing and preview remain unchanged. Temporary speed/rotation settings restored and verified.
See [v185 receipt](docs/handoffs/2026-09-15-v185-moving-corners.md). Previous entries are historical.

# Current: v184 solid photographic lid — 2026-09-15

Owner `boop-solid-felt-lid-v184`. Built source `ab2bd9759bd120b762c6651b148191754d4314d2`; run34937571488 passed. Permanent-signed version184 installed and APK-hash verified on Shield and Pixel7Pro.
V183 was rejected for a second lid. Confirmed cause: flood-filled backdrop produced a stationary partly transparent seam through dark felt. V184 makes that interior solid; preserves PNG, shader, stretch mapping, accepted vivid palette, sharing and preview.
**User reports a huge improvement; outer-corner stationary shadowing still looks disjointed during movement. Investigate that remaining defect next.** Slow actual-device frames confirm central seam improvement. Both temporary review speeds restored to1x and verified. See [v184 receipt](docs/handoffs/2026-09-15-v184-solid-lid.md). Prior entries below are historical and their pending verdicts are superseded by this latest report.

# Current: v183 eye-shadow boundary correction — 2026-09-15

Owner `boop-felt-shadow-boundary-v183`. Reviewed/built source `d06c49358b941b160d17e8a061d6bbafb3fc808c`; run34935869282 passed. Permanent-signed version183 installed and APK-hash verified on Shield and Pixel7Pro.
**V182 was rejected on both devices for a remaining second lid/faint line beneath the shadow. V183 visual verdict is pending.** The coarse felt boundary included neutral eye-shadow pixels, which vivid recolouring made into a bright strip and blink mapping stretched. V183 excludes that shadow and confines edge feathering to the felt.
Preserve accepted vivid palette, sharing and preview; original PNG unchanged. See [v183 receipt](docs/handoffs/2026-09-15-v183-shadow-boundary.md). Earlier entries are historical.

# Current: v182 original PNG eyelids — 2026-09-15

Owner `boop-moving-png-lids-v182`. Built/reviewed source `66cc6d44e61339bbaeade1a9c67be88f088154ce`, run34934919950 passed; signed version182 installed/hash-verified on Shield and Pixel7Pro.
V181 vivid felt colour is physically accepted; preserve palette, preview and sharing. V181 doubled eyelids and both green-visible scars rejected. V182 moves the original photographic lid and rejects measured false boundary spikes; original PNG unchanged. **User verdict on v182 blink and scars pending.**
See [v182 receipt](docs/handoffs/2026-09-15-v182-moving-png-lids.md). Prior entries below are historical.

# v180 physical feedback and proposed PNG rig — 2026-09-15

User confirms felt colour sharing worked first attempt. Preserve it. Colour is too subdued: requested full bright fabric colours, including vivid green/blue. User reports no visible hairs; fibre appearance remains unaccepted. App stays v180.

Inspection confirms current app never loaded the approved generated felt study as its material. It approximates felt over the original master. v180 fades strands in small views; palette preserves dark charcoal luminance. Proposed next step is a layered PNG puppet rig and fabric recolouring separated from illumination. See [design proposal](docs/handoffs/2026-09-15-png-puppet-rig-proposal.md). Proposal only; no new source build/install in this feedback turn. Earlier pending sharing verdict is superseded by this user report.

# Current: v180 Build a Boop (2026-09-15)

Owner `boop-build-a-boop-v180`. Free felt colour below eye colour, fixed live preview, independent opt-in felt sharing, sparse worn fibres with lower small-scale coverage. Built source `4fa906d8e374f628459c50860356efcb27bffb30`; GitHub run34932494089 passed. Permanent-signed version180 installed and hash-verified on Shield and Pixel7Pro. **Physical visual and live felt-sharing verdict pending.** See [receipt](docs/handoffs/2026-09-15-v180-build-a-boop.md). Preserve original artwork, accepted preview interaction, default charcoal lighting and v178 rollback. Earlier entries below are historical.

## v179 physical verdict — 15 September 2026

Ryan reports phone widescreen felt texture is acceptable, but hairs look insufficiently random compared with the approved PNG. Shield now looks overexposed, as though stage lighting increased. v179 is **not visually accepted**. This supersedes the pending-verdict entries below.

Source inspection: strands have randomized parameters but remain contour-spaced and directionally constrained. Width is kept in screen pixels while positions shrink with the face; this increases relative bright coverage at small sizes. The added forward fibre layer is the leading explanation for the Shield brightness regression, despite byte-preserved core lighting. This is source-supported diagnosis, not a controlled device comparison proving the complete cause. Do not blame display capability or change device brightness.

Next design direction: preserve accepted phone material, shared-colour preview and v177 soft lighting; reconsider the fibre layer with irregular sparse clumps, broader direction/length variation and reduced coverage at small scales. No new app revision or rollback installed in this feedback turn. Installed v179 remains unchanged; v178 rollback retained. Agree the revised visual approach before another fibre implementation.

# Current delivery: v179 stage fibres (2026-09-15)

Owner `boop-stage-fibres-v179`; built source `e8b7ed4045c0ed7c1eb06b9f2a02da0a5ddf8c16`. GitHub run34931180380 passed; permanent-signed version179 installed and APK-hash verified on Shield and Pixel7Pro. Screen-width loose fibres and crown wisps replace the too-small v178 fringe. Accepted lighting and colour preview preserved. **User visual verdict pending**, including visible phone-face check. See [full receipt](docs/handoffs/2026-09-15-v179-stage-fibres.md). v178 signed rollback retained. Historical entries below describe earlier state.

# v178 user verdict: colour preview accepted; felt depth unresolved

2026-09-15. Ryan explicitly approves the Eyes and animation preview placement and immediate colour changes. Preserve this accepted interaction. Earlier stage-lighting approval remains valid.

Ryan reports very little visible change to the fibres and says the image feels flattened in transit. v178 fringe geometry passes source/numeric checks but has NOT achieved the desired physical depth/fibre appearance. Device fault, display scaling and rendering causes are not established. Do not call the fibre repair physically accepted or blame the devices. Next visual work should investigate actual displayed scale and material detail while protecting the accepted preview, immediate hue response, lighting, artwork and motion. No new app change or installation accompanies this verdict.

Product direction remains whimsical puppet performance with capable assistance behind it. Approved five-digit hand grip and fake-notification diagnostic remain subsequent work.

---

# v178 installed: loose fibres and live eye-colour preview

Updated 2026-09-15. Current owner `boop-felt-preview-v178`. Built source `4f873eadaf65c1fb954aa95e68b5da966263a94d`, signed run34930044029 SUCCESS; installed/hash-verified on Shield and Pixel7Pro. Physical verdict pending.

Ryan likes v177 stage lighting; v178 keeps that shader byte-identical and adds actual loose fibres outside the upper-lid silhouette. Eyes and animation now contains a fixed live BOOP colour preview using the existing hue binding. No timing, voice or sharing-transport change.

[Full v178 source/test/install receipt](docs/handoffs/2026-09-15-v178-felt-preview.md). v177 rollback retained. Shield16of16 saved preference hashes unchanged; phone10of11 unchanged, with notification bookkeeping the only changed file. Original artwork and user eye/voice choices preserved.

Prior entries below are historical.

---

# v177 installed on Shield and Pixel 7 Pro

2026-09-15. Ryan requested both installations and gave go for the phone. Shield updated176to177; Pixel7Pro updated161to177. Both installed packages report `com.boop.alpha1`, version177 / `1.2.177-felt-eyelids`, and APK SHA256 `e8d082480e63fd02f6b391e6a362a1b71fb529061614c36ed821770347a68c8c`, matching signed run34928908920. Existing BOOP entry opened on both. Physical felt appearance and smoothness verdict pending.

Shield: all16 saved preference-file hashes identical before/after. Pixel7Pro:10of11 identical, including eye/appearance/voice. Only `boop_notifications.xml` changed; its current keys are `observed_channels` and `onboarding_seen_v1`. Existing source automatically records observed channels. Before-values were not captured, so the exact key delta is not proven; do not claim all phone preferences byte-identical. No manual settings, permission or data reset. Pixel10ProXL was not updated. Direct ADB worked; no Desktop Commander required.

[Signed build and source receipt](docs/handoffs/2026-09-15-v177-felt-eyelids.md). New grip and fake-notification diagnostic remain subsequent work.

---

# v177 felt eyelids: signed candidate, device verdict pending

Updated 2026-09-15. Current candidate owner: `boop-felt-eyelids-v177`, based on accepted v176 `f64d49b3`. Source `55feb6e3e83d16d8c27b20cad225c24b2ab472f3`; signed [run34928908920](https://github.com/ryankemble2006-web/boop/actions/runs/34928908920) SUCCESS; artifact `10381345601`.

Approved charcoal felt and soft crown light are implemented in the shared upper-lid shader. Original eye/hand assets, silhouette, hue sharing and motion remain protected. Independent source review found no blocker. APK identity/hash/permanent signer and packaged shader verified. No install or physical acceptance yet. Two inherited historical freeze tests remain skipped.

[Full v177 receipt and rollback](docs/handoffs/2026-09-15-v177-felt-eyelids.md). New approved hand-grip integration and fake-notification diagnostic remain next work; Wall notifications are user-reported broken. No notification repair or voice feature is included here.

Prior entries below are historical; current Johnny state belongs to `johnny-ha-native-v8`, whose v14 acceptance supersedes older pending notes.

---

# v176 lyrics album navigation physically accepted

Updated 2026-09-14. Owner branch `boop-lyrics-album-v176`; separate native player work remains on `johnny-ha-native-v8`.

Source `8918a596eeb8bcc17f37fd5344ca7d4ed69793d9`, signed run `34877467478` SUCCESS, artifact `10361900900`. APK SHA256 `f686ca98e3f58d84d5adab3d7bd0647136e9117c290330c6a473c0223b59977e` matches installed Shield package; permanent signer verified. All 16 preferences unchanged, DIRECT 44100 and animator scale 0 preserved. Phone untouched.

Lyrics artwork now accepts Up/OK and opens the current album through the existing album browser; lifecycle cancellation, focus geometry, initial transport focus and seek controls are preserved. User physically confirmed it opened the intended anthology. Test-first failure run34877370210 observed missing action and fan mapping; final source/CI checks and review passed.

Fan classification now excludes oscillation switches, retaining living-room power switch. Actual HA off/on states and one original fan routine were confirmed; native v8 delay prompted separate v9 safe routine interruption. Native v9 is installed with matching APK hash, faster fan physical timing still pending. Original night/day physically accepted. Latest native OI Shrug integration is in progress; consult that branch's current handoff rather than this historical snapshot. Other installed Johnny variants/default screensaver cleanup remains pending final native tests.

---

# v175 Johnny HA state bridge installed; native lab integration in progress

Updated 2026-09-14. Owner branch `boop-johnny-state-bridge-v175`; native player branch `johnny-ha-native-v8`.

BOOP source `a909907502eceec86483972c770e647f9b3826c6`, signed run `34875906124` SUCCESS, artifact `10360637578`. APK SHA256 `0898a1a2261cc99fbd0267b781d19015d6ff6d1d51df92a41892b024ae5a45f0`. Permanent signer verified locally, installed on physical Shield and installed APK hash matched. All 16 existing preference hashes unchanged; DIRECT 44100 output remains present. Phone untouched.

Read-only provider `com.boop.alpha1.johnny_states` accepts only the exact HA lab package with its pinned current signer. It exposes fixed Living Room fan/light states, never credentials or general HA access. Actual unsigned shell call was rejected as expected. Pairing changes invalidate cached access tokens. Empty/missing/unavailable lights are not treated as off; any confirmed on means day, all confirmed off means night. Multiple candidate fans yield unknown pending explicit resolution.

New JVM policy tests first failed for missing implementation, then passed. Signed CI retained voice startup, relaxed groove, routing, lyrics and animation checks. Review found credential-cache issue, fixed and re-reviewed; no source blocker remains. No claim yet of actual HA entity discovery or fan/light physical acceptance: that requires the separately signed native Johnny candidate.

Keep accepted v174 groove/voice/audio behavior. Original Johnny variants remain installed; original remains selected screensaver until HA lab passes joint tests. User authorized cleaning up other installed Shield Johnny variants afterward, while preserving local original resources, scene-board labels and rollback/source. Original resource files and Johnny signing key remain private and are never uploaded.

---

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
