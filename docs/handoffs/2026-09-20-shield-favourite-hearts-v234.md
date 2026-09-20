# Shield v234 favourite hearts: signed build, physical provider test pending

Ryan approved implementation and accepted v233 as the baseline on 2026-09-20. Live base was `52afeddb66dcca23aaf9ec5c17aa3218cc1eeb9c` on `boop-shield-weather-focus-v221`. Keep this branch long-lived for CI cache reuse.

## Requested layout

Lyrics has explicit REMOVE on the left of Prev/Play-Pause/Next and ADD on the right. All three existing transport positions, artwork, text and progress geometry remain unchanged. HOME Now Playing has one toggle after Next; existing horizontal positions are retained. Both use the selected accent and explicit D-pad navigation.

## Control boundary and limits

A single lifecycle-bound controller uses BOOP's existing selected Android media session, not a new player. Native Deezer only. Heart-rating commands require advertised RATING_HEART and ACTION_SET_RATING; custom commands use the actual provider-published identifier and an exact track-favourite label. Dislike, thumbs, artist and playlist commands are excluded. No private API, credential extraction, new permission, audio-focus request, provider launch or automatic install.

State is provider-confirmed. Unknown is not treated as unsaved. The toggle cannot guess unknown state; explicit buttons never become blind toggles. Requests are single-flight, bounded to three seconds, and recheck session, title/artist/album/duration and media ID immediately before sending. Android's rating endpoint is not track-ID-atomic; this is an application-level stale-selection guard, not a claim to control a provider-side race. Track/session changes cancel pending confirmation; duplicate sends, provider exceptions and missing capabilities have visible outcomes. Both screens subscribe to the same state; no optimistic saved message is emitted.

**The installed Deezer app's heart-rating/custom-action support has NOT been physically inspected or confirmed in this task.** A successful build or deterministic boundary test does not prove Deezer accepts these commands. Unsupported sessions show a neutral/unknown heart and explanatory message. Cast support is not asserted. Ryan must test actual add/remove and cross-screen state separately from layout acceptance.

## Source and verification

Production/build source `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`.

Test-first commit `09d6f8a3736bea9cb2621fd29673ed98bf779f5c`, run `35524653617`, job `106114595440` failed as expected because the production favourite policy did not exist. Its log was read before implementation. A second lifecycle test was observed failing before its implementation.

The three favourite tests execute 70 assertions including the real controller compiled against deterministic Android boundary doubles: add/remove, no optimistic success, shared state, actual published custom-action IDs, stale track/media IDs, unknown state, timeout, provider errors, Cast exclusion and listener teardown. Doubles are test-only and not packaged. The favourite workflow `35525392074` passed.

94 established/favourite focused checks passed. The version-pinned weather release assertion was updated from v233 to v234; weather source is untouched. Diff review passed; no independent reviewer or visual/device test is claimed.

Full signed run `35525392054`, job `106116533781`, completed successfully: focused prechecks, inherited v206 checks, split materialization/integration, HA task, both app builds and package/signature/native/art verification. HA reports contain 20 tests with zero failures/errors/skips; unchanged Gradle tasks may be restored from cache.

## Verified deliverable

Artifact `10609691434`, `BOOP-Shield-v234-Wall-v207-Signed`.
ZIP SHA-256 `5871cfe13020d5e4f9cf50abf9177195403978c3704bd1c5569548236ad10533`.

Shield package `com.boop.shieldoverlay`, version234 / `1.2.234-shield`.
File `BOOP-Shield-v234.apk`, 160518509 bytes.
APK SHA-256 `58d151cd8a91142a3372e7efacdf59edc771dad8a7eddd69aa892b7d4f3876b9`.
Permanent certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The downloaded ZIP and extracted APK passed CRC and SHA checks; the receipt source and package/version match. The actual APK v2 certificate fingerprint matches the permanent signer; full cryptographic apksigner verification ran in CI. All 16 native libraries and frozen art were independently compared byte-for-byte with the accepted baseline.

Wall remains version207. Both app shells compile shared code, so its rebuilt artifact is not claimed byte-identical to a former v207 APK. This task delivers Shield only and performs no device installation. Permissions, signer, voice, native bytes, art, audio, HA, v233 lyric fallback and provider browsing remain unchanged. No Windows synchronization is claimed.

## Next physical check

Play an unfavourited track in native Deezer. Add with the right lyrics heart, verify the actual Deezer favourite state and HOME toggle, then remove using the left heart. Verify D-pad traversal and track changes. An unknown/unavailable result means the installed provider capability remains unresolved; it is not a successful favourite integration.
