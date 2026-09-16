# Shield storage cleanup — 2026-09-16

## Scope and starting point

Ryan requested removal of old Shield clutter after a storage-blocked install. This was device maintenance only, not an app-source change or an installation request. Current main routing identified `boop-hand-colour-v191`; its live HEAD was checked at `572e8d6cb4726951df0604e18a9a8e614d93a543` before documentation edits. The local primary checkout was not treated as current app source. No local source, worktree, build tool or phone was changed.

## Verified result

Fresh ADB `df -k /data` immediately before deletion reported 1,909,016 KiB available and 85% used. Final readback reported 3,784,840 KiB available and 69% used. The measured gain was 1,875,824 KiB: approximately 1.92 decimal GB recovered, leaving approximately 3.88 decimal GB free (3.61 GiB). Android's storage monitor reported NORMAL both before and after; this session did not reproduce or identify the exact previously blocked install.

Removed 204 reviewed obsolete files:
- 103 downloaded APK installer copies: nine large obsolete Unified APKs followed by 94 older BOOP/launcher/Turbo/lab and other installer copies. No installed package was uninstalled.
- Three duplicate Forki installer files. All four original copies had matching SHA-256 hashes; the unnumbered original remains on the Shield.
- 84 old BOOP/Johnny/Rally screenshot and UI-dump files.
- 14 old temporary diagnostic captures/probes.

The latter cleanup passes were shallow, filename/type-scoped and age-filtered; current-day test files were not targeted. All deletion commands returned success. Final old-download-APK count was zero. Five unclassified ZIP archives were deliberately retained in Downloads, not deleted or moved to the laptop. Kodi/Forki libraries, settings, databases, installed add-ons, media and backups were not cleared. No app-data/cache reset, uninstall, permission change, force-stop, reboot or replacement signing occurred.

## Device identity after cleanup

The same 21 third-party package names were present before and after. Direct package readbacks showed these versions and update timestamps unchanged through cleanup:
- `com.boop.alpha1`: versionCode 200, `1.2.200-uniform-tv-chrome-voice-demo`.
- `local.johnnycastaway.halab`: versionCode 15, `1.14-reborn-ha-fast-poll`.

Default Home remains `com.boop.alpha1/.UnifiedEntryActivity`. Default screensaver remains `local.johnnycastaway.halab/local.johnnycastaway.shield.JohnnyDream`.

Important chronology: v200 was already installed when inspected; this task did not build, sign or deploy it. The earlier v197/v198 installation statements in this branch's voice-control receipt are historical, not the latest observed Shield identity. Preserve v200 and fetch the relevant current feature receipt before any later install; do not downgrade from an old next-step paragraph. This observation does not establish v200 source/hash provenance or physical feature acceptance.

## Verification boundary and next step

Cleanup and package/default identity checks are complete. The originally blocked installer was not retried, so its installation success is not claimed. No source changed and no build, emulator or playback/visual acceptance test was run. Any future install must use the explicitly requested candidate and its current published package/signer checks. This receipt contains sanitized results only; private addresses, raw dumps, personal captures and third-party APK contents were not published.
