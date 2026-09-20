# Shield v236 Flow button

2026-09-20. Ryan requested replacing the Now Playing Close player button with Deezer Flow, labelled exactly Flow. Baseline is user-accepted v235; remote/source were verified at7db7295186c7b4a5ce02e2ee5f652c38cbd8d73c before editing. Existing task-owned worktree: .worktrees/boop-deezer-invisible-v235. Primary checkout and concurrent work are untouched.

## Change

The existing130x44dp slot, margins, focus traversal and accent styling are preserved. The Flow callback replaces the old Close player callback in that button only. Close media remains available through its existing separate route. Lyrics, favourite/dislike source, invisible heart helper, artwork, transport geometry and audio/voice/HA logic are untouched.

Flow sends the existing https://www.deezer.com/flow URI directly through the selected native Deezer MediaController.playFromUri with a non-null extras Bundle. This URI was already used in BOOP's native music/voice route. No new display/helper, UI launch, network lookup, shell call, permission, login or dependency is needed for Flow.

The request is restricted to an eligible selected native deezer.android.app session with freshly advertised ACTION_PLAY_FROM_URI. Cast, other players, missing sessions or unsupported URI control produce an unavailable message rather than a visible fallback or the wrong command. A1.2second same-session repeat guard prevents accidental duplicate launches. Dispatch is logged as REQUESTED, not claimed to be provider-confirmed playing. Actual physical Flow playback remains to be tested on the newly built APK.

## Verification before source publication

Two tests were observed failing first: the old button still said Close player, and the native Flow controller did not exist. Both now pass. The real controller executes18 behavioural assertions against test-only Android boundaries: correct Flow URI, non-null extras, missing/wrong/Cast players, live capability loss, paused playback, duplicate clicks, failed dispatch/retry and fresh sessions. A source integration contract checks callback wiring, unchanged slot dimensions and separate Close media preservation.

Ten focused Flow/favourite/invisible-heart/alignment tests passed locally. Production diff and git whitespace checks were reviewed. Review is self-review, not an independent review. New tests are added to the full signed build workflow and the lightweight Deezer checks. Full signed CI, actual APK verification and physical button use are pending this source commit. No new emulator run or live recording. No app install or media action yet for this change.

Shield version236 /1.2.236-shield. Wall version207 unchanged; the shared build is not a separate requested Wall update. Existing permanent signer and packaged native/art checks remain required. Keep the accepted v235 favourite round trip as a regression checkpoint.