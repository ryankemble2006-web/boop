# BOOP Music Lab: installed alongside Unified on Shield

Updated 2026-09-13. Owner: `boop-music-lab-side-by-side-v161`. Ryan wants this fork separate while other operations continue. No merge into Unified, main, Lyrics Lab or another operation is authorized.

## Latest result: installation verified

After the earlier blocked attempt, Ryan explicitly requested another installation attempt. Fetched the LIVE task branch at `4968d324204f74bf26420effa8bb9991391d5b69` and read its current handoff. Retried the ordinary ADB install through the existing Desktop Commander connection on the identified Shield ONLY, using the same staged, signed Music Lab APK. No alternate connector, permission grant, tool upgrade or settings workaround was used.

Preflight confirmed the staged APK SHA256 and aapt package/version, the connected SHIELD Android TV, Music Lab absent, and existing Unified `162 / 1.2.162-native-lyrics`. The standard `adb install -r` returned `Performing Streamed Install`, `Success`, exit code 0.

A separate read-only verification process completed with exit code 0 and established:

- `com.boop.musiclab` installed, version `1 / 0.1.1-v161-audio-prompt`.
- Installed Music Lab base.apk SHA256 exactly matches the signed candidate below.
- Its Leanback launcher resolves to `com.boop.musiclab/.UnifiedEntryActivity`.
- Package state at readback: installed=true, stopped=true, notLaunched=true.
- Music Lab RECORD_AUDIO grant remains false. No permissions were granted with ADB.
- Unified remains `162 / 1.2.162-native-lyrics`; its APK SHA256 is identical before and after: `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
- HOME still resolves to `com.boop.alpha1/.UnifiedEntryActivity`.

**Current state: Music Lab is installed side by side on Shield, but not launched or user-accepted by this task.** No playback input, app launch, data clear, credential migration, HOME change, phone command or emulator action was performed. Only the requested lab was installed. The previous tool block and read-only helper correction remain historical facts in the full preceding handoff at `4968d324204f74bf26420effa8bb9991391d5b69`; they are not the current installation state. This successful retry does not establish why the earlier tool check failed.

## Exact installed candidate and lineage

Name: BOOP Music Lab. Package/application namespace: `com.boop.musiclab`. Independent lab version: `1 / 0.1.1-v161-audio-prompt`.

Source/build commit: `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`.
GitHub signed run: `34774532761`; job `103770090841`, SUCCESS.
Artifact: `BOOP-Music-Lab`, ID `10322813560`.
APK: `BOOP-Music-Lab-v1.apk`, 155257454 bytes.
APK SHA256: `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`.
Artifact ZIP SHA256: `27e9e146108ebf06370150727e20b0fc1198217b9e08aaa771cfcd76bc61fa82`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The staged APK's permanent signature was verified in the preceding attempt. This retry rechecked the exact staged bytes and compared the installed bytes to the same signed digest; it did not create, sign or rebuild an APK.

Parent: `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`, based on accepted v161 owner handoff `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`. This is not a replacement for installed Unified v162. Neither parent nor another app branch was changed.

## What is available for Ryan to test

Open BOOP Music Lab, choose its Shield profile, then Launcher Settings > Now Playing > Music audio access. Its private permission activity checks the real RECORD_AUDIO grant, skips a request when already granted, otherwise explains the Android permission and asks only after Continue. Not now/Back cancels; denial does not loop; Open settings is a separate choice. The entry does not reuse the voice callback or start a recorder, Visualizer, voice session, service or network request. The actual Android prompt, remote navigation and first launch have NOT been exercised here; Ryan operates the consent prompt.

**Real music-driven bounce is not implemented yet.** The agreed next design uses Android Visualizer loudness to set vertical bounce height, quick rise and softer fall, without BPM tracking or physical-microphone fallback. Saved animation speed continues to control blinks independently. Keep approved artwork, authored clips, exact original 1x timing and single-face ownership. Useful Deezer/Shield audio readings remain unproven.

## Preserved build evidence and isolation

`scripts/materialize-music-lab.py` copies the prepared v161-derived app to a separate build tree, adjusts app identity/internal references/label/version/task affinity/auth callback, and verifies its parent tree is unchanged. No shared UID or user-data copy. HOME/ASSIST/boot filters, boot permission and Home override registration are excluded. Existing com.boop startup protections and approved assets are preserved. No relay credentials were injected.

Historical build receipt: `docs/handoffs/2026-09-13-music-lab-fork.md`. It records the test-first fork failures and passing five fork checks, four permission tests including 18 Java decisions, six animation timing functions, materialized consistency, 11 focused canonical/manifest checks, signed assembly and packaged identity/signature/integrity. Its no-install statements describe that earlier build task. No new source tests or visual acceptance are claimed for this installation-only retry.

## Continuation boundaries

Keep the lab separate until Ryan explicitly requests integration. Later integration must reconcile the then-LIVE Unified successor, never copy this v161 base over newer work. Test device behavior jointly. No silent permission grants, phone/emulator operations or reopening accepted colour/speed repairs.

GitHub owns source/tests/builds/signing and durable handoffs. This retry changes repository documentation only; no laptop app source, dirty worktree or signing key was modified, and no local checkout synchronization is claimed. Fetch the LIVE task branch before future changes. Do not publish private device addresses, raw dumps, screenshots, credentials or keys.
