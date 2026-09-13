# Music audio permission prompt: source and verification receipt

Updated 2026-09-13. Scope: Ryan explicitly asked to code a permission prompt if audio access is missing. Earlier read-only discussion remains the boundary for the actual VU bounce. This work does not implement a Visualizer sampler or change motion.

## Source and behaviour

Base: LIVE `boop-unified-eye-sync-safe-v159@593ad609ff87f651d5273bd17f5a2c0ca3ef5198`, accepted v161. Task branch: `boop-unified-music-audio-prompt`, not merged into the accepted owner. Production implementation: `c1c53e84f20e2211b729dc2f5a541018fc2908b8`.

Added a current Shield Home Now Playing settings row, `MusicAudioPermissionActivity`, a pure `MusicAudioPermissionFlow` and one private activity declaration. RECORD_AUDIO and MODIFY_AUDIO_SETTINGS were already declared; neither manifest permission was added by this change. No voice activity callback is reused.

Already granted: no system permission request. Missing: cancellable explanation followed by Continue and the genuine Android request. Not now/Back leaves things unchanged. Denial cannot automatically re-request. Open settings is a separate user choice, and returning rechecks then closes. In-flight request and dialog phase are saved across activity recreation. No recorder, visualizer, speech, service or network code is started by this entry.

## Test-first evidence

RED commit `41fa671852f1ab3836ab44adf4a9a1d3a06984e9`, run `34773557541`, job `103767419809`: four tests failed because the permission flow/activity, private registration and settings entry were missing. Logs were read before production code was written.

After implementation, run `34773806856`, permission job `103768098656`: four tests passed, including 18 executed pure-Java permission decisions. These cover already granted, first explanation/request, duplicate calls during a pending request, denial, explicit retry, restored pending state and grant/revocation rechecks. Other tests cover source integration and declarations, not Android UI execution.

The same run's compile job `103768116703` materialized all four changed production files byte-identically, then stopped before compilation because the new workflow used shallow Git history and the existing animation regression test could not read baseline `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. That file was independently fetched successfully from GitHub. No motion defect was demonstrated. Workflow-only correction `746a7d3ab0c8601c174ca690c2b27397266e673e` sets fetch-depth 0, matching the established full-build workflow; it changes no app source or existing tests.

Final non-visual check run: `34773889717` at `746a7d3ab0c8601c174ca690c2b27397266e673e`. At this checkpoint, its permission job `103768324770` passed; compile job `103768342060` is still in progress. Compilation and final timing outcomes are not yet claimed in this receipt.

## Review and publication boundaries

Reviewed the source diff and Activity lifecycle/permission callback separation. The app changes are limited to two new permission classes, six settings lines and one private activity declaration. Animation/voice/media/artwork files are unchanged. No independent reviewer or physical acceptance is claimed.

The compile job uses the existing materialization/build configuration and `:app:compileDebugSources`. It is not an APK assembly/signing job. No new signed artifact/version or installed checkpoint is produced here. No device operation, emulator launch, permission grant, settings reset or laptop source modification occurred.

The accepted v161 application and its speed/two-way-colour physical acceptance remain unchanged by this task. Next integration must reconcile the LIVE owner branch and preserve its current acceptance handoff. Only joint tests with Ryan can establish the actual Android prompt behaviour or later usable Deezer/Shield Visualizer readings. Blinks remain speed-controlled; music-driven bounce is still a future implementation.
