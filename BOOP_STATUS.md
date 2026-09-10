## v107 installed with approval; physical command result pending

Ryan approved the exact v107 deployment. Delivered APK hash matched
2f8c9905374e1757ed47a06c880bf7e964622b065485ac06ea54a74c5a958b7b;
adb install -r succeeded and versionCode107 was verified. RECORD_AUDIO remains
user-granted, assistant remains BoopAssistantActivity, recognizer remains Katniss.
UnifiedEntry opened for the coordinated remote test. No new permission/role changes.
Successful Shield speech/house/media control remains unverified; next is one real
remote command and inspection of its result. Earlier accepted checkpoints preserved.

## v107 signed and locally verified; physical install pending

Source2c9429e469781a21a177ab4a853e5da6a20322f6; GitHub run34486358015 SUCCESS,
artifact10155974086. 217 Unified +58 Shield tests passed, zero failures/errors/skips.
Local apksigner/aapt/hash verification confirms com.boop.alpha1, version107 /
1.2.107-shield-voice-connection and permanent signer
f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
APK SHA256 2f8c9905374e1757ed47a06c880bf7e964622b065485ac06ea54a74c5a958b7b.
ZIP SHA256 3e2c76e96ce8f45eb0d2bf90c87a53f9286197a087d6f2a38d8a707c728e3dd5.
Delivered BOOP-v107-shield-voice-connection.apk. Not installed or physically tested.
Shield remains v106; successful remote commands remain unverified. Review's stale
pairing return issue was fixed by finishing voice after opening Shield dashboard.
Next: approved exact-artifact installation and coordinated remote-command test.
No GitHub visual tests. Build monitor paused after artifact verification/delivery.

## v107 candidate: Shield remote voice reuses existing house pairing

Ryan reported v106 opens microphone/BOOP but play Elton John and turn on candle
performed no action. Recognition service logs returned speech results; no new
BadToken crash appeared in the inspected window. Actual transcription and commands
are not accepted. App-private preference filenames confirm old Wall boop-ha absent
while Shield pairing store exists. Voice MainActivity was using Wall SecureTokenStore.

Approved repair: on Shield only, load the existing encrypted dashboard credential
into an in-memory server/client-id/refresh-token snapshot. Keep its issuing OAuth
client ID when refreshing. Do not copy/rewrite the persisted dashboard credential.
Normal non-Shield voice keeps its existing connection. Voice registration metadata
is stored separately and scoped to the paired server; existing voice device/room
setup still establishes the HA identity. Missing pairing opens Shield's dashboard.

Local focused OAuth probe passed after missing-class RED. Added JUnit cases cover
issuing client, token escaping, unchanged Wall identity and rejecting missing client.
Shared state/Home/close-gate and delayed-dialog lifecycle checks passed. Version107
candidate; full materialization/build/signing and physical command result pending.
Ryan permits taking control for tests; music was left playing during source work.
No v107 install or credential/device mutation performed by this source work.

## v106 installed with user approval; remote retest pending

Ryan approved the exact delivered v106 installation. APK SHA256 matched the signed
receipt, adb install -r succeeded, and device versionCode106 was verified.
User-granted RECORD_AUDIO remains granted. Assistant remains BoopAssistantActivity
and recognition remains KatnissRecognitionService. UnifiedEntry opened for the
coordinated remote-button test. No additional permissions or role changes made.
Installation is verified; no physical crash-fix or command acceptance claimed yet.

## v106 signed artifact verified; physical retest pending

Source 0a83c69b5eaac0eb683b468bc4fb1e5c294db4f0 on boop-canonical-rebuild.
GitHub run34482554547 SUCCESS, artifact10154388761. 211 Unified plus58 Shield
functional tests passed with zero failures/errors/skips, and lifecycle regression
passed. No GitHub visual tests. Local apksigner/aapt/hash checks independently
confirmed package com.boop.alpha1, version106 / 1.2.106-assistant-lifecycle and
permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
APK SHA256 c6b3883d332c000e79c75356ef6462969e1cfe7defc26761b53a02ed55546a55.
ZIP SHA256 2089754dfe4b7442ac786bf44b2b116db40423c101f92be55c651bc91b31723e.
Delivered BOOP-v106-assistant-lifecycle.apk. Not installed or physically accepted;
Shield remains v105 with user-approved microphone permission. Next: coordinated
v106 install and remote test after approval of the ready artifact. Full Shield
voice/HA routing remains unverified. Build monitor paused after artifact delivery.

## v106 lifecycle repair candidate; v105 remote test found a BOOP crash

Ryan approved microphone permission in Android's prompt, saw BOOP and heard the
microphone beep, then saw the assistant disappear. Live Katniss logs show BOOP's
recognition request opened the microphone, detected speech and returned a result.
This does not establish accurate transcription or successful Shield house control.
At 14:21:45 on 2026-09-10, BOOP crashed with WindowManager.BadTokenException:
MainActivity.showConnectPrompt tried to show an AlertDialog on a finished activity
from a queued Home Assistant discovery callback. This was a BOOP process crash.
The preceding reported amp/display restarts correlated with repeated HDMI plug
changes; system_server retained its PID and no new system crash was recorded.

v106 candidate adds finishing/destroyed guards at the actual dialog entry point.
The nonvisual Java harness executes that source method with a window-lifetime fake:
RED reproduced a closed-window assertion before the fix; GREEN passed active,
finishing, destroyed, duplicate and already-connected cases afterward. CI runs it.
Version 106 / 1.2.106-assistant-lifecycle. Build/signing/install/device retest pending.
No further physical install, role change, permission grant or UI test performed.

Scope limit: the crash is repaired in source, not physically accepted. The one-shot
ends before discovery completes, and Shield's existing HA connection versus Wall's
voice connection still needs investigation. Do not treat this narrow guard as proof
of complete remote speech/control integration. Preserve v105 receipts below and
all earlier physical close-player/room/voice checkpoints. Eyes remain separate.

## v105 installed: Shield assistant role assigned successfully

Ryan approved exact v105 installation and assignment. APK hash verified, install
succeeded and version105 confirmed. Before role change, live package queries
showed no VoiceInteractionService and one DEFAULT ASSIST BoopAssistantActivity.
Role assignment succeeded without the v104 system restart. Live assistant setting
is com.boop.alpha1/.BoopAssistantActivity, voice interaction setting is empty and
the original KatnissRecognitionService remains selected. Opened UnifiedEntry/Home.
This verifies physical installation and role configuration only. Ryan must press
the real remote microphone button to establish activation and audio routing.
No microphone permission was granted by these operations. Existing local use_boop
opt-in was retained. Original assistant was Google Katniss for rollback.

## v105 signed: emulator assistant assignment and entry verified

Branch boop-canonical-rebuild; source af66eccd56dda7b12e8fa86d1c1f53d07678f49d.
GitHub run34480607501 SUCCESS, artifact10153558430. 211 Unified +58 Shield tests,
zero failures/errors/skips, plus shared/Home/close-gate checks. Version105 /
1.2.105-activity-assistant, package com.boop.alpha1, entry UnifiedEntryActivity.
Local package/version/source/hash/permanent-signer checks passed.
APK SHA256 deda81432440dbe9de0a9f91728acc6e3136bfc51e95aa913529cac9e4ce926c.
ZIP SHA256 8a60c930c0439bf2847fb49d9f41ebc1c882356820821a52f8ec032b95682b32.
Signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.

Actual owned Pixel7Pro/API36 emulator install and role-manager assignment passed:
ASSISTANT holder=com.boop.alpha1, assistant=.BoopAssistantActivity,
VOICE_INTERACTION_SERVICE empty, original GoogleTTSRecognitionService unchanged.
No system restart observed. A temporary emulator-only use_boop preference fixture
enabled real activity entry. ACTION_ASSIST forwarded one-shot into MainActivity;
BOOP-Assist log preserved synthetic inputDeviceId=123 and face was manually viewed.
This proves forwarding, not remote audio capture or successful house commands.
No HA credentials or microphone permissions were granted. App stopped; fixture
removed (originally absent); Google role, original assistant/interactor/recognizer
restored and verified. WALL profile unchanged; temporary screenshot removed.
No GitHub visual tests. Other emulator and physical Shield untouched by this test.

Activity-based assistant supersedes malformed voice-service registration for v105.
Review found one inherited receiver replay risk; MainActivity now consumes the
one-shot extra and skips accepting it during recreation. Local entry-policy matrix
and CI pass. Historical voice-service sources are unregistered, retained only as
reference. Android keeps its default recognizer, no new provider or microphone loop.

Delivered BOOP-v105-activity-assistant.apk. Build monitor paused after delivery.
Shield now runs v105 with its activity assistant assigned, as recorded above.
Next: physical remote microphone press. Audio, command routing and user acceptance
remain pending. Preserve original Katniss settings for rollback.

## Shield assistant assignment trial failed: Android system restart

Ryan confirmed turn off the candle succeeded through Pixel. This verifies that
Pixel command route only; it does not validate Shield remote microphone capture.
Ryan approved assigning BOOP to the Shield microphone button for a trial.
Initial assistant/interactor was Google Katniss; recognizer also Katniss.
Standard VOICE_INPUT_SETTINGS could not resolve. BOOP's existing chooser saved
Use BOOP, but Android RequestRoleActivity rejected ASSISTANT as not requestable.

Then adb cmd role add-role-holder --user 0 android.app.role.ASSISTANT com.boop.alpha1
triggered a fatal exception in Android system_server at 13:58:47 device local time:
NullPointerException: class name is null, ComponentName constructor, followed by
VoiceInteractionManagerService RoleObserver.onRoleHoldersChanged. Android/ADB
restarted; Ryan saw the Android startup screen then Home. Do not repeat this command
against v104. This was not a BOOP application-process crash or successful mapping.

Root-cause evidence: generated boop_voice_interaction_service.xml declares a session
service but no android:recognitionService. Android 11 AOSP RoleObserver constructs
ComponentName(pkg, voiceInteractionServiceInfo.getRecognitionService()) without a
null guard. The installed BOOP voice-interaction service is discoverable. A genuine
valid recognizer integration is needed before retrying the role assignment; do not
fill in a dummy or cross-package class merely to suppress the crash.
Reference: aosp-mirror/platform_frameworks_base android-11.0.0_r1,
services/voiceinteraction/java/com/android/server/voiceinteraction/VoiceInteractionManagerService.java
lines1564-1565. Source fetched read-only; raw logs retained privately, not published.

Recovery verified: sys.boot_completed=1, ASSISTANT role and both assistant/interactor
settings remain Google Katniss. Home override, Cast visibility, Button Mapper and
EastEnders accessibility entries remain enabled. BOOP's local Use BOOP preference
is selected, but that does not mean Android assigned it. No app build or permission
grant occurred. Installed v104 and accepted close/room behavior remain separate.
Next: repair and validate assistant recognition metadata/provider routing before a
new explicitly coordinated physical assignment test. Remote capture is unverified.

## v104 installed: authenticated room shortcut and Back physically checked

Ryan explicitly approved v104 installation and room/Back checks. Exact signed APK
hash verified before install; installation succeeded and version104 confirmed.
After package replacement stock Home appeared initially; explicit UnifiedEntry
launch restored BOOP Home and Now Playing. Existing accessibility entries including
Home override, Cast visibility and EastEnders remained enabled; no grants changed.

Actual Set this device's room shortcut opened live HA room choices using the
existing Shield connection, without the former Wall-connect error. Back returned
to BoopProfileActivity, confirmed by fresh resumed-activity output and screenshot.
Living Room / living_room remained saved. One earlier UI dump failed to reach idle;
its stale XML was not counted as evidence, and the check was repeated successfully.
Returned Home and removed temporary device files. No playback commands were sent.
These are agent physical route/cancel checks, not blanket user acceptance of v104.
Live command matching and cross-room controls remain pending; protected v103 close,
v101 Home/reboot and v91 voice acceptance retain their original scope.

## v104 signed room/device artifact receipt

Branch boop-canonical-rebuild. Final combined build source
36651387ee2c30e7e3fda0b97e2142168659a3f9; GitHub run 34474767283 SUCCESS;
artifact 10151147276. 207 Unified +58 Shield functional tests, zero failures,
errors or skips, plus shared/Home/close-gate checks. Scoped reviews found no
important defects. Four resolver regressions reproduced RED then passed locally.
Version 104 / 1.2.104-device-name-matching; package com.boop.alpha1;
entry com.boop.alpha1.UnifiedEntryActivity. Local aapt/apksigner checks passed.
APK SHA256 dcb8d4bf39cdd8084015550b1c405d9dcef3877cc63f5d313c6439d947682477.
ZIP SHA256 9b6f68f4c87183e5ce962476829566a371378348c3b598e307a7dab55bf9ab84.
Permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.

Device matching uses nonempty Unicode whole-word phrases, preventing lamp/clamp
and blank/non-Latin collisions while retaining ordinary phrase matches. Exposure,
availability, room filtering, ambiguity and HA Assist fallback stay unchanged.
Shield profile room shortcut reuses existing HA picker after its normal pairing
gate instead of incorrectly requiring the separate Wall connection. Current room
is retained until selection. Back cancels. On activity recreation after the one-shot
extra is consumed, HA Home may reopen instead of the picker; saved room is retained.

Manual owned-emulator check: v104 installed; settings/profile route opened Shield
HA pairing flow (emulator is unpaired), Back returned to profile settings. No
credentials or permissions added. WALL restored and verified, app stopped and
temporary device files removed. No GitHub visual tests. Authenticated shortcut
operation and live device-command matching require physical checks.

Before the approved v104 installation, Shield ran v103. Ryan reported close player perfect, instant and nearly
invisible; exact native/Cast route was not specified. Existing v101 Home/reboot and
v91 voice checkpoints remain protected. During room audit, Ryan confirmed an earlier
test room and requested Living Room. The v103 shortcut failure was reproduced;
existing Shield HA Settings > Room picker restored Living Room and canonical
living_room was verified. Home returned, music transport was not changed.
Broader cross-room command and profile acceptance remain pending.

Delivered locally as BOOP-v104-room-device-fixes.apk. Monitor paused after delivery.
Authenticated shortcut and Back/cancel checks are complete as recorded above.
Next remaining scope: physical current-room and cross-room voice/device controls.
The earlier name-only v104 run34474085300 was not delivered or installed.

## v103 installed: native cleanup physically tested; Cast check pending

Branch boop-canonical-rebuild. Build source b075370d56eb7fcc209e2ab078682ce05878a7c7.
GitHub run 34471952390 SUCCESS; artifact 10149989572. 203 Unified +58 Shield
functional tests, zero failures/errors/skips, plus shared/Home/close-gate checks.
Version 103 / 1.2.103-unified-close-media-apps; package com.boop.alpha1;
entry com.boop.alpha1.UnifiedEntryActivity. Source and identity verified locally.
APK SHA256: d44a9b01ae7a930dcf57e81d7c864528e71831d2b575490a50cf70c25d280b01.
ZIP SHA256: 495b121a8b043c825f19f75fc8fdc5f0d8bb2edfc976d8cd8a8047ca8945d136.
Permanent signer: f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
Local apksigner and aapt verification passed after sandbox execution retry.

Manually inspected on owned Pixel 7 Pro API36 emulator: persistent Close media
apps button is readable on Shield Home without Now Playing. Actual button opened
BoopClosePlayerActivity and showed Failed as expected without media-listener access.
This does not prove successful physical cleanup. No permissions granted. Restored
and verified WALL profile, stopped app, removed temporary device files. Other
emulator and physical Shield were untouched during v103 verification.
No GitHub visual tests. Signed APK delivered as BOOP-v103-unified-close-media-apps.apk.

Ryan approved a separate Home action that remains available without Now Playing.
It stops active Cast through advertised transport STOP and closes native Deezer
and YouTube through the existing authenticated HA ADB route. A random app-private
marker proves the local Shield and is rechecked between native stops. Back revokes
pending authorization. Fresh Android session queries verify cleanup; Done/Failed
is shown. Missing notification access or unsupported Cast STOP fails closed.
No shared Cast receiver force-stop, new permission, or default-home change.
Scoped review found no important defects. Build monitor paused after delivery.
Ryan approved installation and a short physical test. Signed v103 installed on
Shield and version verified. Opened native Deezer and YouTube; both processes and
two inactive media sessions were confirmed while Home had no Now Playing card.
Pressed the actual persistent Close media apps button. Android logs confirmed
force-stop of both packages; both processes disappeared, media sessions became
zero, and Home resumed. App-private close markers were removed. This is an agent
physical pass for both native targets without a Now Playing card, not user acceptance.
Existing accessibility services (including the separate EastEnders service), Deezer
device-idle exemption, Home settings and rollback packages were preserved.
New button's active Cast path remains pending; requested a user-started Cast session.
User's v101 Home/reboot acceptance remains the protected acceptance checkpoint.

## v102 physical checks: native Deezer and regular Deezer Cast pass

Installed signed v102 with Ryan's approval. Actual Home Close player button
force-stopped native Deezer, removed its process and media session, returned Home,
and removed its private marker. Regular Deezer Cast displayed one corner BOOP;
Home showed current metadata. Close player left zero media sessions and receiver
logs confirmed app stopped, audio focus abandoned, and all resources released.
These are agent physical checks, not a new blanket user acceptance checkpoint.
The regular receiver was Deezer; v102 Deezer - Beta corner remains unobserved.

YouTube played before Home. Home destroyed its media session and released its
Cast resources but left the native YouTube process running. The Now Playing card
therefore disappeared before its Close player button could be used. This is the
reason for the approved persistent v103 action, not a passing YouTube button test.
Leftover YouTube and temporary device files were cleaned up after testing.
Existing Home/reboot acceptance, Deezer exemption, accessibility services including
the separate EastEnders service, and rollback packages were preserved.

## v102 signed artifact receipt

Owning branch boop-canonical-rebuild. Final build source
1979b6d8ddc298d068c34fa01427efac20ca4dbc; GitHub run 34467435056 SUCCESS,
artifact 10148228337. 202 Unified +58 Shield functional tests, zero failures,
errors or skips, plus shared/Home/close-gate checks. Review and re-review complete.
First run 34466946285 at c9da55f44345733729344fef799d0b4af52204c5 omitted the
five new client tests from its filter; the final source corrects that omission.

Version 102 / 1.2.102-unified-close-player; package com.boop.alpha1.
APK SHA256: 210ddb3e2be904329f4febfe0f7f6a28a8b826927045bb3ebc87b3e668700ff5.
Final ZIP SHA256: db08932d9c74bac3465152f002152ee812e86e98a3e158f1580539ed30fb4188.
Permanent signer: f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
Downloaded source, version, package, entry, signer and artifact hashes verified.
Final APK is byte-identical to the first v102 APK manually inspected on the owned
Pixel 7 Pro API36 emulator. Voice/settings and Shield Home launch were viewed.
The emulator has no HA pairing/active media session, so Close button operation and
Cast corner appearance are NOT validated there. Attempted private activity start
through run-as/am was rejected by Android caller-package validation, without
exercising the activity. No permissions were granted. WALL profile restored and
verified, app stopped, temporary device files removed. Other emulator untouched.

Delivered locally as BOOP-v102-unified-close-player.apk. Build monitor paused.
Physical Shield now runs v102; physical scope is recorded above.
Home/reboot acceptance and Deezer exemption remain unchanged.

Ryan approved replacing the Now Playing Open player button with Close player:
terminate native Deezer or the active Cast source, including YouTube, because
paused sources can still compete for Shield audio. Preserve artwork's separate
open-source action. Do not label pause or unverified transport STOP as a completed
receiver shutdown. Candidate v102 implements a non-exported close activity, separate
button callback, same-session validation, Back cancellation and fresh Android token
absence verification. Native Deezer/YouTube use the existing HA ADB route with an
app-private random marker proving this local Shield before force-stop. Cast uses
the selected receiver's advertised STOP action. Unsupported/failed actions say
Failed; no shared receiver package force-stop. Artwork still opens its source.

Repeated user Cast -> Home test captured: YouTube TV abandoned audio focus and
destroyed its media session about one second after Home, then Cast reported
STOP_BY_APP and released that receiver session. BOOP remained foreground; paused
native Deezer remained the only media session. No BOOP fatal exception appeared
in the captured interval. This explains the brief YouTube card without proving
why YouTube ended playback. Landscape artwork rendering remains unverified.
A prior later task-manager removal by Ryan is separate from this clean repeat.

Prior accepted Home/reboot checkpoint was signed v101, source
be20d50ed3ee72ec7183215ade63e6253e929653; Home/reboot acceptance is unchanged.
Ryan confirmed native Now Playing had data and was visible after the earlier
Deezer Cast/native transition with the exemption enabled. Longer-term reliability
remains open; the Deezer-only exemption remains enabled.

Close capability notes: app-private temporary files are accessible through ADB
run-as on this installed debuggable build; the attempted external-file probe was
denied. No capability should be assumed on a non-debuggable build. Temporary
private file was removed. The first STOP probe refused an absent Cast session.
Ryan then reported phone Cast stuck locally; during the approved test window,
force-stopping native Deezer and YouTube cleared both sessions. Ryan reconnected
and confirmed Cast loading. The live receiver identified itself as Deezer - Beta,
explaining the missing corner: the old policy matched only Deezer. v102 permits
that exact additional name while still excluding native UI/player and other Cast
apps. Regression reproduced RED, then shared/Home/close-gate checks passed locally.

The source-built STOP probe then dispatched to the exact Deezer Beta Cast session.
Android reported zero sessions; receiver logs confirmed app stopped, audio focus
abandoned and all resources released. This is physical capability evidence, not
acceptance of the new BOOP button. Temporary probe removed, music controls returned
to Ryan. Close client tests cover unique hardware, wrong/ambiguous hardware, stale
receipts and selection change. Compilation/CI and scoped emulator launch checks
passed; physical v102 results are recorded above. Review corrected delayed Back cancellation
and false success from lost session observation; re-review found both addressed
and no further important defects. Final CI results are recorded above.
Ryan controls music outside explicitly agreed short tests; ask before interrupting.

## Deezer exemption: paused native session survives Cast interval

Ryan started casting for the transition test. Cast receiver reported playing while
native Deezer retained a paused session. DeezerSdkMediaService remained started in
background (no isForeground flag) with the Deezer-only exemption enabled. Repeated
read-only checks showed the same native paused session surviving beyond 150 seconds
since its pause, unlike the earlier idle-service termination. No playback commands
sent during this observation. This strengthens the exemption hypothesis but is not
full long-term acceptance. Ryan subsequently confirmed that the native return had
Now Playing data and was visible; see the current Close player entry above.
No app source or further OS settings changed. Exemption remains enabled for testing.

## Deezer-only exemption experiment: initial native Home survival

Ryan approved exempting only Deezer from battery optimization and one restart.
Added deezer.android.app to the user device-idle allowlist and verified membership.
One force-stop/relaunch restored an empty native media session. Normal play key did
not load a track; announced and started native Flow through the established
MediaController playFromUri helper, then returned to Home. No BOOP code changed.

Across approximately three minutes in background, Deezer retained its media session,
reported state PLAYING and updated metadata. Its DeezerSdkMediaService was present
and isForeground=true. Agent visually confirmed the Now Playing bar with current
track/artwork, controls and one puppet after the prior roughly 150-second idle
interval. Temporary helper and screenshots removed from device. Music left running.

This is recovery plus initial survival evidence, NOT proof the exemption caused the
survival: restart and native playback also restored a foreground service. The prior
failure followed a Cast/native transition; that transition still needs reproduction
with the exemption before declaring a durable fix. Exemption remains enabled for
this approved experiment. Remove only this added exemption if ineffective. v101
Home/reboot acceptance remains unchanged; missing-session reliability is still open.

## Missing-session cause isolated to Android stopping Deezer service

Further read-only logs identify the concrete failure boundary: ActivityManager
stopped DeezerSdkMediaService due to app idle; 19 milliseconds later system media
listeners reported zero controllers and the system Now Playing card was cancelled.
Deezer audio subsequently continued without that service. This explains why reopening
and native pause/resume did not restore metadata. It is not a BOOP rendering failure.

Shield API30; display on and deviceidle ACTIVE, so this is background-service idle
limiting rather than whole-device Doze. Deezer RUN_IN_BACKGROUND and
RUN_ANY_IN_BACKGROUND use default allow; current standby bucket active. Deezer has
no device-idle exemption. No OS settings changed. Do not claim these current values
prove its earlier state or why Deezer failed to recover its service.

Next proposed bounded diagnostic: with explicit approval, exempt only Deezer from
battery optimization, perform one announced Deezer restart to restore its destroyed
session, then reproduce native playback -> Home and Cast/native transitions and
observe beyond the previous idle interval. The exemption is a hypothesis to verify,
not a proven fix; remove it if ineffective. No global power changes or automatic
force-stop added to BOOP. Preserve the existing Now Playing design and checkpoints.
Android background-service limits reference:
https://developer.android.com/about/versions/oreo/background

## Missing-session recovery test: native pause/resume did not recover

Ryan approved a brief pause/resume diagnostic. Android media keys did not restore
sessions. Opened native current-track UI via sidebar, verified focused pause button
visually, and used native remote controls. Auto-hiding controls required another
navigation step to complete resume. Audio diagnostics confirmed player paused then
started again; Android media_session still had zero sessions after resume.
Returned to unified Home, music running, controls released, temporary device files
removed. No force-stop or app restart. Missing Now Playing remains unresolved;
pause/resume is not a recovery. Do not promote this into a fixed checkpoint.

## Open regression: audible native Deezer without Android media session

Ryan reports Home from Deezer has no Now Playing box and confirms music remains
audible. Read-only inspection on v101: unified Home foreground; Android media_session
reports zero sessions globally; unified notification access retained; Deezer process
and audio player running. This reproduces the earlier v99 missing-session condition.
Reopening native Deezer without pause/restart did not republish a session. Returned
to Home; no playback command sent. Temporary screenshot removed. Logs place the
Cast session ending before native playback resumed, but causation is not established.

Missing media data explains the absent card; this is not proof of a Home renderer
regression. Root cause inside provider/session lifecycle remains unresolved. Do not
fake current metadata or add automatic force-close/restart. Next bounded diagnostic
is a user-coordinated native pause/resume recovery check, observing whether a media
session reappears, before considering any implementation change. Room/discovery
follow-up paused for this issue. v101 Home/reboot acceptance remains scoped and valid.

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
