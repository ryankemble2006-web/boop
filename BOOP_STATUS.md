# BOOP Status

## Current handoff — 2026-09-06

### Cancelled voice-to-Deezer Cast route — 2026-09-07

Do not implement the Music Assistant/Deezer/Cast bridge proposed in temporary
2026-09-07 documents: Ryan rejected it because it requires installation,
authorization and target binding, contrary to BOOP's zero-setup, dad-friendly
requirement. The documents were deleted before source/configuration work began.
Google Assistant is being removed and is not an allowed fallback. Existing Deezer
puppetry is untouched; no BOOP app, HA or device state changed.

Read SESSION_HANDOFF.md for the current app state and BOOP_START_HERE.md for
cross-device branch routing. Ryan accepted4fe28a4's lower placement ("awesome
placement"); H1 dancing and pausing with Deezer are physically user-verified.
Full lifecycle/HDR/soak/all-layout acceptance remains separate. Fanart detection
and foreground minimise/return are still unimplemented; investigated metadata
presence and the proposed external UI label did not provide a usable signal.
This synchronization publishes existing preview/context work, not new runtime
behaviour. The older chronological snapshots below must not restart completed work.

## Latest sofa access step — 2026-09-06

H1 play/pause is now user-verified: Ryan reports dancing, fast response and pausing
with the track. His old animator scale0.0 blocked motion; only that scale changed
to1.0 with permission and verified read-back. Ryan explicitly chose to KEEP1.0;
do not restore0.0 automatically. Other system animation scales untouched.
Latest correction: Ryan confirmed the black triangle inside the headband
is the problem and asked to lower H1 enough to remove it. Source now targets25%
across/50% down, preserving x/size/art/nod and ordinary eyes. Full motion stays
below63% height, above the supplied paused bar (~65%+). Geometry test observed RED
before source edit, then GREEN;154 Android/42 source checks and local build pass.
Independent read-only review found no blockers; physical clearance needs sofa test.
Ryan approved publication; exact two-file commit4fe28a4 is pushed to the animation
branch. Fresh154 Android/42 source tests and full local build pass. Signed build
34014467071 PASSED; downloaded APK independently verified (stable signer, package/
permissions/services/unchanged artwork). Deliverable: task outputs/
boop-shield-deezer-lower-4fe28a4/BOOP-Shield-Deezer-Lower-4fe28a4.apk, receipt beside it.
Main verified unchanged; no PR/merge. Private media/dirty notes not published.
No device/install/grant changes; previous APKs preserved for recovery.
Placement sofa verification subsequently passed; broader acceptance remains pending.

Previous signed placement-only change followed comparison of paused/art-present and maximum-
curtain photos. H1 eye pivot now targets25% across/35% down the English Shield view,
same size/art/nod and steady play/pause anchor; ordinary eyes/runtime untouched.
Only HeadphoneGeometry.java and its test changed in application/test code. New
regression observed failing against7d8ca54, then passing. Fresh local154 Android/
42 source tests and assembleDebug pass. Independent read-only review found no code
blockers. Ryan approved the signed release; exact two-file commit9529072 is pushed
to this separate animation branch. Signed build34013239912 PASSED; downloaded APK
independently verified (stable cert/package/permissions/services/unchanged art).
Deliverable: task outputs/boop-shield-deezer-placement-9529072/
BOOP-Shield-Deezer-Placement-9529072.apk, BUILD_RECEIPT.txt beside it.
Main verified unchanged; no PR/merge, private photos/dirty notes not published.
No installation by Codex. Ryan's06:19:55 video now shows H1 in the left-side spot
and clear of track text, but he reports "we were so close...". Curtain edge passes
through the open headband area in sampled frames; Ryan now confirms that black gap
is the objection. Down-only local correction above; no acceptance/checkpoint yet.
Local APK is verification-only. Existing deprecation warnings remain; lint not
rerun or claimed green. Private video/frame evidence saved in task outputs. No checkpoint.
Automatic minimise/return for Kodi is separate and unimplemented: for now this
fixed H1 placement also applies with background Deezer. Existing END gravity is
retained; this placement targets the user's English/LTR Shield, not RTL proof.

Ryan reports installing the Deezer build and enabling its feature. Android's access
screen is absent on this Shield. After informed explicit approval, Codex granted
only BOOP's Deezer notification listener for user0; read-back confirms the addition,
all previous grants preserved, and the listener bound to Android. Probe remains off.
No install/reset/pairing change or playback command performed by Codex. Physical
H1 rendering/play/pause user-confirmed; other physical cases pending, no checkpoint sealed.

Future requirement: first-startup guided setup with minimal button presses and clear
consent, not a buried Settings route. The Shield-specific no-settings-screen path
still needs design/verification; no silent grant or new onboarding code implemented.
This section supersedes the build-time access-pending snapshot below.

## Animation worktree update — 2026-09-06

This worktree is `boop-shield-media-puppetry`, based on `d3d7f7e`. The approved
H1 headphones and P1 popcorn motion preview is built and locally tested at the
user-approved 1.2x pace. Deezer integration is reviewed and committed as `7d8ca54`
on this separate branch. Stable-signed GitHub run `34010543556` passed; root verified
the downloaded APK's certificate, package/permissions/protected services and H1 alpha.
Deliverable: task `outputs/boop-shield-deezer-7d8ca54/BOOP-Shield-Deezer-7d8ca54.apk`,
with BUILD_RECEIPT.txt beside it. Feature defaults OFF. Nothing installed or granted;
no new physical checkpoint. The separate local debug candidate is non-installable.

A separate throwaway probe physically verified Deezer play/pause/resume/next
callbacks, including short background delivery. Its temporary notification access
has been revoked and the original other-listener baseline restored. Kodi/Forki
foreground behavior and production permission UX are not verified. Working BOOP's
overlay, Home, Routines, pairing and app data were not changed by that probe.

Ryan approved Deezer-only first, then a physically tested checkpoint, before popcorn
or the saved Armin and any-Queen-track/Freddie Easter eggs. The written design is
`docs/superpowers/specs/2026-09-06-shield-deezer-puppet-design.md`; Ryan approved it
with "happy" and selected helper agents. Reviewed execution has begun using
`docs/superpowers/plans/2026-09-06-shield-deezer-puppet.md`. All four tasks and the
whole-change/final-fix reviews are complete (153 Android / 42 source tests freshly
verified by root, build passed). Reviews fixed cached-animation-setting and
reentrant publication races, added conservative Power Saver holds, and freeze
motion if animator observation is unavailable.
Lint remains 3 errors/22 warnings, semantically identical to a separately built
unchanged baseline. Ryan explicitly approved retaining those documented issues in
the signed test build. He approved publishing only reviewed Deezer + required
H1 changes on this separate branch and running stable-signed CI after checks and
reviews. No main merge, installation or Android access change is authorized. See
`BOOP_MEMORY.txt` for evidence boundaries and the current direction. The older
Current/Green/Next sections below are the inherited September 5 snapshot, not
instructions to restart BOOP Wall resurrection in this animation worktree.

## Current
Routines v1 is wired end to end on the existing authenticated Home Assistant WebSocket.
The compatibility correction restores the original BOOP contract: Home Assistant automations, scripts and scenes all appear as `Routine`, and automations run through exact-target `automation.trigger`. Routines v1 is physically green on signed Shield build `3fa18c6`: all three available automations discover and run, feedback shows `Running…` -> `Done` -> the normal `Routine` row, remote navigation is correct, and the protected Home favourite path remains healthy. This Home Assistant currently exposes no script or scene routines, and three rows do not require scrolling, so those physical cases were not applicable.

## Green
- Shield Home launch/pairing/room discovery
- Real Home Assistant favourite discovery
- Remote navigation
- Physical Home Assistant control
- Real `state_changed` confirmation
- Routines Task 1: whole-house automation/script/scene discovery
- Routines Task 2: scene execution
- Routines Task 3: truthful script completion using exact-target `on -> off`
- Routines Task 4: controller state, timers, timeout and concurrency
- Routines Task 5: scrollable remote-first TV routines list implemented
- Routines Task 6: Activity composition on the existing authenticated Home Assistant WebSocket
- Routines automation compatibility: all three existing Home Assistant automations physically discovered and activated from the Shield UI
- Exact-target automation start confirmation and `Running…` -> `Done` -> normal-row feedback physically verified on Shield build `3fa18c6`
- Routines remote navigation physically verified: Up/Down traversal, Left return to the Routines rail item, and stable row ordering all work without issue
- Protected Home regression physically verified: correct favourite changes, its real state appears almost immediately, and the 10-second confirmation safety window has ample margin

## Next
- Execute the approved BOOP Wall resurrection plan in
  `docs/superpowers/plans/2026-09-05-boop-wall-resurrection.md`
- Restore Alpha 6.5.5's 33 natural BOOP wake phrases, BOOP OpenCode conversation, immediate local
  Home Assistant control and direct media control with the newer QR pairing and
  stable signing
- Exclude timed voice routines pending a separate redesign
- Do not create a BOOP Wall checkpoint until the signed APK passes the physical
  acceptance gate on the Pixel 7 Pro

## Do Not Touch
- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- HomeAssistantRepository unless a failing regression proves a change is required
- FocusCardView unless a failing regression proves a change is required
- BoopOverlayService / protected overlay runtime behaviour
- Do not create a second Home Assistant socket for Routines

## Active Branch
boop-shield-media-puppetry

## Latest Physically Green Routines Code Head
- 3fa18c6 — fix: confirm Shield automation starts

## Latest Checkpoints
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-3fa18c6 — signed and physically verified Shield Routines v1
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
