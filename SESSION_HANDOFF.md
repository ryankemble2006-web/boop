# BOOP current handoff

Updated 2026-09-20. Owner lineage: `boop-wall-shield-split-v207`. Live Shield iteration branch: `boop-shield-weather-focus-v221`. Keep the established long-lived branch; its suffix is not the app version.

## Latest: v234 favourite route failed physical use; log-only investigation

Ryan's screenshot showed the right lyrics heart returning `Deezer isn't exposing this favourite control.` After RDC was restored, the installed native Deezer media session was read before and after user button actions. It reports rating type 0, action mask 273714 without SET_RATING, and no custom actions. The media-session routes implemented by v234 are therefore unavailable on this installed provider. Do not call this feature physically working.

Live screenshots/UI hierarchy capture slowed the Shield until Ryan could not navigate. He explicitly requested NO LIVE RECORDING and an action-first/log-read-after workflow. Capture was stopped. Do not restart recording or repeated UI polling without a new request.

After the latest user-operated add/remove sequence, one bounded read of Deezer's own main/system logs since the recorded baseline returned only two garbage-collection entries. No favourite command or response was logged in that retrieved window. A one-shot capability read remained unchanged, and a process check found no recording/screenshot/UI-dump processes. No remote input was sent.

Offline review recovered a previously saved full-player screenshot and its UI sample. The player shows a normal heart and a separate crossed-out heart; heart-area clickable nodes carry no resource ID, content description or useful checked state. This is layout evidence, not proof of either button's internal meaning. Confirm normal-heart unfavourite versus the separate crossed-heart operation before remapping anything. The approved BOOP layout remains explicit remove-left/add-right on lyrics and one HOME toggle.

Next useful investigation: inspect the installed provider's matching APK/heart handler offline with existing tools, without live UI polling. No alternate working route has been found or implemented. Details and evidence limits: `docs/handoffs/2026-09-20-deezer-heart-live-diagnosis.md`. Raw diagnostics and screenshots stay private.

## Unchanged signed v234 artifact

Package `com.boop.shieldoverlay`, version234 / `1.2.234-shield`.
Source/build commit `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`.
Successful signed workflow `35525392054`, job `106116533781`.
Artifact `10609691434`, `BOOP-Shield-v234-Wall-v207-Signed`.
Shield file `BOOP-Shield-v234.apk`, 160518509 bytes.
APK SHA-256 `58d151cd8a91142a3372e7efacdf59edc771dad8a7eddd69aa892b7d4f3876b9`.
Certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `5871cfe13020d5e4f9cf50abf9177195403978c3704bd1c5569548236ad10533`.

Existing receipt, source-review and test history remain in `docs/handoffs/2026-09-20-shield-favourite-hearts-v234.md`. Those CI/boundary checks did not validate the installed Deezer app. This continuation is docs-only: no app changes, fresh build, install, permission change or source-checkout synchronization.

## Preserve accepted work

Ryan accepted v233 as the baseline. Preserve the Deezer-first/LRCLIB timed lyric fallback, v231 artist browsing, v230 title clipping fix, v229 centred column/spacing, v228 marquee, v226 bitmap corners, v224 three-button transport, v223 weather geometry, v221 focus, v220 accent, v217 reordering and all voice/audio/HA behaviour.

Keep the favourite controller's no-guessed-action, no-optimistic-state, bounded-pending and stale-track guards when replacing its unavailable route. Never reinterpret dislike or recommendation exclusion as unfavourite without an explicit change of scope.

Wall remains version207. Shared-code CI builds are not separate requested Wall releases or asserted byte-identical artifacts.

Accepted v233 rollback: source `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`, docs/base `52afeddb66dcca23aaf9ec5c17aa3218cc1eeb9c`, APK SHA-256 `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`.

Durable implementation rules remain in `BOOP_UNIFIED_MEMORY.md`; this live diagnostic handoff supersedes its earlier statement that installed-provider capability had not yet been inspected.
