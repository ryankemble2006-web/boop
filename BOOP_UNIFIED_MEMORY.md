# Music Lab memory: installed on Shield, keep separate

Updated 2026-09-13. Ryan wants this fork separate while other operations continue, with integration only when he explicitly requests it. Never replace a newer Unified with the v161-derived lab base.

## Latest verified delivery

After requesting "sweet install it on the shield" and receiving a tool-blocked result, Ryan explicitly asked to try again. LIVE task HEAD was checked at `4968d324204f74bf26420effa8bb9991391d5b69` and its current handoff read. The same staged, GitHub-built APK was rechecked by SHA256 and package badging, then installed on the identified Shield through the existing Desktop Commander connection using ordinary ADB install -r. Android returned Success, exit 0. No tool upgrade, alternate connector, permission-grant flag or settings workaround.

A separate read-only verification completed with exit 0: Music Lab package `com.boop.musiclab`, version `1 / 0.1.1-v161-audio-prompt`, installed APK SHA256 exactly matched the signed candidate. Leanback launcher resolves to `com.boop.musiclab/.UnifiedEntryActivity`. At readback it was installed=true, stopped=true, notLaunched=true, RECORD_AUDIO granted=false.

Unified remained `162 / 1.2.162-native-lyrics`. Its APK SHA256 before and after was identical: `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`. HOME remained `com.boop.alpha1/.UnifiedEntryActivity`. No app launch, playback input, data clear, credentials/settings copy, permission grant, phone command or emulator action occurred.

**Current state is INSTALLED side by side, not merely staged or blocked.** The earlier block and reserved-PowerShell-variable correction remain recorded in the complete prior documents at `4968d324204f74bf26420effa8bb9991391d5b69`. Do not erase that history or treat it as the current failed delivery. Successful retry does not explain the earlier tool-layer failure. Installation identity is not runtime/visual/user acceptance.

## Identity, lineage and isolation

Owner: `boop-music-lab-side-by-side-v161`. Parent: `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`, from accepted v161 owner handoff `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`. No parent/Unified/main/other-lab branch was modified or merged.

BOOP Music Lab has independent application ID/namespace `com.boop.musiclab`, app data and grants. Its versionCode 1 is independent of Unified's version. Internal app references and auth callback were repackaged; library namespaces remain valid. No shared UID, settings/credentials copy or permission migration. The materializer preserves its parent tree and approved images/shaders/assets, excludes HOME/ASSIST/boot filters, boot permission and Home override registration, and retains com.boop Startup Manager protection. Permanent signing remains on GitHub; no replacement key or relay credentials were injected.

Installed source/build commit: `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`.
Signed run/job: `34774532761` / `103770090841`; artifact `10322813560`.
APK SHA256: `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`.
ZIP SHA256: `27e9e146108ebf06370150727e20b0fc1198217b9e08aaa771cfcd76bc61fa82`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
The prior attempt verified the staged signature directly; the retry rechecked the staged and installed digest against those same signed bytes. No new app build/version/signing operation occurred.

## Agreed music behavior, not implemented yet

Actual music loudness should determine vertical bounce height with quick rise and softer return, using Android Visualizer. Saved animation speed independently controls blinks. No BPM/rhythm inference, full-clip restarts or physical-microphone fallback. Approved artwork, single-face ownership, authored animations and exact original 1x timing remain locked.

The actual Visualizer sampler and music-reactive bounce are still unimplemented. This installed candidate is the full v161-derived fork plus the conditional permission entry. Do not misreport packaging or installation as working audio-reactive motion. Useful Deezer readings remain to be established jointly.

## Permission test and next boundary

In Music Lab choose Shield profile, then Launcher Settings > Now Playing > Music audio access. The private activity checks the live grant, skips an already-granted request, explains the Android microphone-labelled permission when missing, and calls the OS request only after Continue. Not now/Back cancels; denial does not loop; Open settings is optional. Request/dialog state survives recreation. This entry does not start capture, Visualizer, voice, services or networking, and avoids the existing voice callback. Ryan operates consent; do not grant with ADB. Its actual Android dialog and remote navigation are not yet tested by this session.

Historical test-first and passing build evidence remains in `docs/handoffs/2026-09-13-music-lab-fork.md`: five fork checks, four permission tests with 18 Java decisions, six timing functions, materialized consistency, 11 focused canonical/manifest checks, signed assembly and APK checks. No independent reviewer or visual acceptance is claimed. First-launch/permission acceptance and eventual audio-signal testing are separate from installation verification.

## Workflow and accepted original context

Source, tests, builds, signing and durable handoffs stay on GitHub. Existing Desktop Commander 0.2.47 remains unchanged. Preserve dirty/concurrent laptop work; no local source edits/builds or checkout synchronization are claimed. This retry only publishes documentation. Future device tests are joint with Ryan; physical Pixel 10 and emulators remain excluded from autonomous operation. Integration needs an explicit request and then-LIVE Unified successor.

Inherited v161 acceptance covers Ryan's confirmed speed controls on Shield and Pixel 7 and automatic eye-colour sharing in both directions. Speed is local; colour shared. That acceptance belongs to the original app, not this lab; do not reopen those repairs without a reported regression. Unified v162 readback here is identity/preservation evidence only.
