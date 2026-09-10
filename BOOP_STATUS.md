# Scoped canonical rebuild - v94 handoff

Updated 2026-09-10. Owning branch: `boop-canonical-rebuild`. Local source: `C:/Users/ryank/Documents/Codex/BOOP/.worktrees/boop-canonical-rebuild`. Base: `boop-unified@99474d141e7affad17cdbe854e94dd3986076980`.

## Current candidate

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

Reproduced locally against exact v94 MediaRequest source: bare `play Britney Spears` -> NOT_HANDLED_LOCALLY; `play Britney Spears on Deezer` -> DEEZER_SEARCH with query `Britney Spears`; `pause music` -> PAUSE. The local parser currently requires the explicit `on Deezer` suffix. Bare artist search continues into the HA path; HA NO_VALID_TARGETS maps to the reported `I can't find that` wording. This establishes the routing gap, not the actual remote response or provider capability.

Follow-up: the explicit Deezer phrase returned BOOP's `Deezer cannot play searches on this device. Open Deezer and choose the music there` message. Ryan clarified BOOP runs on the Pixel 7 and commands the Shield. This changes the diagnosis: BoopLocalMedia invokes Activity.startActivity on the Pixel, while the intended playback target is the remote Shield. The caught RuntimeException does not establish whether Deezer supports artist search on the Shield, or even distinguish a missing Pixel app from an unsupported intent. The previously implemented same-device Deezer search does not satisfy this remote use case.

Required behavior: artist requests from the Pixel must target the intended Shield through the available authenticated HA route. Do not silently switch to a nearby Google Home speaker, invoke Google Assistant, or change physically working HA transport/voice paths. Bare artist parsing alone is not a fix. Item 7 remains incomplete for remote artist playback.

Live HA browser inspection: the Shield is the TV device in Living Room, owned by Android TV Remote, with media entity media_player.tv_2. Its current app is deezer.android.app. Browse media shows Applications with No items. Android Debug Bridge was not present in the configured integration list. This verifies the existing remote target but does not establish artist-search support. Do not fabricate a deep link or blindly forward a search intent to the Shield.

Music Assistant 2.10.2 is already running. Its HA integration is discovered but not configured. Music sources currently contain only Ambient Sounds and the built-in Music Assistant provider; Deezer is available to add but is not connected. The Deezer setup form requires an ARL token; no token was read or entered and setup was cancelled without saving. Asked Ryan whether Music Assistant streaming to Shield is acceptable or playback must remain inside the Shield Deezer app. No HA configuration or playback was changed. ADB still lists emulators only.

References: https://www.home-assistant.io/integrations/androidtv_remote/ and https://www.home-assistant.io/integrations/androidtv/ . No application code or permissions changed during this diagnosis. Preserve working HA/basic transport and Natural Voice paths; remote artist search and unexpected speaker activation remain unresolved.

## Physical baseline and next action

Ryan physically confirmed v91 natural voices installed, selectable, demos speaking and a normal selected-voice BOOP reply. Accepted source: `11650313221ae5bf997dbb93b6a905bfdc7da1ed`; protected branch `checkpoint-boop-unified-v91-natural-voices-accepted`. APK SHA256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`. Preserve the exact v91 Desktop/server TEST APK and v88 rollback `f5f086fc4f67712b5746be067aff852331299bb0`. Canonical app source remains v91; canonical acceptance documentation was synchronized separately at `771b68a00ac95b40ed6e17cffac60af77528a934`.

Next: continue the artist-search and device-target diagnosis above. HA/basic transport and Natural Voice have positive scoped physical results; new-room reassignment/new exposed-device discovery, provider artist search, remote microphone and broader appearance/Back acceptance remain pending. Emulator success is not physical acceptance. Eyes transplant remains separately owned. Do not merge or replace the physical baseline by implication.

## Continuity caution

This checkout has a limited `remote.origin.fetch` list that omits unified/rebuild branches. A bare `git fetch origin boop-unified` may update FETCH_HEAD while leaving `origin/boop-unified` stale. Fetch explicit source:destination refs and compare live `git ls-remote` before using cached tracking refs. At this handoff, live main was `8d29d2bada1b8710ec030debce52c1976c990e56`; its personality continuity/canary clarification was reread. No new durable personality fact required an edit.
