# Unified v164: installed and identity-verified on Shield

Updated 2026-09-13. Owner: `boop-unified-v164-music-bounce`.

Ryan requested installation and said Shield was free. Standard ADB install returned Success/exit0. Independent readback verified `com.boop.alpha1`, `164 / 1.2.164-music-bounce`, installed SHA256 `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`. Staged source/package/permanent signature and native Lyrics Activity were checked. Source `f9f65569250b9dc02602101ef4d56195824e0380`, signed run `34778178916`, artifact `10323694771`.

Preflight found version163, not162; the installed v164 APK was nevertheless built directly from accepted v162. HOME, UID, first-install timestamp, granted RECORD_AUDIO and enabled media-listener access remained unchanged. No data clear, uninstall, grant, reset, explicit app launch, playback command, phone/emulator action or source rebuild occurred. Only Shield was targeted.

**Implemented, not yet user-accepted:** real Visualizer-driven bounce, independent saved blink speed and conditional permission prompt. **Preserved in source:** v162 native Lyrics route/screen/Skip/footer, existing media manager, other confirmed features and artwork. Prior focused build/logic tests remain recorded, not rerun here.

No branch merge or accepted-owner advance. Music response and v164 runtime lyrics behavior await Ryan's test; installation success is not audio/visual acceptance. See SESSION_HANDOFF.md and `docs/handoffs/2026-09-13-v164-shield-install.md`. Full build evidence remains in `docs/handoffs/2026-09-13-v164-music-bounce.md`; prior root context is retained at `677009e53c52df3ec6c92ea75214df5e7c4dba0a`.
