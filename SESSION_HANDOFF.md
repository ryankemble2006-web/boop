# Unified v163 music audio access: source integrated, signed build pending

Updated 2026-09-13. Owner remains `boop-unified-eye-sync-safe-v159`. The branch suffix is not the app version. Integration branch: `boop-unified-music-v163`.

## Latest request and scope

Ryan reported Music Lab would not load Now Playing and requested: "roll this into 162 bump it to 163". This authorizes bringing the implemented music-permission entry into current Unified. It supersedes the earlier no-merge instruction for this small integration, not for unrelated operations.

Fetched LIVE accepted v162 at `112d09b5b446d6582954a6d89b3700fe16298ecb`. The v163 source is `cac499bf9e6ab325faa2d843112f90ace71118b1`, based directly on that live owner. After reviewing the scoped diff and passing its integration workflow, the owner was fast-forwarded without force. No old lab tree, package rename, Home/assistant/boot removals or other lab changes were imported.

App changes: the two reviewed MusicAudioPermission classes, six ShieldHomeSettingsView lines, one private activity registration added to the existing v162 manifest, and version `163 / 1.2.163-music-audio-access`. Package remains `com.boop.alpha1`; signing configuration is unchanged. Now Playing observation, lyrics, launcher callbacks, voice, animations, colour, artwork and materialization scripts are unchanged from accepted v162.

## Evidence so far

Read-only Shield diagnosis found the Music Lab ShieldNowPlayingListenerService was NOT enabled in Android's notification-listener setting, while Unified's was enabled. Unified reported `162 / 1.2.162-native-lyrics`. This is a concrete missing-access finding, not an exhaustive runtime diagnosis or a new Now Playing acceptance test. No permission/settings change or app input was issued.

RED integration source `7cb39385aa4f5f20502795c8c4d68e438e37c61e`, run `34776776511`, job `103776215998`: five failures because the prompt/version were absent, three v163-only snapshot checks skipped. Failure logs read before copying app code.

GREEN source `cac499bf9e6ab325faa2d843112f90ace71118b1`, run `34776875654`, job `103776490381`: integration workflow SUCCESS. It runs the conditional permission tests and v163 app-input/manifest/transplant checks. No independent review or device UI acceptance is claimed. The existing full signed Unified pipeline is the next verification stage; no v163 APK or installation is yet claimed here.

## Preserve accepted features

Unified v162 native lyrics and Skip were USER-ACCEPTED ON SHIELD when Ryan said "perfection". Keep the internal lyrics Activity and half-size passive licence footer. The obsolete Lyrics Lab was removed after that confirmation; do not reinstall it.

Accepted v162 APK SHA256 `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`; source `1e0136ffa9732353035f88ca7a7cb131f7481557`; signed run `34773509395`; artifact `10322553107`. Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Full prior handoff/status/memory remain at base `112d09b5b446d6582954a6d89b3700fe16298ecb`, with original dated acceptance receipts retained.

Earlier v161 speed acceptance on both Shield and Pixel 7, automatic two-way colour, original 1x timing and approved art remain protected. Speed is local; colour is shared. Neither phone is an installation target for this continuation.

## What this feature does and does not do

Launcher Settings > Now Playing > Music audio access checks RECORD_AUDIO, skips the OS request when already granted, otherwise offers Not now/Continue. Continue requests Android access. Denial does not loop; Open settings is a separate choice. The permission callback does not invoke voice recognition or start audio sampling. Existing manifest uses-permission declarations are unchanged.

Actual Visualizer sampling and music-driven bounce remain UNIMPLEMENTED. The design remains music level controlling bounce height independently of blink speed, with no physical microphone fallback or beat/BPM analysis. Do not report the permission-only integration as a dancing feature.

## Next delivery and boundaries

Complete the normal full signed GitHub build, verify artifact/package/version/signature/hash, then update only Shield's existing Unified in place. Preserve data, permissions and HOME; verify their relevant readbacks, not just ADB Success. Ryan owns runtime/visual acceptance. Leave Music Lab installed unless removal is requested. No phone/emulator actions, grants, data clears, key substitutions, connection upgrades or local app-source edits/builds. Laptop checkouts are not claimed synchronized. Main remains the context hub and needs no ownership change.

Current receipt: `docs/handoffs/2026-09-13-unified-v163-music-permission.md`.
