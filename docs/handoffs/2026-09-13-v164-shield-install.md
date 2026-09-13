# v164 Shield installation receipt

2026-09-13. Ryan requested: "install it please, the shield is free".
Task branch: `boop-unified-v164-music-bounce`.
Starting task HEAD: `677009e53c52df3ec6c92ea75214df5e7c4dba0a`.

## Artifact and target

Only the connected target identified as SHIELD Android TV was operated. The exact already-signed artifact from run34778178916/artifact10323694771 was staged in a dedicated laptop Downloads directory with the existing GitHub CLI. No source checkout, build, signing-key replacement or connection upgrade.

Source `f9f65569250b9dc02602101ef4d56195824e0380`.
Package/version `com.boop.alpha1`, `164 / 1.2.164-music-bounce`.
APK SHA256 `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`.
Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Staged hash, embedded build receipt, aapt package/version, actual apksigner verification and packaged ShieldLyricsActivity registration all passed before installation.

Preflight found version163 /1.2.163-music-audio-access, APK SHA256 `d7921b2e982c10795c4d5e1fed6b16b6b00271bb00618e665aae3b7c1c73966c`. This supersedes assumptions about the installed baseline, not Ryan's earlier report or the new APK's accepted-v162 source lineage. The discrepancy was not investigated.

## Installation and independent readback

Ordinary `adb install -r` returned Performing Streamed Install, Success, exit0.
Separate readback completed exit0 and confirmed:

- Installed164 /1.2.164-music-bounce and base.apk SHA256 exactly matching the verified candidate.
- HOME resolution, app UID and first-install timestamp unchanged.
- RECORD_AUDIO granted=true before and after; no new permission granted.
- Unified ShieldNowPlayingListenerService access enabled=true before and after.

Only an in-place APK update was requested and performed. No uninstall, data clear, settings/colour reset, permission grant, explicit app-launch command, playback input, phone or emulator command was issued. Do not claim the app remained unopened: Android may manage/restart its existing Home/services during an update. No visual or audio acceptance was performed or inferred.

## Continuation

Ryan can now test installed164. Real Deezer readings and visible bounce remain unconfirmed. The native lyrics registration check is packaging evidence, not a new runtime Lyrics-button test. Prior build/test results are preserved in `docs/handoffs/2026-09-13-v164-music-bounce.md` and were not rerun for this install.

Documentation-only handoff/status/memory update belongs on this task branch. No merge or accepted-owner advance. Main/v162/other labs were not written. Private staging and comparison files stay on the laptop; no app checkout synchronization is claimed. The pre-installation root records remain available at the starting HEAD above.
