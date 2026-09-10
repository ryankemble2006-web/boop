## v101 post-reboot Cast recheck

Ryan initiated Deezer casting after the accepted Home/reboot test. Read-only
physical inspection confirmed the Cast receiver foreground and playing session;
native Deezer was paused. Agent manually viewed one upper-right headphones corner,
clear of artwork and track text. No playback or screen-navigation commands sent;
music left running and temporary device screenshot removed. This closes the
agent-observed v101 post-reboot Cast regression check. Ryan's Home/reboot acceptance
remains scoped; no blanket release or protected-checkpoint replacement implied.
Source be20d50ed3ee72ec7183215ade63e6253e929653; signed run 34461331633.

## v101 Home routing physically accepted

Ryan reports that the physical Home button opens the new unified Home and that
reboot also returns to the new launcher. This is physical acceptance of v101 Home
routing and reboot behavior, source be20d50ed3ee72ec7183215ade63e6253e929653,
run 34461331633, artifact 10145803647, APK SHA256
255832a013383b9e964f66fb398056f05765b748bc9f9acd7baab94353f1730d.
It does not imply blanket acceptance of other features. Preserve v23 rollback.
Post-reboot read-only inspection found native Deezer playing and foreground;
no Cast session. Asked Ryan to initiate casting for the remaining v101 corner
regression check. No playback or foreground command sent during this follow-up.

## v101 approved Shield installation and Home migration

Ryan explicitly approved v101 installation and replacement of the old Home override.
Install succeeded; package reports 101 / 1.2.101-unified-home-button. Replaced only
standalone v23 HomeOverride with com.boop.alpha1.BoopHomeOverrideService. Preserved
Button Mapper and BOOP Cast visibility. v23 remains installed for rollback; stock
launcher remains installed and enabled. Prior accessibility list saved privately.

Injected Android HOME key on physical Shield opened the unified package's
ShieldLauncherActivity. Agent manually viewed Home with one Now Playing puppet.
Launcher Settings > Home Assistant controls displayed the authenticated dashboard;
Back twice returned to Home without fullscreen headphones. No house commands sent.
Open player returned to native Deezer; active native media session reported playing.
A hierarchy inspection/reconnect did not steal foreground back to Home. Agent
screenshot confirmed native Deezer had no corner. No pause/restart/selection sent.
Temporary device probe files removed and controls returned to Ryan.

These are agent-observed physical-device checks. Ryan's physical remote-button
acceptance, reboot behavior and v101 Cast regression remain pending. No Cast session
was active during this check; preserve prior v100 Cast evidence without claiming a
v101 Cast pass. Protected v91 voice checkpoint and other rollbacks unchanged.

## v101 signed artifact and emulator receipt

Source be20d50ed3ee72ec7183215ade63e6253e929653, version 101 /
1.2.101-unified-home-button. GitHub run 34461331633 SUCCESS; artifact 10145803647.
197 Unified +58 Shield functional tests, zero failures/errors/skips. New Home
registration and shared/Home policy checks passed. No GitHub visual tests.
APK SHA256: 255832a013383b9e964f66fb398056f05765b748bc9f9acd7baab94353f1730d.
Artifact ZIP SHA256: 4706fe391d7da228583d2dc29abc96b86f1b1fb364c2f7965d0c91655a178b46.
Permanent signer: f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
Downloaded source/hash/version/package/entry/signer independently verified.

Exact signed APK installed on owned Pixel 7 Pro API36 emulator. Manual inspection:
Voice > Device and room settings > Shield Home > Launcher Settings > Device and
room settings > Home button setup reaches its explanatory dialog and Android
Accessibility settings. Android lists BOOP unified Home button OFF alongside Cast
corner OFF. No access granted. Back returns to BOOP. Wall profile restored and
verified, app stopped, temporary device files removed. Phone profile-transition
capture briefly clipped dialog action text; settled tree exposed full actionable
Open settings button and it opened correctly. TV layout remains a physical check.

APK delivered locally as BOOP-v101-unified-home-button.apk. Build monitor paused.
Shield remains on v100 with v23 Home override; its Cast evidence remains unchanged.
Pending explicit v101 physical install plus single-override migration approval,
then physical Home, reconnect, authenticated HA Back and Cast regression checks.
No canonical merge or protected checkpoint replacement.

## v101 unified Home workaround candidate

Ryan approved wiring the existing Home hack into Unified after the stock HOME
preference was ignored by Shield firmware. Version 101 registers a distinct optional
BOOP unified Home button accessibility service, reusing the imported implementation
and minimal window-state-only configuration. No content retrieval, key interception,
gestures, microphone, automatic grant, or stock-launcher disabling is added.

The app adapter checks the current profile before reconnect, events and launch, and
routes through UnifiedEntryActivity HOME so profile/media initialization is retained.
The once-per-boot reconnect guard and 350ms foreground debounce remain. Only stock
TV/Leanback Home triggers replacement; native Deezer and Cast windows do not.
Shield profile settings provides Home button setup explaining how to turn off the
old override before enabling the unified one, retaining Cast visibility.

Registration regression failed before implementation and passes afterward. Shared
state and Home policy checks pass locally, including inactive profile, same-boot
reconnect, next boot, unknown boot, stock Home, other apps and own package.
Signed GitHub build and exact-APK emulator verification remain pending. No physical
install or accessibility migration has occurred. Existing v100 Cast result and v23
recovery remain unchanged; physical unified Home and authenticated HA Back pending.

## Approved physical Home preference test: firmware override

Ryan explicitly approved switching Shield Home to unified BOOP while retaining v23.
Android's set-home-activity for com.boop.alpha1/.UnifiedEntryActivity returned
Success, but an immediate resolve-activity still selected stock TV launcher with
priority 2. Therefore the preference did not establish unified Home ownership.
Followed the prepared stop/restore condition: restored stock Home preference and
verified resolution. Existing accessibility list was unchanged throughout: Button
Mapper, standalone v23 HomeOverride, and unified Cast visibility. No stock launcher
was disabled, no competing override enabled, and no playback command sent.

Physical unified Home-button and authenticated HA Back remain unverified. Existing
v100 Cast evidence and protected acceptance are unchanged. Next engineering step is
to expose the already imported HomeOverride capability in unified, review its
profile/lifecycle gates and setup, then build/test a candidate before migrating the
single active override. Do not retry the ineffective preference as if it succeeded.
This approval covered the attempted Home switch; it is not blanket install/access
approval for a new build.

## v100 emulator Home and Back follow-up

Exact signed v100 installed on the owned Pixel 7 Pro API36 emulator. Agent used
Voice > Device and room settings > Shield / TV Home, then Launcher Settings >
Home Assistant controls. Unpaired HA showed its pairing screen. Hardware Back
returned to Launcher Settings; another Back returned to Home, with no fullscreen
headphones puppet. Screens manually inspected; no GitHub visual tests.
This verifies the unpaired navigation path only, not authenticated HA control or
the physical Shield Home button. No permissions or HA credentials added. Wall
profile restored and checked, app stopped, temporary device files removed.

Ryan said "you did awesome.. next" after the Cast verification. Retain that positive
feedback without expanding it into blanket acceptance. Next physical Home-routing
change was subsequently approved and attempted; see the firmware result above.
Unified currently does not declare the imported HomeOverride service; do not simply
enable a nonexistent component or enable competing Home overrides. Inspect the
actual Home routing and prepare a reversible migration before changing it.
Read-only Shield check: Android resolves HOME to stock com.google.android.tvlauncher/.MainActivity;
standalone v23 HomeOverride and Button Mapper are enabled. UnifiedEntryActivity is
an available HOME candidate. Proposed approved test: select Unified as HOME and
disable only the standalone HomeOverride, retaining Cast visibility and Button Mapper.
If firmware refuses the preference, stop and restore; do not disable stock launcher.
Rollback restores stock HOME and the exact prior accessibility list. No routing
change has been made. Physical remote Home and authenticated HA Back remain pending.

Signed v100 source `75516b12ccef4de9faadec139778326897cf0518`.
Run `34458283236` SUCCESS; artifact `10144534569`.
197 Unified + 58 Shield functional tests passed; no GitHub visual tests.
APK SHA256 `dc12df6fdd03e2bc3f80d31818f6c4ece829ae501b8b0b063f908583813f1963`.
ZIP SHA256 `acde0565e7e4af7c6b8209fc02b8f71a58050228e5a4af4a9f6495f24c330a76`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded hashes, signer, package com.boop.alpha1, version 100 and entry verified.
APK delivered as BOOP-v100-cast-only-corner.apk. Ryan explicitly approved installation
and the optional BOOP Cast corner accessibility listener. Installation succeeded;
package reports 100 / 1.2.100-cast-only-corner. Listener enabled while preserving
existing services. Existing overlay and notification access reused.

Agent manually inspected physical Shield screenshots: native Deezer playback had
no corner; Deezer Chromecast playback showed one upper-right headphones puppet;
opening native Deezer while Cast remained playing hid the corner; reopening the
existing Cast session via its launch PendingIntent restored one corner. Cast
reported playing before and after. No pause, force-stop, restart, or track-selection
commands were sent during that transition. Temporary device probe files removed.
Controls returned to Ryan. This is agent-observed hardware verification, not Ryan's
physical acceptance of v100. Focused v100 emulator navigation was subsequently checked below.
Standalone v23 remains the Home-button destination. The earlier intermittent native
Deezer missing-media-session issue is not resolved by this visibility change.
Protected physical checkpoints remain unchanged; v91 remains accepted for voices.

# v100 corrected Cast-only corner policy

Ryan corrected the intended rule: show the corner during Deezer Chromecast music,
NEVER over Deezer's native UI. Its changing screen layout makes collisions unavoidable.
The previous v99 native-Deezer corner demonstrations were the wrong product rule,
not physical acceptance. Home retains its own Now Playing presentation.

Observed live cast evidence: media-session package com.google.android.apps.mediashell,
CAST_APP_NAME metadata Deezer, visible CastWebContentsActivity in that receiver.
Native Deezer session stayed paused. Metadata type was not a usable music signal;
source identity is required instead. No title-based or artwork-position guesses.

v100 gates corner ownership on the visible Cast receiver AND selected receiver
session with Deezer source metadata. Unknown foreground, native Deezer, other apps,
other Cast sources, pause/stop, and unavailable visibility access hide the corner.
Home ownership remains independent. New BOOP Cast corner accessibility service
observes only window-state package events, with no content retrieval, gestures,
Home replacement, playback commands or microphone. Service reconnect starts unknown;
a subsequent receiver window event is needed. Disable/unbind/destroy clears visibility.
Settings offers the optional accessibility setup. No automatic grants or installs.

Version 100 / 1.2.100-cast-only-corner. Observed failing regression reproduced
native playback incorrectly owning the corner, then shared-state/policy tests passed.
Review found no serious blocker; positive window evidence additionally requires
the observed CastWebContentsActivity class, not receiver dialogs. Shared tests
passed after this tightening. Signed nonvisual build complete. Approved v100 is now
installed; physical Cast/native transitions were manually inspected as recorded above.
Broader v100 emulator checks and Ryan's physical acceptance remain pending. Protected
checkpoints unchanged.

## v99 installed Shield handoff check, 2026-09-10

Ryan explicitly approved installing signed v99 on the Shield, then separately
approved the new unified ShieldNowPlayingListenerService notification access.
Install succeeded; package reports 99 / 1.2.99-artist-name-matching. Automatic TV
profile selected. Existing overlay permission reused; standalone v23 remains.
No default-HOME or accessibility remapping changed.

Initial real-device blocker: Deezer displayed playing and Ryan confirmed audible
Queen, but Android media_session reported zero sessions. BOOP therefore had no
playback metadata/ownership. One announced diagnostic Deezer restart restored its
native session; Queen artist URI resumed Don't Stop Me Now. This is recovery,
not a fixed root cause and not an automatic restart in BOOP's command path.
Session-loss recurrence remains unresolved and should not be described as fixed.

Manual physical screenshots and Android state confirmed: one corner over Deezer;
direct v99 Home acquired one Now Playing puppet with correct Queen title/artwork;
pause removed the puppet while retaining paused metadata/transport controls;
leaving Home while paused showed no corner; resume returned state 3. Music left
playing in Deezer, controls explicitly released, temporary probe/screenshot files
removed. No further device controls without coordinating a new test window.

This is agent-observed physical testing, not Ryan's blanket visual acceptance.
Remote Home still routes through standalone v23; direct activity testing does not
prove that cross-package route. Next: explicitly choose whether to switch Home
routing to unified v99, then verify Home-button transitions and HA Back. Preserve
v23 recovery and all protected checkpoints. No application source changed here.
App source b39142a3da44251a334ac5237161aa01c80bfdfb; exact signed receipts below.

## v99 Home/media-corner verification, 2026-09-10

Ryan reports music replies feel more comfortable and play Everytime by Britney
returned a result. This is scoped user evidence; no blanket physical checkpoint.

Exact signed v99 installed on owned API36 Pixel emulator. With explicit approval,
media notification access and overlay access were temporarily enabled and the
profile temporarily set to Shield. A bounded silent shell MediaSession supplied
playing/paused/stopped states; it produced no audio and installed no helper app.
Manual inspection: Home showed one Now Playing puppet; leaving Home for the stock
launcher showed one media corner; pause removed the corner; resuming and returning
Home showed one Home puppet without a duplicate; stopping removed the Home media
card/puppet. Existing shared ownership behavioral checks passed locally.
Android Settings suppresses non-system overlays; its absence there is not a failure.
Forced Shield-on-phone layout changed scale after portrait/landscape transitions;
TV-shaped verification remains pending, so this is not blanket layout acceptance.
The probe was released, temporary files removed, original WALL profile restored,
notification-listener grant revoked and overlay app-op restored to default.
The initial private probe used unsupported Files.readString; corrected to readAllBytes.
That was a probe failure, not an application crash or app-source change.

Read-only Shield package check: Unified com.boop.alpha1 is still v47 and standalone
com.boop.shieldhome is v23. Thus current Shield Home cannot physically verify the
v99 unified ownership integration. No live playback, Home, install or permissions
changed on the Shield. Next is an explicitly approved v99 Shield candidate install
and test window, preserving standalone v23 and existing rollback provenance.
No app code changed in this check; v99 source remains b39142a3da44251a334ac5237161aa01c80bfdfb.

# BOOP v99 artist-name matching candidate

Branch `boop-canonical-rebuild`; version 99 / `1.2.99-artist-name-matching`.
Ryan reports play music is perfect, Bohemian Rhapsody works, but play John Lennon
selected the namesake SCH song. Exact installed version was not independently
identified in this report; this is scoped physical feedback, not blanket acceptance.

The resolver previously let the first exact song title override an exact artist.
v99 compares matching performers across the returned catalogue rows with exact
song-title matches: an exact artist whose music dominates wins; ties retain the
song default. Explicit title by performer stays a track request. No hardcoded
artists, extra requests, device control changes or OpenCode dependency.
This is a bounded catalogue heuristic, not universal natural-language certainty.

Regression reproduced John Lennon selecting SCH, then passed after repair.
A live read-only probe caught the incidental Bohemian Rhapsody artist; a second
regression reproduced and protected that collision. All 27 focused JVM music
tests pass. Live public-catalogue probes select John Lennon artist 226, John Lennon
by SCH track 112736672, Bohemian Rhapsody by Queen track 4091937401, Queen artist
412, Britney Spears artist 483, and local Flow. No live device playback performed.
Done/Failed wording from v98 remains.

Signed v99 source `b39142a3da44251a334ac5237161aa01c80bfdfb`.
GitHub run `34454191785` SUCCESS; artifact `10142921248`.
197 Unified + 58 Shield tests passed with zero failures/errors/skips.
APK SHA256: `99f628ca5e68b505265f1d1da1b49577d3b5126c025fcd6cdbf9c57c83e6e522`.
ZIP SHA256: `1ffbd1af1e31c89332f354543fec4d1e356e3f17b2f907ed7a1d7ae70fc12289`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded hashes, signer, version 99, com.boop.alpha1 package and UnifiedEntryActivity
independently verified. No GitHub visual tests or new device controls performed.
Exact v99 physical acceptance remains pending; v97 emulator evidence is not v99 acceptance.
No UI changes or GitHub visual tests. Protected v91 and canonical remain unchanged.

# BOOP v98 short music replies candidate

Branch `boop-canonical-rebuild`. Ryan requested music confirmations say only
"Done" or "Failed". v98 changes the native Deezer route wording accordingly;
"Done" means the request was dispatched, not acoustically verified playback.
Version 98 / `1.2.98-short-music-replies`. Run `34453509576` succeeded;
superseded for delivery by v99 because of the known catalogue collision.
All v97 native routing and physical acceptance limitations below still apply.
No canonical merge or replacement of the protected v91 checkpoint.

# BOOP canonical rebuild - v97 direct Deezer candidate

Updated 2026-09-10. Branch `boop-canonical-rebuild`; canonical `boop-unified`
and the protected v91 voice checkpoint remain unchanged. Candidate is unmerged.

## Current v97 state

Version 97 / `1.2.97-direct-deezer-playback`, package `com.boop.alpha1`.
Signed source `dd8a2389750968f6eefc90d356c2578bffb2063a`.
GitHub run `34452461627` SUCCESS; artifact `10142207674` (BOOP-Unified).
195 Unified + 58 Shield functional tests passed; no GitHub visual tests ran.
APK SHA256: `7525043a0cf5c39b7af876bb3933d26a02fab6ec4224fb94549c53b287b6b2d0`.
ZIP SHA256: `bad3672db549d77dee817db301133a5c9e45b4ec540fa00e87baf5422596440e`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded ZIP/APK hashes, signer, package, version and entry independently checked.
Exact signed APK installed on owned API36 Pixel emulator; settled Voice settings
screen manually inspected and readable. No AndroidRuntime exception observed.
Emulator app stopped and temporary screenshot removed after inspection.
This is a launch/UI smoke check, not Pixel-to-Shield voice or acoustic acceptance.
Delivered `BOOP-v97-direct-deezer-candidate.apk` in the current task outputs.

Ryan reports v96 commands work, but force-closing/reopening Deezer and its delay
are too clumsy. He explicitly said keeping music playing during navigation feels
acceptable. This feedback does not create a blanket v96 physical checkpoint.

v97 replaces UI navigation with Deezer's native MediaController Play-from-URI.
Public catalogue resolution supplies an official artist or exact track ID;
`play music` uses `https://www.deezer.com/flow` without a catalogue request.
There is no force-stop, pre-pause, hierarchy dump, album scrolling or fixed
startup sleep in the music command path. Existing basic transport and voices stay.

The same configured-room/exposed-TV/unique-ADB/exact-MAC checks precede control.
A small Java helper is compiled to DEX from repository source during materialization.
Its generated payload is sent through the already-authorized local HA ADB action,
written to a unique private temporary shell file, SHA256 checked, made read-only,
executed under the actual com.android.shell context, then deleted by an exit trap.
No installed app/service, provider secrets, account changes, root, permission grants,
new microphone or permanent device configuration. No private provider APIs.

The helper selects exactly one native Deezer media session and requires advertised
Play-from-URI support. It reports requested, not acoustically verified. Cold preparation
can launch Deezer only if its session is absent and polls boundedly for URI readiness;
it never plays. The host rechecks room/epoch after preparation before separate playback.
Unsupported controls, ambiguous sessions, stale output or changed room fail closed.
Newer Android needs process-local media-framework initialization, verified on API36;
this changes no system setting and does not suppress accessibility services.

## v97 verification so far

- 25 focused local music JVM tests pass after observed RED-to-GREEN regressions.
- Source-built helper compiles/D8-dexes. Actual generated payload executes on the
  owned API36 Pixel emulator, returns needs-prepare with Deezer absent, and its
  temporary file is confirmed removed. Initial media-framework bootstrap failure
  was diagnosed and fixed, then this probe passed.
- Read-only code review found two cold-path issues; both were repaired and reviewed.
- Authorized live Shield capability probes used the genuine shell context and
  standard MediaController transport: native Flow selected Bryan Kearney, direct
  Britney artist selected ...Baby One More Time, and exact track URI selected
  Bohemian Rhapsody. The Deezer process stayed alive. These prove the native control
  capability, not the final v97 Pixel-to-HA APK end-to-end or timing acceptance.
- Earlier warm browsing-only experiments were superseded: they retained music but
  could hang Home on repeat Flow. They are not the shipped design.
- Controls were explicitly returned to Ryan, temporary Shield probes removed,
  visible Queen page restored and Bohemian Rhapsody left playing. Do not resume
  audible device tests without coordinating an uninterrupted window.

Next: Ryan tests the exact v97 APK on Pixel through HA to Shield: repeat/switch
artists, Bohemian Rhapsody, and play music (Flow). Final physical acceptance and
voice-to-playback latency remain pending. No automatic Pixel install, canonical
merge or new physical checkpoint. Protected v91 voice acceptance remains intact.

Reference for process-local media initialization:
https://android.googlesource.com/platform/frameworks/base/+/master/media/java/android/media/MediaFrameworkPlatformInitializer.java

## Previous v96 build and preserved provenance

The following receipts describe the prior candidate, not the v97 implementation.

## Exact signed artifact

- Source/build commit: `8a6c2bdb4a01149f86ce464505b2d354d1dde60f`.
- Version 96 / `1.2.96-native-deezer-tracks-flow`; package `com.boop.alpha1`.
- GitHub run `34447810258`: SUCCESS; artifact `10140428216`.
- ZIP SHA256: `65073549029fc3e9712bd1b58768b93d1b2d345d9bac8cf4ce4854a81c584de9`.
- APK SHA256: `8e09e35555a18896b67d40ccf7a7ef662b1feca1062a6fb943d98270b198abde`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Local deliverable: `C:/Users/ryank/Documents/Codex/2026-09-10/the-x20/outputs/BOOP-v96-native-deezer-candidate.apk`.

GitHub passed 200 Unified and 58 Shield focused functional tests, zero failures,
errors or skips, plus existing nonvisual contract/integrity checks. No GitHub
visual tests ran. ZIP/APK hashes, built source, package/version/entry activity and
permanent signer were independently verified after download. The earlier v96 run
34447365574 at 6ae1b144a07324429b10eeb5f793f19218ae882d passed but was superseded
by the live-catalogue ambiguity correction; do not deliver that older artifact.

## v96 music behavior and evidence

Ryan uses BOOP on a Pixel 7 to command the Shield and requires native Deezer.
v95 worked on his first artist request, but repeat/switch attempts only blipped
without changing music. It is not physically accepted for repeat artist control.

v96 supports artist requests, exact song titles with optional `by artist`, and
`play music` -> native Deezer Flow. Existing pause/resume/next/previous and Natural
Voice reply paths are preserved. No Music Assistant streaming or alternate speaker.

Public metadata resolves official numeric artist/album links. Deezer has a separate
artist named Bohemian Rhapsody and multiple artists named Queen: the highest-ranked
track result distinguishes an exact song title from its exact artist name, followed
by exact unique artist or exact playable song matching. Album membership is verified
by track ID. Live production resolver checks returned Queen's original album for
`play Bohemian Rhapsody`, expected artists for Britney Spears and Queen, and local
Flow for `play music`. Song replies include the artist. Public calls carry no HA
credential; Flow and existing basic transport need no public catalogue lookup.

The configured room must contain one exposed eligible Android TV and one ADB media
candidate. Read-only exact MAC matching verifies ADB is the same hardware behind
that authorized TV before control. Ryan explicitly approved adding HA Android Debug
Bridge; it is configured for the Shield in Living Room. Existing Assist exposure
was retained. Private addresses/MACs and raw diagnostic data are not published.

The controller cleanly restarts Deezer to discard stale intents/stuck Home loads.
It uses nonce-tagged ADB responses and a unique temporary hierarchy file per read.
Exact text/content descriptions identify native controls; selection requires focus,
including same-bounds nested button wrappers, and a fresh check after pause before
one DPAD_CENTER. No coordinate taps or fixed track-row counts. Room changes,
interruption, stale output, wrong hardware and changed foreground fail closed.
Navigation is bounded to 25 reads/scroll steps and can take tens of seconds for deep
album rows. Changed provider layouts/languages or unavailable content may fail safely.

Thirty local offline music tests and the shared-state harness passed. Source review
found no remaining serious issues after stale-file, focus and test-coverage repairs.
Manual Shield component checks selected Bohemian Rhapsody, Flow, Britney Spears
and then Queen with native playing indicators. Final clean restart was exercised
for Flow and artist switching. These are component/device observations, not v96
Pixel-to-HA end-to-end or acoustic acceptance. Queen was left playing.

Hierarchies are transient control data, not conversation input or app-persisted data.
Home Assistant itself keeps the latest `adb_response` as documented. Temporary
Shield files were removed, HA action editor cleared and template reset to its demo,
then Overview restored. No provider account, new app permission or microphone change.

## Installed launcher compatibility repair

Android's standard hierarchy dump reconnects enabled accessibility services. The
installed standalone ShieldHomeOverrideService unconditionally launched Home on
reconnect; logs/source confirmed the interruption while Ryan left the remote alone.
The minimal fix rearms once per device boot while preserving stock-Home event routing.
No artwork or permission/content-access changes. The same repair is in unified source.

- Existing standalone branch: `boop-shield-clean-launcher`.
- Source: `ccb10658bded07dbf2a91ee8234509999f960a49`; receipt HEAD `83925ecd1acb86d3b6ef351669f8d22384d86329`.
- Signed version 23 / `0.10.8-service-reconnect`; package `com.boop.shieldhome`.
- GitHub run `34446163444` SUCCESS, artifact `10139755652`; two focused policy tests passed.
- APK SHA256: `6bcc46633c38f61f4c7c5b4b0a49a4d3eec7b05818a25ef29abd4d36e82f411e`.

Ryan explicitly approved installing this exact update. Installation succeeded after
saving the actual v22 APK privately; its SHA256 is
`62886af8b7bac55bebb019555aba3f3b08bc0f060ae53780133b15b8a20f4bf0`.
Repeated hierarchy reads now keep Deezer foreground. Reboot and single/double Home
physical acceptance remain pending; do not infer a new blanket physical checkpoint.

## Manual emulator check

Exact signed v96 installed on the owned Pixel_7_Pro_API_36 emulator. Agent-viewed
screens confirmed Voice, scrolling, and the device/room settings route through its
visible button. Back was exercised. No AndroidRuntime error appeared in
the inspected recent log. No HA credentials or voice packs were installed for this
check. BOOP was stopped afterward; v96 remains installed with the prior Wall profile.
Other emulators, the Shield's eyes task and locked assets were untouched.

## Selected rebuild scope retained

Ryan selected items 1, 3, 6, 7, 8 and 10: shared immutable state/ownership; configurable
HA room and exposed generic device discovery; existing Shield Home/Now Playing source
inside the unified APK with one media corner/Home owner; local media and Deezer routing;
HA/TV Back behavior; first-run and later profile/room settings. Eyes/blink and Turbo
redesign remain separately owned. See the scoped plan and unified/SOURCE_HEADS.md.

Original rebuild base: `boop-unified@99474d141e7affad17cdbe854e94dd3986076980`.
v94 source `e683b26e04a3bc2bb8ba5a94ee23eeb2380d1ec0` had positive Ryan reports for
HA, basic music controls and Natural Voice on conversational/HA replies. v95 source
`61d7604e1f923c1789aba94afb0d76831bed073d` added native artists but failed repeats.
Historical exact receipts and diagnostics remain in Git history at `14b9f4a`.

## Protected acceptance and next action

Ryan physically accepted v91 natural voice installation, selection, demos and a
normal selected-voice reply. Source `11650313221ae5bf997dbb93b6a905bfdc7da1ed`,
checkpoint `checkpoint-boop-unified-v91-natural-voices-accepted`, APK SHA256
`42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`.
Preserve exact v91 Desktop/server TEST artifacts and v88 rollback
`f5f086fc4f67712b5746be067aff852331299bb0`. No canonical merge or checkpoint replacement.

Ryan subsequently reported v96 commands working but rejected the restart/delay;
see the current v97 entry above. Broader room/device, microphone and physical
acceptance remain pending. No prior protected checkpoint was replaced.

Fetch explicit owning-branch and main refspecs before continuing; this repository's
fetch configuration omits some branches. Live main at verification was
`8d29d2bada1b8710ec030debce52c1976c990e56`; personality continuity was reread and no new
durable personality fact required a shared edit. Source/build commit above remains
separate from later documentation-only HEAD; verify live GitHub before claiming sync.

References: https://www.home-assistant.io/actions/androidtv.adb_command/ and
https://www.home-assistant.io/integrations/androidtv/ .
