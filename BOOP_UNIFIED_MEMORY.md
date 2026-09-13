# Music Lab fork memory: preserve separation until Ryan requests integration

Updated 2026-09-13. Ryan's separation instruction remains: do not merge while other operations continue; fork this entirely to install side by side and combine the work later. This overrides inherited instructions suggesting immediate Unified integration.

## Latest installation request and actual outcome

Ryan explicitly asked: "sweet install it on the shield". This authorized installing the already-built Music Lab on Shield only, not either phone, emulator activity, permission grants or autonomous runtime tests. The LIVE lab branch was read at `ba8893ad37c8c43d71c6da94bf64b8b1de570560`; current main BOOP_START_HERE.md confirmed GitHub development and joint device testing. Historical local context was read only; no checkout was changed or claimed synchronized.

The Shield was connected and identified. Existing Unified was already `162 / 1.2.162-native-lyrics`, not the v161 parent. Music Lab was absent. The exact signed GitHub artifact from run `34774532761` was staged in a new task-specific laptop Downloads directory. Staged APK hash and actual apksigner verification matched the candidate and permanent certificate below.

The combined verification/install tool request was blocked before execution because the tool could not determine its safety status. It was NOT an Android package-manager failure and did not prove the app unsafe. Do not invent a successful install or attribute the block to dangerous animation code. No alternate route was used to retry the denied install.

Read-only follow-up confirmed `com.boop.musiclab` still ABSENT; Unified v162 APK hash and resolved HOME unchanged. One read-only PowerShell helper failed after the package/hash check due to the reserved `$HOME` variable; the corrected read-only helper passed the remaining HOME/hash/signature checks. This was not an install retry. No app launch, playback key, grants, data clear, HOME change or device settings write occurred. No phone or emulator was operated.

**Current delivery state: signed and staged, NOT installed.** The request remains unfulfilled at the tool-layer installation boundary. Hand-off/status/memory updates record facts only and do not change application code or the signed candidate. Keep the actual tool block visible, respect it, and separate source/build evidence from installation and user acceptance.

## Identity and lineage

Task owner `boop-music-lab-side-by-side-v161`. Parent `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`, based on accepted Unified v161 at owner handoff `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`. No merge or parent-branch write occurred. Do not restart from isolated old app branches or overwrite a newer Unified with this base.

Separate app **BOOP Music Lab**, application ID/namespace `com.boop.musiclab`, independent versionCode1/versionName `0.1.1-v161-audio-prompt`. It is an installable full v161-derived fork plus the conditional music permission feature, not a new Unified version. Its app data and grants are separate; no settings, credentials or permission grants were copied from Unified. Original library class namespaces are retained where they do not represent app identity. App internal intents and auth callback scheme were adjusted for the fork.

Materializer copies to a fresh independent build tree and asserts the parent tree fingerprint is unchanged. App HOME, ASSIST and boot intent filters, boot permission and Home override service registration are excluded. All `com.boop.*` startup protection remains intact. Images, shaders, authored motion and copied assets remain unchanged. Existing permanent BOOP signer is reused entirely on GitHub. This is not permission to replace keys, select a default Home/assistant or edit device settings.

## Agreed music behavior

Ryan wants a VU-style bounce using Android Visualizer loudness, not clever beat detection: actual level determines vertical bounce height with quick rise and a softer return. Saved animation speed must continue to control blinks independently of audio. No BPM lookup, rhythm inference, whole-clip restarts or physical-microphone fallback. Artwork is locked.

The actual sampler/bounce is still unimplemented. The earlier feasibility discussion was read-only, followed by a narrow request to code the missing-permission prompt. The completed packaging work makes that already-implemented prompt and the v161 app independently installable. Do not misreport this packaging or installation attempt as working music-reactive animation.

## Existing conditional permission entry

Launcher Settings > Now Playing > Music audio access launches private `MusicAudioPermissionActivity`. It checks the live grant, skips an already-granted request, otherwise explains Android's microphone-labelled permission and requests only after Continue. Not now/Back cancels; denial does not loop; Android app settings are an explicit separate choice. Pending request/dialog state survives recreation. The prompt creates no audio capture, Visualizer, voice activity, service or network connection. Existing RECORD_AUDIO and MODIFY_AUDIO_SETTINGS declarations were already present. Ryan operates the actual OS prompt; do not silently grant it using ADB.

## Verified candidate and limits

Source `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`; signed build `34774532761`/job `103770090841`; artifact `10322813560`. APK SHA256 `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`; ZIP SHA256 `27e9e146108ebf06370150727e20b0fc1198217b9e08aaa771cfcd76bc61fa82`; permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

GitHub test-first fork failures were observed before implementation. Final permission, timing, canonical ownership, five fork isolation checks, signed build and packaged identity/signature/integrity checks passed. Downloaded artifact identity and hashes were independently rechecked without executing the app. Full build evidence is in `docs/handoffs/2026-09-13-music-lab-fork.md`; no independent reviewer or runtime visual acceptance is claimed. That build receipt predates the read-only Shield package checks recorded above.

Keep app source/tests/builds/signing on GitHub, and preserve dirty/concurrent laptop work. Desktop Commander 0.2.47 stays unchanged while functioning. Future tests are joint with Ryan, targeting the fork only. Physical Pixel10 remains excluded. A later merge needs the live then-current Unified successor and explicit authorization; preserve other ongoing operations. The complete pre-installation root handoff/status/memory remain retrievable at `ba8893ad37c8c43d71c6da94bf64b8b1de570560`.

## Accepted original app context

The parent records Ryan's physical acceptance of animation-speed controls on both Shield and Pixel7 and automatic colour sharing in both directions on installed Unified v161. Speed is local; colour is shared. That is inherited accepted evidence, not a test of this newly packaged lab. Do not reopen those repairs absent a new observation. The later live version-162 readback above is installation identity, not new visual acceptance. Historical acceptance and permission-task receipts remain in Git history and docs/handoffs.
