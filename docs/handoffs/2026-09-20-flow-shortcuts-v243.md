# Deezer Flow shortcuts: Shield243 / Wall210

## Current: Flow shortcuts installed on Shield243 / Pixel7 Wall210

Ryan requested music and play some music as shortcuts for the existing play music -> Deezer Flow behavior. Both now bypass catalogue search and use exactly that native Flow path; case/space/punctuation variants and play some music on Deezer are covered. No other resolver or UI behavior changed.

Signed source a026b967e69429b4c95fd698b571297c0e6f9ba3; successful run 35542528310; artifact 10615227739 (BOOP-Shield-v243-Wall-v210-Signed). Five new cases failed before the alias change and passed after; canonical core checks and 16 focused tests passed, followed by the full signed/inherited CI. Both APK signatures, source, hashes, sizes and ZIPs were independently checked. All 18 assets and 16 native libraries per APK match the prior 242/209 rollback exactly.

Both apps installed with data preserved; versions 243/210 read back. Each new phrase was injected into each installed HomeAssistantClient: all four tests reported resolved_flow, playback_requested and Done using existing pairing/registration. These are handler/native-route tests, not microphone or acoustic acceptance. Temporary helpers removed. APKs saved to task outputs and Desktop/APKBOOP; prior versions retained. No permissions, settings, artwork or voice-model changes. Details: docs/handoffs/2026-09-20-flow-shortcuts-v243.md.


[Successful signed build](https://github.com/ryankemble2006-web/boop/actions/runs/35542528310)

- BOOP-Wall-v210.apk: 160583929 bytes, SHA256 `a355817869463339787453ef5d2e54426244b044de20fbe4dd6aa71ad14d9f82`.
- BOOP-Shield-v243.apk: 160584045 bytes, SHA256 `dddd0c6ffd0dd74f517304b60f636bdf185125ec3a5fa3112ff842e79b5b8981`.
