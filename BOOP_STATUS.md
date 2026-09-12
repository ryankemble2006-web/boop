# BOOP Rally Shield status

Updated 2026-09-12. This branch owns only the adjacent Network Q Rally project. Main remains the shared context hub; do not interpret this file as the current Unified or standalone HOME status.

## Implemented and published

Branch/worktree `boop-rally-shield`, project `rally-shield/`, package `com.boop.rally`. One offline Android TV launcher selects the original RAC Rally and Rally Championship through a pinned native ARM64 DOSBox Pure core. It includes controller/keyboard mapping, a pause/return menu, app-private save locations and an optional prepared-pack importer. The app does not replace HOME or request Internet/storage/accessibility/microphone permissions.

Public source, behavioral tests and GitHub build/signing workflow are committed. Proprietary game files remain private. Original desktop game folders were preserved. This is an emulator-host port, not a rewritten game engine or PC-streaming shortcut.

## Installed candidate

Version 1 / 1.0, source `71c71157fd9b6931b3c7d320409fa32d94f72197`.
Actions run `34688081241` succeeded; artifact `10296216335`.
APK SHA-256: `cb3b37efd9dfe4b5ea1850c4fe3ff9155e3c319ecefa1855ee1ae6f408e192d8`.
APK size: 1,602,630 bytes. Permanent BOOP signer verified.

Installed successfully on the positively identified Shield. Both private game packs were provisioned and hash-verified. Both titles booted; advancing frames and nonzero PCM were observed. Championship's menus responded to keyboard navigation. Twelve behavioral tests passed, independently rerun from the release source. See `rally-shield/VERIFICATION.md` for details and limitations.

## Incomplete acceptance

Actual driving, physical gamepad feel, audible engine/co-driver/music, save/clean-return/reopen persistence and repeated game-switch/background-resume acceptance remain unfinished. No complete race was verified. The laptop's remote file/command/ping requests timed out before these checks finished. The user remains visual/acoustic authority.

A separate `local.networkq.rally` APK also exists and sometimes took foreground. Its dialogs are not evidence about this build. It and Johnny Castaway were left unchanged.

## Continuation

Read this branch's live SESSION_HANDOFF.md and rally-shield/MEMORY.md. Fetch/check live GitHub before touching the local worktree: the final documentation was published directly while the laptop was not answering. Do not repeat preparation, replace the approved signer or merge this app into Unified. No post-install application-code changes were made in this session; later documentation builds do not establish a new device installation.
