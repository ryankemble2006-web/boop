# BOOP current handoff

Updated 2026-09-20. Owner lineage: `boop-wall-shield-split-v207`. Live Shield iteration branch: `boop-shield-weather-focus-v221`. Keep this long-lived branch for the established writable Gradle cache; its suffix is not the app version.

## Current Shield: v234 signed, favourite controls awaiting physical validation

Ryan explicitly accepted v233 as the starting point and approved this layout: lyrics REMOVE heart to the left of Prev / Play-Pause / Next, ADD heart to the right; HOME Now Playing gets one toggle heart after Next. Existing transport positions, text/artwork/progress geometry and v233 lyric loading remain unchanged.

Package `com.boop.shieldoverlay`, version `234` / `1.2.234-shield`.
Source/build commit: `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`.
Successful signed workflow `35525392054`, job `106116533781`.
Artifact `10609691434`: `BOOP-Shield-v234-Wall-v207-Signed`.

Deliver `BOOP-Shield-v234.apk`, **160518509 bytes**.
APK SHA-256: `58d151cd8a91142a3372e7efacdf59edc771dad8a7eddd69aa892b7d4f3876b9`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `5871cfe13020d5e4f9cf50abf9177195403978c3704bd1c5569548236ad10533`.

## Control capability and acceptance boundary

The new shared controller uses the existing selected Android media session, native `deezer.android.app` only. It sends heart ratings only when HEART plus SET_RATING are advertised, or actual provider-published custom actions with an exact track-favourite label. Dislike, thumbs, artist/playlist operations and guessed action identifiers are excluded.

Unknown is not unsaved. The HOME toggle never guesses unknown state. Explicit lyrics buttons remain add/remove, not blind toggles. Provider confirmation is required for a saved/removed success message; pending is single-flight, bounded to three seconds and invalidated by track/session changes. Immediate caller-side identity checks include media ID where available, but Android's rating endpoint is not provider-side track-ID-atomic.

**The installed Deezer app's capability and actual add/remove round trip were not inspected or tested on the Shield in this task.** Unsupported sessions show a neutral/unknown heart and explanatory message. This is a signed, capability-gated implementation, not a claim of physical Deezer success. Cast favourites are not supported by this candidate. Ryan owns layout, D-pad and provider acceptance.

Physical test: on native Deezer, add an unfavourited track with the right lyrics heart; confirm in Deezer and HOME; remove with the left heart; verify HOME reflects the confirmed state. Also test a track transition during a request. An unavailable message must be reported as missing provider capability, not dismissed as successful integration.

## Verification

Test-first policy RED was observed in run `35524653617`, job `106114595440`, before implementation. The focused favourite workflow `35525392074` passed. Three behavioural tests exercise 70 assertions, including the actual controller against test-only deterministic Android boundaries. The 94 focused source checks passed; the full signed workflow additionally passed inherited v206 functional checks, split materialization/integration, HA unit-test task, both app builds and packaged signer/native/art checks.

The downloaded archive and extracted Shield APK were independently checked for CRC, source receipt, SHA-256 and byte size. The actual APK v2 signing certificate fingerprint matches the permanent signer. All 16 native libraries and frozen artwork match the accepted baseline byte-for-byte. Cryptographic apksigner verification was performed in CI; extracting the certificate locally is not a second full signature-verifier claim. HA reports contain 20 tests with zero failures/errors/skips; unchanged Gradle tasks may be restored from cache.

Self-review was performed; no independent reviewer, device/emulator install, visual test, permission change or Windows synchronization is claimed.

## Preserve accepted work

Ryan's v233 acceptance supersedes the prior pending fallback re-test. Preserve Deezer-first timed lyrics, separate LRCLIB six-second fallback window, `track_name + q` search, 404 clean misses and strict identity/duration matching. Preserve v231 artist browsing, v230 title clipping fix, v229 centred column/30px gaps, v228 marquee, v226 bitmap corners, v224 three-button transport, v223 weather geometry, v221 return focus, v220 accent slider, v217 reordering and all voice/audio/HA behaviour.

Wall version remains v207 and no Wall APK is being delivered or installed for this Shield request. Both shells share compiled code, so the CI-produced Wall artifact is not asserted byte-identical to an earlier v207 APK.

Accepted v233 rollback source `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`; docs/base `52afeddb66dcca23aaf9ec5c17aa3218cc1eeb9c`. Its APK SHA-256 remains `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`.

Detailed record: `docs/handoffs/2026-09-20-shield-favourite-hearts-v234.md`. Durable rules: `BOOP_UNIFIED_MEMORY.md`.
