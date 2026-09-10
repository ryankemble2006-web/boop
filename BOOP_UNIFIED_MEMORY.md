## v96 native music candidate - implementation in progress

Updated 2026-09-10. Owning branch remains boop-canonical-rebuild, based on v95
source 61d7604e1f923c1789aba94afb0d76831bed073d and receipt 14b9f4a.
Ryan physically reported v95 worked on the first artist request, but repeat/switch
attempts only blipped and did not change music. v95 is not accepted for that path.

v96 adds exact song-title lookup (optionally "by artist") through public Deezer
metadata, native album-row selection, and "play music" -> native Deezer Flow.
No Music Assistant streaming or alternate-speaker fallback. Public catalogue
calls carry no HA credential. Flow and existing transport need no public lookup.
Deezer's top-ranked track distinguishes an exact song title from its artist name;
this handles the separate artist named Bohemian Rhapsody and multiple artists named
Queen. Otherwise an exact unique artist is used, then an exact playable song. The
spoken song request includes its artist. Live metadata resolved Bohemian Rhapsody
to Queen's original album, Britney Spears and Queen to their expected artists,
and play music to Flow. User end-to-end testing remains needed.

The selected exposed Android TV must have one ADB candidate in the same room.
Read-only hardware MAC matching verifies it is the same physical TV before control.
Ryan explicitly approved adding HA Android Debug Bridge; configured and placed in
Living Room. Existing Assist exposure was retained. Device addresses remain private.
Each request cleanly restarts Deezer to discard stale intents. Fresh nonce-tagged
ADB responses, unique temporary screen files, exact text/accessibility labels,
focused remote controls and a second check after pause gate one DPAD_CENTER.
No fixed song-row numbers or coordinate taps. Room changes/interruption cancel.
Screen hierarchies are transient local control data, never sent to conversation.
HA itself stores adb_response as documented; raw hierarchies stay out of Git.

Live investigation found Android's uiautomator dump reconnects accessibility.
The installed standalone launcher brought Home forward on every reconnect.
A narrow once-per-boot compatibility fix was reviewed and published on the existing
boop-shield-clean-launcher branch: ccb10658bded07dbf2a91ee8234509999f960a49.
Run 34446163444 succeeded; artifact 10139755652; signed v23 APK SHA256
6bcc46633c38f61f4c7c5b4b0a49a4d3eec7b05818a25ef29abd4d36e82f411e.
Ryan explicitly approved installation; installed successfully after saving the exact
old APK privately for rollback. Repeated screen reads now keep Deezer foreground.
The same small fix is included in unified source. No artwork/permission changes.
Reboot and single/double Home acceptance remain pending; no new physical checkpoint.

Local shared-state harness and 30 music tests pass. Exact controller through the
existing ADB shell selected Bohemian Rhapsody with native playing indication and
then Flow with native playing indication. This is component/device evidence, not
end-to-end Pixel v96 acceptance. Full signed v96 build and emulator check pending.
GitHub visual tests remain disabled. Eyes work is untouched.

# Scoped canonical rebuild - v95 signed candidate handoff

Updated 2026-09-10. Owning branch: `boop-canonical-rebuild`. Local source: `C:/Users/ryank/Documents/Codex/BOOP/.worktrees/boop-canonical-rebuild`. Base: `boop-unified@99474d141e7affad17cdbe854e94dd3986076980`.

## Signed v95 candidate

Application source/build commit: `61d7604e1f923c1789aba94afb0d76831bed073d` on `boop-canonical-rebuild`. Version 95 / `1.2.95-native-deezer-artist`, package `com.boop.alpha1`.

- GitHub run `34442138007`: SUCCESS; artifact `10138398012`.
- ZIP SHA256: `2d306febc06ca26d67cf5d3ecc27f8d83737f0fa89ae1e45de596d78b7989e5f`.
- APK SHA256: `539824affb7e3d750558fd5104941de57326576e24abe5863c23c0cff64b039b`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- 184 Unified and 58 Shield focused functional tests passed, zero failures/errors/skips. GitHub performed no visual tests.
- Downloaded ZIP digest, APK digest, source receipt, package/version/entry activity and permanent signature independently verified locally.
- Signed deliverable: `C:/Users/ryank/Documents/Codex/2026-09-10/the-x20/outputs/BOOP-v95-native-deezer-candidate.apk`.

First v95 run `34441802763` failed compiling the new test fixture because checked JSON exceptions were undeclared; application source compiled. Commit `61d7604` fixes the declarations, and the rerun above passes.

Exact signed v95 updated the Pixel_7_Pro_API_36 emulator successfully. Manual screenshot inspection confirmed the readable Voice screen, scrolling and device/room settings navigation. Profile was opened through its visible button; direct ADB launch correctly refused the non-exported activity. Back was exercised; no AndroidRuntime error appeared in the inspected log. No HA credentials or natural voice packs were installed for this check. BOOP was stopped afterward; v95 remains installed with the prior Wall profile. Other emulators and the eyes task were untouched.

Physical acceptance: Ryan confirmed the native Britney diagnostic route, and v94 HA/basic transport/Natural Voice worked. The v95 Pixel-to-Shield application route is still pending physical testing: cold Deezer launch, repeat the same artist, then switch artist. Fixed page/pause waits remain best-effort. No merge to canonical or replacement of protected v91 TEST artifacts.

## Previous built candidate (v94)

Application source/build commit: `e683b26e04a3bc2bb8ba5a94ee23eeb2380d1ec0`. Later documentation commits do not change this APK. Resolve the current branch documentation HEAD with live `git ls-remote`; do not confuse it with the built source.

- Version 94 / `1.2.94-canonical-room-selection`; package `com.boop.alpha1`.
- GitHub run `34437732145`: SUCCESS. Artifact `10136874068`.
- ZIP SHA256: `cf15bf9852dec1a9f223ee53c1611b80e310e0a89062f697bde7e7d23e91507b`.
- APK SHA256: `f4bc6f1032f3b2fb324ac415086a9163caf403cfe603465771108179b4ebea16`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Signed local deliverable: `C:/Users/ryank/Documents/Codex/2026-09-10/the-x20/outputs/BOOP-v94-canonical-candidate.apk`.
- 170 Unified and 58 Shield focused functional tests passed, zero failures/errors/skips. Shared-state/media JVM harness also passed locally. Source adapters materialized successfully. ZIP, APK, built-commit receipt and permanent signer independently verified after download.
- No GitHub visual/appearance tests ran. No merge into `boop-unified`; candidate awaits Ryan's device/provider acceptance.

## Implemented selected scope

Ryan selected items 1, 3, 6, 7, 8 and 10. Eyes are owned by another task for later transplant; no eye/blink mechanics or Turbo redesign is included.

- **Item 1:** Shared speech, room and media snapshots; synchronous release-before-acquire ownership, reentrant-safe publication and shared playback epoch.
- **Item 3:** Configurable current room across Wall/Shield; actual HA registry ID resolution before saving; discovery of enabled, visible, explicitly conversation-exposed lights/fans/switches. Named ambiguity asks for clarification. Group/unsupported requests preserve local HA Assist. Room reassignment invalidates stale identity and late results; failed selection preserves the prior room.
- **Item 6:** Existing clean Shield Home/Now Playing source integrated as an internal library. A single media corner appears outside Home during playback; Home acquires the puppet in its Now Playing card. Pause/stop removes it. This is same-device presentation handoff, not cross-device playback transfer.
- **Item 7:** Explicit Deezer search/play intent and active-session pause/resume/next/previous, with existing HA media fallback. Unsupported Deezer search reports failure; no Google detour or fabricated playback success.
- **Item 8:** HA Back returns to its caller without fullscreen puppet takeover. Short TV Back returns to Home; long Back opens Android settings. Fresh entry resets old Launcher Settings to Home. Android16 legacy Back compatibility explicitly retained.
- **Item 10:** First-run automatic/manual profile selection and later device/room settings from Launcher, Wall voice settings and Shield settings. Explicit overlay-access settings route; application does not grant access or change default HOME automatically.

Preserved: natural-voice subsystem, controller-owned microphone, local-first HA control, package/permanent signer, original temporary eye assets and standalone Shield Home provenance. See `unified/SOURCE_HEADS.md` and `docs/superpowers/plans/2026-09-10-scoped-canonical-rebuild.md`.

## Manual emulator evidence

Exact signed v94 installed on local API36 TV and Pixel AVDs. Inspection used ADB input and screenshots viewed by the agent, not scripted visual assertions.

TV: readable Home/settings and profile screen; simulated session Play and Next reached its MediaSession and track 2 appeared; one puppet in Home Now Playing, one corner over its player, and one Home puppet after re-entry. Short Back from settings returned Home, fresh entry reset settings, HA unpaired Back returned Launcher Settings, long Back opened Android settings. Overlay button opened Android's display-over-apps page. Stop/release removed the corner. No AndroidRuntime error appeared in the inspected logs.

Phone: automatic routing selected Launcher for the AVD's generic reported model; its empty workspace is intentionally black. Menu opened device settings. Manual Wall selection reached MainActivity's existing idle-black surface. Voice-settings intent opened the scrollable Voice screen; its device/room link returned to Profile and Back returned to Wall. Room Save while unconnected showed the connection-first message. No natural-pack download or acoustic acceptance was claimed. An emulator System UI ANR appeared during boot, was dismissed with Wait, and subsequent controls responded; no BOOP crash was observed.

Local media fixture `com.boop.testmedia` supplied metadata only (no audio/provider), then was uninstalled. Temporary TV notification-listener grant was removed; preexisting TV recommendations listener preserved. Overlay app-op restored to `default`. BOOP stopped on both emulators; signed v94 remains installed. The other task's Animation Lab app/files were untouched; the shared TV AVD was left running. Phone app profile remains Wall from the manual test.

Review findings (room identity, ambiguity, lifecycle ownership, navigation and stale room callback) were resolved; final read-only review confirmed no outstanding findings. Manual screenshots and fixture sources remain local scratch under the dated task's `work/`, not in the repository.

## Latest physical report and music investigation (2026-09-10)

After receiving v94, Ryan reports HA commands working, basic music commands working, and Natural Voice on conversational and HA replies. These are positive scoped physical results; they are not blanket acceptance of artist search, playback targeting or the whole candidate.

Remaining reported failure: saying `play Britney Spears` produces `I can't find that`. During a separate/unclear interaction BOOP asked which device; Ryan observed a nearby Google Home speaker activate, then turn off. Exact initiating command, answer and tested device have been requested. Do not infer that BOOP launched Google Assistant or selected that speaker without evidence.

Reproduced locally against exact v94 MediaRequest source: bare `play Britney Spears` -> NOT_HANDLED_LOCALLY; `play Britney Spears on Deezer` -> DEEZER_SEARCH with query `Britney Spears`; `pause music` -> PAUSE. The v94 local parser required the explicit `on Deezer` suffix. Bare artist search continues into the HA path; HA NO_VALID_TARGETS maps to the reported `I can't find that` wording. This establishes the routing gap, not the actual remote response or provider capability.

Follow-up: the explicit Deezer phrase returned BOOP's `Deezer cannot play searches on this device. Open Deezer and choose the music there` message. Ryan clarified BOOP runs on the Pixel 7 and commands the Shield. This changes the diagnosis: BoopLocalMedia invokes Activity.startActivity on the Pixel, while the intended playback target is the remote Shield. The caught RuntimeException does not establish whether Deezer supports artist search on the Shield, or even distinguish a missing Pixel app from an unsupported intent. The previously implemented same-device Deezer search does not satisfy this remote use case.

Required behavior: artist requests from the Pixel must target the intended Shield through the available authenticated HA route. Do not silently switch to a nearby Google Home speaker, invoke Google Assistant, or change physically working HA transport/voice paths. Bare artist parsing alone is not a fix. Item 7 remains incomplete for remote artist playback.

Live HA browser inspection: the Shield is the TV device in Living Room, owned by Android TV Remote, with media entity media_player.tv_2. Its current app is deezer.android.app. Browse media shows Applications with No items. Android Debug Bridge was not present in the configured integration list. This verifies the existing remote target but does not establish artist-search support. Do not fabricate a deep link or blindly forward a search intent to the Shield.

Music Assistant 2.10.2 is already running. Its HA integration is discovered but not configured. Music sources currently contain only Ambient Sounds and the built-in Music Assistant provider; Deezer is available to add but is not connected. The Deezer setup form requires an ARL token; no token was read or entered and setup was cancelled without saving. Ryan requires playback inside the Shield Deezer app; Music Assistant streaming is rejected for this feature. Initial browser inspection changed no configuration or playback. Subsequent native playback tests are recorded below.

References: https://www.home-assistant.io/integrations/androidtv_remote/ and https://www.home-assistant.io/integrations/androidtv/ . No application code or permissions changed during this diagnosis. Preserve working HA/basic transport and Natural Voice paths; remote artist search and unexpected speaker activation remain unresolved.

## Native Deezer investigation and v95 implementation

Ryan explicitly requires native Deezer playback on the Shield. A pre-existing authorized Shield ADB connection was available; no debugging setting or device permission was enabled. The installed Deezer TV package is deezer.android.app, version 1.0.1.1. Its manifest declares native HTTPS artist links and SEARCH, but no PLAY_FROM_SEARCH activity handler. SEARCH produced no visible navigation. The public Deezer artist lookup resolved Britney Spears to artist/483; opening that artist link showed the correct native artist page with focused Play top tracks. Selecting it started music and Ryan confirmed audibly: "yes britney came on". This is physical acceptance of the diagnostic route, not of a new BOOP APK.

Repeated via existing Home Assistant Android TV Remote: remote.turn_on with the official artist URL opened the correct page; media_player.media_pause changed its focused button to Play; remote.send_command DPAD_CENTER then showed playback again. The native button toggles, so selecting while already playing pauses it. The room/device-pair discovery template was verified live, and the TV media entity is already exposed to Assist. No HA integration, account, exposure, permission or automation was changed. Music was left playing. Always announce subsequent audible tests first; the initial live test surprised Ryan.

Deezer's declared media-browser service and media-button receiver are exported=false. A temporary permission-free, non-playing diagnostic app confirmed connection failure, then was uninstalled. No private interface bypass is proposed. Physical screenshots, diagnostic APK/source, copied third-party APK and network details remain local scratch only; never publish them. Android media_session diagnostics showed no active session despite audible native playback, so that API is not proof of stopped music on this installed app.

v95 implementation in this branch: bare play requests resolve an exact unique Deezer artist first; unmatched/ambiguous bare requests and unavailable catalogue preserve normal HA/conversation routing. Explicit on-Deezer failures remain local. The selected room must have one enabled, visible, Assist-exposed Android TV media player paired by HA device identity to its Android TV Remote. Open the official artist link, allow the native page to load, verify unchanged room and Deezer current app, pause, allow pause to settle, recheck, then send one selection command. No alternate speaker fallback or automatic selection retry. HA credentials never go to the public Deezer catalogue. The local reply says Requested rather than claiming confirmed playback; natural speech and existing transport paths are preserved.

Known limitation: Android TV Remote reports the current app but not artist-page focus or completed pause. The 3-second page and 500-ms pause waits are best-effort, not visual/state confirmation. Cold app launch, repeated same artist, switching artist and Pixel end-to-end behavior must pass physical tests before accepting this candidate. A slow/error page or manual navigation during the sequence can still defeat UI-based playback; do not promote request dispatch to physical success. Item 7 stays open until those tests are accepted.

Local shared-state/media behavioral harness and materialization passed. Added offline HTTP/JSON tests for credential separation, paired targets, ambiguity, exposure, app changes, pause failures, room changes and conversation preservation; GitHub CI/build/signing and manual emulator checks are now complete as recorded above; physical candidate tests remain pending. Review found bare-play conversation capture; fixed by validating bare artist requests before consuming them. The signed v95 artifact is recorded above; no new physically accepted rollback checkpoint exists.
## Physical baseline and next action

Ryan physically confirmed v91 natural voices installed, selectable, demos speaking and a normal selected-voice BOOP reply. Accepted source: `11650313221ae5bf997dbb93b6a905bfdc7da1ed`; protected branch `checkpoint-boop-unified-v91-natural-voices-accepted`. APK SHA256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`. Preserve the exact v91 Desktop/server TEST APK and v88 rollback `f5f086fc4f67712b5746be067aff852331299bb0`. Canonical app source remains v91; canonical acceptance documentation was synchronized separately at `771b68a00ac95b40ed6e17cffac60af77528a934`.

Next: install signed v95 on the Pixel, select Living Room, and test native artist playback on the Shield, including cold launch, repeat and switching artist. HA/basic transport and Natural Voice have positive scoped physical results; new-room reassignment/new exposed-device discovery, provider artist search, remote microphone and broader appearance/Back acceptance remain pending. Emulator success is not physical acceptance. Eyes transplant remains separately owned. Do not merge or replace the physical baseline by implication.

## Continuity caution

This checkout has a limited `remote.origin.fetch` list that omits unified/rebuild branches. A bare `git fetch origin boop-unified` may update FETCH_HEAD while leaving `origin/boop-unified` stale. Fetch explicit source:destination refs and compare live `git ls-remote` before using cached tracking refs. At this handoff, live main was `8d29d2bada1b8710ec030debce52c1976c990e56`; its personality continuity/canary clarification was reread. No new durable personality fact required an edit.
