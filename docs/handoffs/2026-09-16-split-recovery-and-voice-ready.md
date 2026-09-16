# Supplemental evidence: recovery APKs retained; current Voice opened

Date: 2026-09-16. Owner: `boop-hand-colour-v191`.

This supplements `docs/handoffs/2026-09-16-wall-shield-split-prerequisites.md`. Concurrent advances to `926ce10b852328a73166ed87bfcc5e6a22968bd8` and then `de28b0762b616acfaff61cf246cb8d6670dea5c5` were detected and reread. An earlier unreferenced documentation draft was not published; the current supplement preserves the newest accepted-UI clarification and all existing document text.

## Current acceptance and gate

Ryan has accepted the v206 UI, including the latest Home layout. Earlier physical voice testing established natural voices and working pitch/cadence, but reported long Try Emma / TEST VOICE delays. Do not reopen a UI/focus acceptance gate or erase that voice progress. Outstanding prerequisites are current response-delay acceptance and cross-device voice-profile sharing, in coordination with the other window. No split implementation or clean install was started.

## GitHub evidence rechecked, not rerun

Built app source: `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`. Signed run `35099151524`, build job `104803873837`, reports all steps successful, including Voice/Home ownership, inherited functional checks, accepted assets, Johnny state isolation, Android build, permanent signer and packaged identity/content. Artifact: `10447197742`, `BOOP-Unified-v206-Idle-Home-Corner`.

All four source check jobs reported completed/success: runs `35099151524`, `35099151512`, `35099151480` and `35099151474`. No tests or application builds were rerun by this documentation/device-evidence continuation. Build success is not response-delay or sharing acceptance.

## Recovery APKs now retained privately

The currently installed base APK from each authorized target was pulled to a private `BOOP_RECOVERY/pre-split-20260916-142325/` directory outside the checkout. No app data was copied. These are existing-install recovery copies, not split replacements or a backup of onboarding.

| Target | Recovery filename | Package/version | APK SHA-256 |
| --- | --- | --- | --- |
| Shield | `Shield-Unified-v206.apk` | `com.boop.alpha1`, 206 / `1.2.206-idle-home-corner` | `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca` |
| Pixel 7 Pro | `Pixel7-Unified-v191.apk` | `com.boop.alpha1`, 191 / `1.2.191-hand-colour` | `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1` |

Both copies passed Android build-tools `apksigner verify --print-certs`. Both certificate SHA-256 digests match the current permanent-signer reference: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. No key was created, exported or substituted. Shield's APK hash also matches the GitHub build receipt. Pixel 7's installed identity/signature are verified; this continuation did not independently map its older APK to a build run.

Shield HOME resolves to `com.boop.alpha1/.UnifiedEntryActivity`; the only other enabled HOME candidate returned was `com.android.tv.settings/.system.FallbackHome`. Android Settings separately resolves to `com.android.tv.settings/.MainSettings`; the stock TV launcher is disabled. Nothing was enabled or reassigned. Recovery files and working ADB do not establish a physically tested replacement HOME/recovery route. No uninstall is permitted yet: both replacement APKs and the usable recovery route remain outstanding.

## Current Voice screen: narrow runtime observation

Only the connected Shield transport received navigation inputs: HOME, Home settings, BOOP device/room settings, then VOICE SETTINGS. No device/profile option was selected. No permission, name, room, voice model, backend, pitch or cadence setting was changed.

A fresh v206 screenshot shows unobstructed name/training controls, Pitch, Cadence and TEST VOICE, without full-screen felt eyes. The focused Pitch slider has a blue outline/track and the Cadence track is blue. This records the observed state; the UI already has Ryan's acceptance and does not need to be reaccepted. Captures and raw hierarchy remain private.

This continuation did not press TEST VOICE or adjust sliders, and did not take over the other window's voice timing test. Preserve its current planned timing sequence (Try Emma, TEST VOICE, then Try Emma again) and source timing diagnostics. Response-delay and voice-sharing acceptance remain open. Pixel 7 is still v191, so latest-to-latest sharing is not established; do not silently perform an in-place update in place of the separately authorized future fresh-install test.

The latest inspected Shield screen was its existing Voice settings. Pixel 7 kept its existing BOOP installation. Neither device is at fresh setup step one. No command targeted physical Pixel 10.

## Preservation and next step

The newest concurrent handoff, context, status, memory and split instructions are preserved. Dirty local v203 documentation was not modified or synchronized over. No app source, workflow, signing configuration, installation, app data, permissions, default HOME, Home Assistant or unrelated application was changed.

Finish the remaining Voice response-delay/sharing work with Ryan and the owning window, record accepted results and preserve every subsequent fix at the live owner. Only then proceed with the approved Wall/Shield shells and package-sensitive integration audit. The intended identities, no-profile-chooser routing, genuine Android confirmations, staged replacement/recovery requirements and stop-at-first-setup boundary remain unchanged.
