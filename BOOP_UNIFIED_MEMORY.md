# Unified memory: v163 music audio access integration

2026-09-13. Ryan requested rolling Music Lab's implemented permission entry into current Unified v162 and bumping to163 after Now Playing failed in the separate app. Earlier no-merge instructions are superseded for this requested integration only. Other operations remain protected.

## Source and isolation

Current owner remains boop-unified-eye-sync-safe-v159. Live accepted base112d09b5b446d6582954a6d89b3700fe16298ecb; integration branch boop-unified-music-v163; app source cac499bf9e6ab325faa2d843112f90ace71118b1. The source copies only MusicAudioPermissionActivity, MusicAudioPermissionFlow and the six-line settings entry from lab7b596a3. The v162 manifest receives one private activity; version becomes163 /1.2.163-music-audio-access. Package com.boop.alpha1 and stable signer are unchanged. Do not import the lab materializer/package identity, Home/assistant/boot exclusions, older media paths or its data.

Read-only Shield access check: Unified's current ShieldNowPlayingListenerService access true, Music Lab's false; installed Unified version162. Missing media access is concrete evidence, not proof of every possible cause of the reported lab failure. No notifications or track contents were inspected and no grant/settings change was made.

## Feature and remaining design

Music audio access is in Launcher Settings > Now Playing. Already granted skips the system request; missing access gets Not now/Continue and the genuine Android request. Denial does not re-request automatically; Open settings is deliberate. This uses a separate permission Activity/callback, not voice recognition. No new uses-permission declaration or microphone listening is added by this feature.

Actual Android Visualizer audio sampling and VU bounce remain unimplemented. Ryan's agreed simple direction is unchanged: animation speed controls blinks; real music loudness controls vertical bounce separately, with quick rise and softer fall. No BPM detection, song lookup or physical-microphone fallback. Do not equate the prompt, an APK install or granted permission with audio-reactive motion.

## Verification checkpoint

Red7cb39385 /run34776776511 showed five missing-feature/version failures and three v163-only snapshots skipped; logs read. Green source cac499bf /run34776875654 succeeded. It checks actual pure-Java permission decisions plus source/manifest integration and v163-only preservation. The one-release snapshot checks intentionally do not freeze later versions. Reviewed scoped diff in-session; no independent reviewer claimed. The existing full signed Unified pipeline and installation still need completion at this checkpoint.

## Accepted work to preserve

Ryan accepted integrated v162 native lyrics/Skip with "perfection". Internal Activity routing, shared parser/client/loader/presentation and the passive licence footer at half its old size stay intact. Lyrics Lab was retired after acceptance; do not reinstall it. Existing v162 Now Playing manager, state bus and callbacks stay unchanged. Do not reopen unreported catalogue/natural-completion/offline coverage as a blocker to accepted work.

Accepted v162 source1e0136ffa9732353035f88ca7a7cb131f7481557, run34773509395, artifact10322553107, APK cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171. Permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde. Earlier v161 speed on Shield/Pixel7 and colour both ways remain accepted; speed local, colour shared. Prior complete root memory/handoff/status remain at112d09b5; dated receipts remain in docs/handoffs.

## Workflow

GitHub owns source/tests/builds/signing/handoffs. Deliver v163 to Shield only using existing package update, preserving data, HOME and grants; installation verification is distinct from Ryan's runtime acceptance. Leave Music Lab installed unless separately asked to remove it. No physical Pixel10, other-phone operations, emulator gates, permission grants, data clears, signing replacement or connection upgrade. Desktop Commander0.2.47 unchanged. No local app source edits/builds or checkout-sync claim. Main owner mapping is unchanged.
