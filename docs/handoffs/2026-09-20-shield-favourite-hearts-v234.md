# Shield v234 favourite hearts: source candidate

Ryan approved implementation and accepted v233 as the baseline on 2026-09-20. Live base was `52afeddb66dcca23aaf9ec5c17aa3218cc1eeb9c` on `boop-shield-weather-focus-v221`. The branch remains long-lived for CI cache reuse.

## Requested layout

Lyrics has explicit REMOVE on the left of Prev/Play-Pause/Next and ADD on the right. All three existing transport positions, artwork, text and progress geometry remain unchanged. Home Now Playing has one toggle after Next; all existing horizontal positions are retained. Both use the selected accent and explicit D-pad navigation.

## Control boundary and limits

A single lifecycle-bound controller uses BOOP's existing selected Android media session, not a new player. Native Deezer only. Heart-rating commands require an advertised `RATING_HEART` and `ACTION_SET_RATING`; custom commands use the actual provider-published identifier and an exact track-favourite label. Dislike, thumbs, artist and playlist commands are excluded. No private API, credential extraction, new permission, audio-focus request, provider launch or automatic install.

State is provider-confirmed. Unknown is not treated as unsaved. The toggle cannot guess unknown state; explicit buttons never become blind toggles. Requests are single-flight, bounded to three seconds, and recheck session, title/artist/album/duration and media ID immediately before sending. Android's rating endpoint is not track-ID-atomic; this is an application-level stale-selection guard, not a claim to control a provider-side race. Track/session changes cancel pending confirmation; duplicate sends, provider exceptions and missing capabilities have visible outcomes. Both screens subscribe to the same state; no optimistic saved message is emitted.

The installed Deezer app's heart-rating/custom-action support has NOT been physically inspected or confirmed in this task. A successful build or the deterministic boundary tests does not prove Deezer accepts the commands. Unsupported sessions show a neutral/unknown heart and an explanatory message. Cast support is not asserted. Ryan must test actual add/remove and cross-screen state, separately from layout acceptance.

## Evidence at source publication

Test-first commit `09d6f8a3736bea9cb2621fd29673ed98bf779f5c`, run `35524653617`, job `106114595440` failed as expected because the production favourite policy did not exist. The failure log was read before implementation. A second lifecycle test was observed failing before its implementation.

Scoped source review from the verified GitHub source export: 94 established/favourite checks passed. The three favourite tests execute 70 assertions, including the real controller compiled against deterministic Android boundary doubles: add/remove, no optimistic success, shared state, actual published custom-action IDs, stale track/media IDs, unknown state, timeout, provider errors, Cast exclusion and listener teardown. Doubles are test-only, not packaged. The version-pinned weather release assertion was updated from v233 to v234; weather implementation is untouched. Diff review passed; no independent reviewer or visual/device test is claimed.

Full signed pipeline and APK receipt are pending this source commit. Shield version 234; Wall version remains 207 and no Wall/device install is requested. Permanent signer, manifest permissions, voice, native libraries, art, audio, HA, v233 lyric fallback and provider browsing are unchanged. Windows synchronization is not claimed.
