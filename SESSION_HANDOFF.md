# BOOP Music Lab: Shield installation requested, tool blocked, not installed

Updated 2026-09-13. Owner: `boop-music-lab-side-by-side-v161`. Ryan explicitly requested an entirely separate installable fork because other operations are continuing, with integration deferred until he asks. Do not merge this branch into Unified, main, Lyrics Lab or another operation.

## Latest installation attempt

Ryan then explicitly requested: "sweet install it on the shield". Installation of `com.boop.musiclab` on Shield ONLY was authorized. No phone, emulator, permission grant or app launch was requested.

Read the laptop continuity files and historical tool-path receipt, then fetched the LIVE task branch at `ba8893ad37c8c43d71c6da94bf64b8b1de570560` and main's current BOOP_START_HERE.md. Main's newer GitHub-development/joint-testing rules supersede the historical local rule stack. No laptop checkout was changed or claimed synchronized.

The connected target was identified as SHIELD Android TV. Before installation, existing Unified reported `162 / 1.2.162-native-lyrics`; Music Lab was absent. Downloaded the exact `BOOP-Music-Lab` artifact from run `34774532761` to a new task-specific directory under the laptop Downloads folder, without building or editing app sources locally.

The tool rejected the combined verification/install command before execution: it could not determine the request's safety status. This was a tool-layer block, not an Android package-manager failure, and it did not establish that the application was unsafe. The denied install was not retried through another route.

Read-only follow-up confirmed Music Lab remained ABSENT, existing Unified APK SHA256 was identical before/after and its version remained 162. The resolved HOME activity was unchanged. Staged APK SHA256 matched `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`; Java/apksigner verification succeeded with the expected permanent signer below. An initial read-only helper stopped after the package/hash checks because PowerShell reserves `$HOME`; a corrected read-only helper completed the HOME/file-hash/signature checks successfully. This correction was not an installation retry.

**Current physical state: Music Lab NOT installed or launched. Unified v162 unchanged. No permission grants, playback inputs, resets, data migration, HOME changes, phone commands or emulator operations.** The signed artifact is staged and verified, not physically accepted. Preserve the actual tool block and do not silently report installation success or route around it.

## Exact candidate and lineage

Separate **BOOP Music Lab**, package/application namespace `com.boop.musiclab`, independent lab version `1 / 0.1.1-v161-audio-prompt`. It is based on accepted Unified v161 plus the conditional music audio permission prompt, not an update to installed `com.boop.alpha1`.

Parent: `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`. That branch starts from `boop-unified-eye-sync-safe-v159@593ad609ff87f651d5273bd17f5a2c0ca3ef5198`, whose app version was 161 despite its branch name. Neither parent was advanced or merged by this fork or installation task.

Build/source commit: `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`.
GitHub signed run: `34774532761`, job `103770090841`, SUCCESS, rechecked during this installation request.
Artifact: `BOOP-Music-Lab`, ID `10322813560`.
APK: `BOOP-Music-Lab-v1.apk`, 155257454 bytes.
APK SHA256: `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`.
Artifact ZIP SHA256: `27e9e146108ebf06370150727e20b0fc1198217b9e08aaa771cfcd76bc61fa82`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## What is present, and what is not

The existing v161 code and conditional permission entry are retained. Open Launcher Settings > Now Playing > Music audio access after choosing the Shield profile in this independently configured app. If RECORD_AUDIO is already granted, no Android request is made. Otherwise a cancellable explanation leads to Continue and the real OS permission request. Denial/Back/Not now does not repeatedly prompt; opening Android app settings is a separate choice. Granting music access is not routed through the voice callback. The permission activity itself creates no recorder, Visualizer, voice session, service or network request.

**Real music-driven bounce is not implemented yet.** The agreed next design is a simple Android Visualizer loudness-to-height mapping: quick rise, softer fall, no BPM/beat detection and no physical-microphone fallback. Saved animation speed must continue to control blinks independently. Useful Deezer readings, real OS prompt handling and physical side-by-side behavior remain untested with Ryan; passing CI does not establish them.

## Preserved fork implementation and build checks

`scripts/materialize-music-lab.py` copies the prepared Unified tree into a separate fresh `boop-music-build/BOOP-Music-Lab` tree. It changes app namespace/package, matching internal references, label, task affinity, auth callback scheme and version, while leaving the parent tree unchanged. Library namespaces remain valid within the separate application sandbox. No shared UID is used. No user data or credentials are copied.

The fork excludes HOME, ASSIST and BOOT_COMPLETED intent filters, RECEIVE_BOOT_COMPLETED permission and the Home override service registration so it is not another automatic Home/assistant/boot owner. Source Startup Manager protection for all `com.boop.*` packages remains intact. Approved images, shaders and other copied assets remain byte-identical. Existing permanent signer configuration is reused; no replacement key. Relay credentials were not injected into this lab build.

RED source `fafcc351c373c0591edcbf93d0ed9c0d20b7a9de`, run `34774429117`: four new fork checks failed because the separate generated app did not yet exist; parent preservation passed. The failure log was read before implementation.

GREEN run above: all five fork checks passed; four permission tests including 18 executed Java decisions passed; all six existing animation-speed functions passed, materialized speed/colour/master checks passed, and 11 focused canonical-owner/notification-manifest tests passed. Signed assembly completed with 104 tasks. Packaged ID, version, label, entry point, forbidden-role absence, permanent signature and ZIP integrity passed. Downloaded ZIP/APK hashes and embedded build commit were rechecked in the artifact sandbox; no app code was built or executed there. Existing nonfatal deprecation warnings remain. Detailed historical build receipt: `docs/handoffs/2026-09-13-music-lab-fork.md`. Its statements that no device checks occurred describe the preceding build task, not the later read-only checks above.

## Next and boundaries

Any later installation must target `com.boop.musiclab` only and preserve whatever Unified/other lab versions are live then. Do not grant permissions with ADB. Ryan operates the OS prompt and tests behavior jointly. Do not automatically contact physical Pixel 10, operate emulators, replace HOME or reopen accepted colour/speed repairs.

Keep source, tests, builds, permanent signing and handoffs on GitHub. Fetch LIVE task HEAD before continuation and preserve concurrent work. This installation attempt changes documentation only; source, version and signed APK remain unchanged. The pre-installation root handoff/status/memory remain available at `ba8893ad37c8c43d71c6da94bf64b8b1de570560`. The pre-fork permission handoff is preserved at parent `174d492f36db80bc3da4036d9434ce1ec3c1582a`. Integration into the then-current Unified successor is deferred until explicitly requested, never a copy-over of this v161 base.
