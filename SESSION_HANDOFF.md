# Shared eye colour: signed v159, emulator checkpoint; speed still next

Updated 2026-09-13. GitHub feature owner: `boop-unified-eye-sync-safe-v159`.
Accepted physical-device source remains v156 `a901c1e9`; its Shield sweep, favourites and artist navigation are complete. Both old colour recovery branches remain preserved. GitHub, not a local draft, is the source of truth.

## Verified delivery

- Built source: `0a4134ebfe8049254378b4706d2ee1df73cd7e87` (implementation `dcebdedd251585e2dae0b414cb0c92fccf52f148`). Package `com.boop.alpha1`, version `159 / 1.2.159-shared-eye-colour`.
- Full GitHub build `34757337845` succeeded, including existing non-visual integration/animation/ownership/media gates, compilation, signing and archive verification. Artifact `10317198102`, name `BOOP-Unified`.
- APK SHA-256 `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`.
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, independently rechecked with the laptop SDK's apksigner before emulator installation. No replacement signer.
- Focused functional report: 235 Unified tests and 68 Shield tests, zero failures/errors/skips. Appearance contracts also passed; they are not evidence of actual Home Assistant synchronization.

## What is implemented

Appearance settings, private activity registration, Application startup and authenticated opt-in hue sharing are wired. The runtime guards invalid connection attempts and stale authentication work. Existing `boop_eyes/hue_degrees`, Wall hue controls, shaders, approved eye master and authored clips remain unchanged. Sharing is OFF by default on upgrade. No new Android permission, voice changes, unauthenticated UDP or implicit native-lyrics merge.

The recovered baseline `04f7c10c` passed four checks and failed the missing startup/settings wiring check. Its runtime file was complete; the earlier description of a truncated runtime was stale. The recovery window added tests at `9e452b43`, observed three expected failures, and another writer retained those tests in the completed implementation. Do not reapply the superseded, unreferenced competing implementation `c5016ef7`.

## Emulator results actually observed

The recovery window installed the verified signed APK with `adb install -r` on emulator-5570 (Pixel_10_Pro_XL_API_36) and emulator-5572 (BOOP_Android_TV_API_36), and read back version 159 on both. No `-g`, permission changes, data clearing or physical-device installation was performed by that window.

Before upgrading, private rollback APKs were captured from phone v151 and TV v156. Both initially had no persisted hue/sharing XML. Deliberate migration fixtures used hue 73 on the phone and 288 on TV; both exact values survived the APK upgrade. Cold launches of MainActivity with the existing voice-settings extra returned Status: ok on both. The phone's voice settings screenshot was inspected locally; this is not full visual acceptance.

Private receipts, rollback APKs, fixtures, downloaded artifact and screenshots are under `%TEMP%/boop-colour-v159-checks` on the laptop. The migration colours are test fixtures, not Ryan's personal colour preferences. Do not clear unrelated app data or tokens to remove them.

## Remaining gates and coordination

The branch advanced from another writer during recovery; those changes were preserved rather than overwritten. A later phone UI dump showed the profile screen after an attempted voice-settings scroll. That unexpected transition has not been attributed conclusively to another test session versus app routing. Avoid simultaneous emulator inputs; establish one test owner and investigate the transition.

A final read-only device/log receipt request was blocked by the tool safety layer with an indeterminate safety status. It did NOT run. No bypass, permission change or reauthentication was attempted. This does not invalidate the earlier verified install/read-back/launch results, and does not mean GitHub source editing is blocked.

Still unverified: appearance-screen interaction/Cancel/persistence after UI edits; actual two-device Home Assistant synchronization; disconnect/reconnect and server-change behaviour on Android; physical Shield/Pixel 7 acceptance. Pixel 10 physical deployment remains excluded. Do not describe colour as fully runtime-approved yet.

Next: finish those controlled colour tests, then independent animation-speed work with exact 1x equivalence. Speed has not been implemented by this continuation. Ryan owns visual acceptance. Primary research and the approved scope remain in `docs/superpowers/plans/2026-09-13-shared-colour-and-speed.md`; recovery coordination details are alongside it in `2026-09-13-colour-recovery-validation.md`.
