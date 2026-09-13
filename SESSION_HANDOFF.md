# BOOP Music Lab: signed side-by-side fork, no merge

Updated 2026-09-13. This task belongs to `boop-music-lab-side-by-side-v161`. Ryan explicitly requested an entirely separate installable fork because other operations are continuing, with integration deferred until he asks. Do not merge this branch into Unified, main, Lyrics Lab or another operation.

## Current result and exact identity

Built and downloaded the separate **BOOP Music Lab** APK. Package/application namespace `com.boop.musiclab`, independent lab version `1 / 0.1.1-v161-audio-prompt`. It is based on accepted Unified v161 plus the conditional music audio permission prompt, not an update to the installed `com.boop.alpha1` app. No installation, device input, permission grant, settings reset, data migration or emulator action occurred in this task.

Parent: `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`. That branch starts from `boop-unified-eye-sync-safe-v159@593ad609ff87f651d5273bd17f5a2c0ca3ef5198`, whose app version was 161 despite its branch name. Neither parent was advanced or merged by this fork task.

Build/source commit: `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`.
GitHub signed run: `34774532761`, job `103770090841`, SUCCESS.
Artifact: `BOOP-Music-Lab`, ID `10322813560`.
APK: `BOOP-Music-Lab-v1.apk`, 155257454 bytes.
APK SHA256: `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`.
Artifact ZIP SHA256: `27e9e146108ebf06370150727e20b0fc1198217b9e08aaa771cfcd76bc61fa82`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## What is present, and what is not

The existing v161 code and conditional permission entry are retained. Open Launcher Settings > Now Playing > Music audio access after choosing the Shield profile in this independently configured app. If RECORD_AUDIO is already granted, no Android request is made. Otherwise a cancellable explanation leads to Continue and the real OS permission request. Denial/Back/Not now does not repeatedly prompt; opening Android app settings is a separate choice. Granting music access is not routed through the voice callback. The permission activity itself creates no recorder, Visualizer, voice session, service or network request.

**Real music-driven bounce is not implemented yet.** The agreed next design is a simple Android Visualizer loudness-to-height mapping: quick rise, softer fall, no BPM/beat detection and no physical-microphone fallback. Saved animation speed must continue to control blinks independently. Useful Deezer readings, real OS prompt handling and physical side-by-side behavior remain untested with Ryan; passing CI does not establish them.

## Fork implementation and checks

`scripts/materialize-music-lab.py` copies the prepared Unified tree into a separate fresh `boop-music-build/BOOP-Music-Lab` tree. It changes app namespace/package, matching internal references, label, task affinity, auth callback scheme and version, while leaving the parent tree unchanged. Library namespaces remain valid within the separate application sandbox. No shared UID is used. No user data or credentials are copied.

The fork excludes HOME, ASSIST and BOOT_COMPLETED intent filters, RECEIVE_BOOT_COMPLETED permission and the Home override service registration so it is not another automatic Home/assistant/boot owner. Source Startup Manager protection for all `com.boop.*` packages remains intact. Approved images, shaders and other copied assets remain byte-identical. Existing permanent signer configuration is reused; no replacement key. Relay credentials were not injected into this lab build.

RED source `fafcc351c373c0591edcbf93d0ed9c0d20b7a9de`, run `34774429117`: four new fork checks failed because the separate generated app did not yet exist; parent preservation passed. The failure log was read before implementation.

GREEN run above: all five fork checks passed; four permission tests including 18 executed Java decisions passed; all six existing animation-speed functions passed, materialized speed/colour/master checks passed, and 11 focused canonical-owner/notification-manifest tests passed. Signed assembly completed with 104 tasks. Packaged ID, version, label, entry point, forbidden-role absence, permanent signature and ZIP integrity passed. Downloaded ZIP/APK hashes and embedded build commit were rechecked in the artifact sandbox; no app code was built or executed there. Existing nonfatal deprecation warnings remain. Detailed receipt: `docs/handoffs/2026-09-13-music-lab-fork.md`.

## Next and boundaries

The installable file has been prepared for side-by-side use; it has NOT been installed or launched. Any requested installation must target `com.boop.musiclab` only and preserve whatever Unified/other lab versions are live then. Do not grant its permissions with ADB. Test behavior together with Ryan. Do not automatically contact physical Pixel 10, operate emulators, replace HOME, or reopen accepted colour/speed repairs.

Keep source, tests, builds, permanent signing and handoffs on GitHub. No laptop source checkout was edited or synchronized. Fetch LIVE task HEAD before continuation, preserve concurrent work, and publish scoped handoff/status/memory changes with live branch verification. The pre-fork permission handoff is preserved at parent `174d492f36db80bc3da4036d9434ce1ec3c1582a`. Integration into the then-current Unified successor is deferred until explicitly requested, never a copy-over of this v161 base.
