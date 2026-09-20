# BOOP current handoff

Updated 2026-09-20 after Ryan's reported power outage. Owner lineage: `boop-wall-shield-split-v207`. Live Shield iteration branch: `boop-shield-weather-focus-v221`. Keep the long-lived branch; its suffix is not the app version.

## Latest: correct heart semantics established; native route still unresolved

Ryan clarified that he toggled the SAME normal heart twice: add/fill, then remove/outline. The separate crossed-out heart is dislike-and-immediately-skip and was not used in his favourite test.

Approved target: Lyrics LEFT dislike+skip (outline, normal focus only), Lyrics RIGHT favourites toggle, HOME Now Playing one favourites toggle. Both favourites toggles fill with the current launcher accent-slider colour when saved and become outlines when unsaved. NO hardcoded orange and NO saved-state fill on dislike. This supersedes v234's remove-left/add-right semantics and the historical favourite section in BOOP_UNIFIED_MEMORY.md. No new implementation is yet claimed.

Ryan has authorized automatic installation of the next verified Shield APK and a normal next-track action to attract his attention when it is ready to test. Do not use dislike for this notification. Neither installation nor ready signal has happened in this continuation.

## Diagnostic result and required next decision

The installed-provider media-session route in v234 FAILED: unavailable-control message on the physical Shield; rating type0, action mask273714 without SET_RATING, and an empty custom-action list before/after provider-button tests. The later log-only test exposed only two memory-cleanup entries, not the favourite request. Prior saved UI data has clickable heart nodes without useful resource IDs/descriptions/checked state.

After the interruption, RDC and ADB reached the intended Shield. Cached provider APK and installed base APK match exactly by SHA-256. Existing tools completed a partial offline reconstruction with 585 decompilation errors. Targeted readable code traces the normal heart to Deezer's own track-specific add/remove network/data mutations and then back to its favourite-state stream. The separate ban callback is identified; its complete body was not reconstructed. No usable external favourite/dislike entry was established. No credentials were accessed.

Next proposal: a feasibility test of a brief, deliberate switch into Deezer's real player to perform the requested UI action and return to BOOP. This is different from an invisible media-session action. Obtain Ryan's agreement to that visible handoff before implementing it. Do not claim the alternative already works or build another guessed-command APK.

Detailed decisions, evidence and limitations: `docs/handoffs/2026-09-20-deezer-heart-static-and-ui-scope.md`.
Prior physical/log evidence: `docs/handoffs/2026-09-20-deezer-heart-live-diagnosis.md`.

## Capture and publication boundaries

NO LIVE RECORDING and NO repeated UI/screenshot polling: they slowed navigation until Ryan could not use the Shield. Do not restart them. Offline source inspection and bounded after-action reads are the agreed diagnostic pattern.

This continuation made no app-source changes, fresh application tests/build, install, new device screenshot, UI input, permission change or signing change. A process check found no screenrecord/screencap/uiautomator on the Shield. Raw diagnostics, matching third-party APK and reconstructed source remain private on the laptop. No Windows BOOP checkout synchronization is claimed.

## Unchanged signed v234 artifact

Package `com.boop.shieldoverlay`, version234 / `1.2.234-shield`.
App source/build `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`.
Historical successful signed workflow `35525392054`, job `106116533781`.
Artifact `10609691434`, `BOOP-Shield-v234-Wall-v207-Signed`.
Shield file `BOOP-Shield-v234.apk`, 160518509 bytes.
APK SHA-256 `58d151cd8a91142a3372e7efacdf59edc771dad8a7eddd69aa892b7d4f3876b9`.
Certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `5871cfe13020d5e4f9cf50abf9177195403978c3704bd1c5569548236ad10533`.

Existing test/build receipts remain in `docs/handoffs/2026-09-20-shield-favourite-hearts-v234.md`. They did not validate the installed Deezer app and do not establish the new route.

## Preserve accepted work

Ryan accepted v233. Preserve Deezer-first/LRCLIB timed lyric fallback, v231 artist browsing, v230 title clipping fix, v229 centred column/spacing, v228 marquee, v226 bitmap corners, v224 three-button transport, v223 weather geometry, v221 focus, v220 accent, v217 reordering and all voice/audio/HA behavior.

Keep no-guessed-action, no-optimistic-state, bounded-pending and stale-track guards when replacing the unavailable favourite route. Dislike is now explicitly requested as a separate left-hand action; never treat it as unfavourite or quietly use it as a fallback.

Wall remains version207. Shared-code CI builds are not separate requested Wall releases or asserted byte-identical artifacts.
Accepted v233 rollback: source `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`, docs/base `52afeddb66dcca23aaf9ec5c17aa3218cc1eeb9c`, APK SHA-256 `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`.
