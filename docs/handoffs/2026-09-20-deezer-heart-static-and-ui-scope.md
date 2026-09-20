# Deezer hearts: clarified scope and matched-package inspection

Updated 2026-09-20 after Ryan's reported power outage. Investigation only; no new BOOP app build or installation.

## Authoritative user decisions

Ryan pressed the SAME normal favourite heart twice: first add/fill, then remove/outline. He did not press the crossed-out heart. The separate crossed-out heart means dislike and immediate skip.

The requested BOOP layout is now:
- Lyrics LEFT: dislike-and-skip icon; outlined, ordinary focus highlighting only.
- Lyrics RIGHT: a true favourites toggle, not add-only.
- HOME Now Playing: the same favourites toggle.
- Both favourites toggles must fill with the CURRENT user-selected launcher accent when saved and return to outlines when not saved. No hardcoded orange. Dislike must not gain a saved-state fill.

These instructions supersede v234's explicit remove-left/add-right design. The old BOOP_UNIFIED_MEMORY.md favourite section describes v234 implementation, not this newly approved target. No updated implementation is yet claimed.

Ryan authorized automatic installation of the next verified BOOP Shield APK and normal next-track action(s) to attract attention once ready for his test. Use a normal transport skip, not dislike, for that signal. No install or ready signal has happened in this continuation.

Keep NO LIVE RECORDING and NO repeated UI/screenshot polling. They made navigation unusable. Offline inspection and bounded post-action reads are allowed; do not resume a recorder merely because RDC is connected.

## Recovery and inspection evidence

RDC and ADB reached the intended Shield after the interruption. The existing cached provider base APK is exactly the installed package: independently computed SHA-256 values match, `6ae269a08a4a5d3c66084cdd056b5f7339780549a19e2939351588f9a5a1df15`. Package is deezer.android.app version1.0.1.1 / 301000101.

The interrupted inspection had opened a task-owned viewer instead of producing CLI output. That viewer was stopped. Existing Java17 and JADX1.5.6 were reused with the actual CLI entry. No new tool installation, provider modification, signing substitution or permission change.

The CLI finished with 585 decompilation errors. Treat the result as partial, not a complete readable reconstruction. Targeted readable code traces the normal heart from the UI callback through the current track ID and inverted current favourite state to separate add/remove track-favourite mutations in Deezer's network/data layer. The resulting Boolean is fed back into the player's favourite-state stream. The separate ban callback was located, but its large coroutine body was not readably reconstructed; dislike-and-skip semantics come from Ryan's explicit report.

The inspected manifest makes the provider's media-browser service and media-button receiver non-exported. The exported search provider rejects insert/update/delete. A media-session callback forwards custom command/action objects into SDK streams, but no functional favourite/dislike consumer or externally callable favourite entry was established by this inspection. Do NOT invent a command ID or claim that no alternative could ever exist.

Prior live evidence remains ratingType0, SET_RATING absent, custom actions empty. Thus the v234 route is still a verified physical failure for this installed provider. No account credentials, cookies or tokens were read. Raw APKs, reconstructed source, logs and screenshots stay private on Ryan's computer.

## Next decision, not yet an implementation

A small feasibility probe could deliberately bring Deezer's own player forward, identify its actual control, issue one requested action, obtain a provider-state receipt, and return to BOOP. This would be a visible app handoff rather than the originally hoped-for invisible media-session command. Ask Ryan whether that UX trade-off is acceptable before implementing it. It must be on-demand and bounded, not live recording or repeated UI polling. Unknown state, changed tracks, ambiguous controls, missing capabilities, and failed receipts must fail visibly without accidental dislike or optimistic favourite fill.

The native UI handoff has NOT been proved in this continuation; do not ship a replacement APK based on assumed success. A separate authenticated account integration would require its own deliberate authorization/design and is not established as available here.

## Unchanged source and verification level

Owning live branch checked at bca4340b5c673c4cdc8b0c98ca102dad060cfdb4 before documentation changes: boop-shield-weather-focus-v221. BOOP app source remains ccbd42cf3c4dc77614425f675e80fa8a3f146d20 (v234). No new application tests, build, device install, track skip or Windows source-checkout synchronization is claimed. Source-path tracing, matched provider package hash and a no-capture-process check are the new evidence.

Preserve accepted v233 lyrics, artwork, spacing, voice, audio and HA behavior. Existing v234 artifact and historical test receipts remain in the prior handoffs. Do not present those tests as verification of the unimplemented new route.
