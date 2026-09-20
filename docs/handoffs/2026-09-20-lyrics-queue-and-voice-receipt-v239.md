# Shield v239 Lyrics Queue and native music reply race

2026-09-20. Base: bc53a2573fab45193e262a7e23b2f502cca61b00 on boop-shield-weather-focus-v221. Existing isolated worktree .worktrees/boop-deezer-invisible-v235. Primary checkout and other app branches preserved.

## User acceptance and request

Ryan accepted v238 Queue as working perfectly and approved the corrected clipping. He requests the same Queue from Lyrics for karaoke: one button under Play/Pause, centred, moving the music column upward without changing its gaps. Flow remains excluded. He also reports a phone-to-Shield voice action that succeeded but was spoken as Failed. The exact utterance and sending phone/version are not yet supplied; do not assert a captured phone failure or diagnose unrelated home-control commands from this report.

## Lyrics change

The same ShieldQueueDialog and event-driven DeezerQueueController are reused in Lyrics. The entry follows the shared native album/playlist visibility and matching media-session identity, so Flow/radio/unknown sources stay hidden. D-pad Down from the transport row reaches Queue, and Up returns to Play/Pause (or an available transport control). Queue opens over Lyrics and Back is expected to return to the same Lyrics screen, not HOME. Pause dismisses the dialog; Stop unsubscribes its entry and hides stale state.

Keep every existing left-column frame and translate the whole group upward 38 design pixels. New Queue is centred on progress/Play-Pause, 112x38 design pixels with 17px between the transport row and the button. This produces 16px top and 17px bottom margins at720p. Artwork/title/artist gaps and the horizontal centre do not change. Right-hand lyrics position, timed lyric providers, marquee, hard artwork mask, heart meanings and accent source are unchanged. The button uses existing focus chrome and selected accent, not fixed orange.

## Reproduced music reply defect

DeezerNativeController discarded Home Assistant's service-call response and separately read the entity's mutable latest adb_response afterward. A track change can trigger a concurrent Shield heart lookup between those requests. A deterministic regression executes the real native controller, confirms exactly one playback command occurred, then overwrites that shared latest response with a different heart nonce. The old code throws IOException and the voice client maps that to Failed even though playback was dispatched.

AdbCommandReceipt now first extracts the exact entity AND full first-line nonce from the state snapshot returned by that service call. It accepts no other command's response, rejects contradictory matching receipts, and uses the existing nonce-checked state GET only when the original service response contains no matching receipt. It NEVER repeats a playback action merely because acknowledgement was lost. Room, epoch, hardware and provider-result checks remain. Both music and heart callers use the same receipt reader; no change to actual playback or heart operations. This fixes the reproduced race; it is not proof that the user's unobserved utterance hit exactly this condition.

Home Assistant's REST contract returns changed states from service execution. Its AndroidTV integration writes adb_response from each shell-command result. Official sources reviewed: https://developers.home-assistant.io/docs/api/rest/ ; https://raw.githubusercontent.com/home-assistant/core/dev/homeassistant/components/androidtv/media_player.py ; https://raw.githubusercontent.com/home-assistant/core/dev/homeassistant/components/api/__init__.py . Public documentation is not evidence of the exact installed HA version, and the legacy fallback remains.

## Build and test scope

Shield239 /1.2.239-shield. Companion Wall208 /1.2.208-wall is needed to update the sending phone's music client; no phone installation is authorized or performed in this task. The shared split lineage was checked separately; older Wall-native branches were not overwritten. Do not replace a separately newer installed phone build without checking it. Natural voice models, pitch/rate, microphone permissions and providers are unchanged.

Before implementation the new Lyrics-entry/layout tests failed. The actual native music regression then failed AFTER its playback counter reached one, reproducing the false-failure boundary. After the fix,26 focused test functions passed, including22 race/rejection assertions and the existing Flow/heart/Queue/layout/provider tests. A separate real-Lyrics-Activity boundary harness passed47 lifecycle/transport/Queue assertions, including hidden Flow, wrong session, duplicate dialog and unsubscribe. Tests use synthetic network responses and no account credentials. Existing cached JSON dependency is reused locally and hash-pinned in CI. Diff whitespace and source self-review passed. No independent reviewer claimed.

Full signed CI and final physical Lyrics panel test are pending this source publication. No new device command, install, recording, permission or real playback operation has been sent in this task so far. Raw diagnostic work stays private; only synthetic test data and source are published.

## Initial CI provenance check caught a version-edit mistake

Initial run35537117004/job106148019380 compiled both apps and passed105 focused prechecks,128 materialized integration checks,18 inherited stages and the HA test task. Its final APK verifier correctly stopped at Baseline provenance mismatch before artifact publication: a broad238-to239 version edit had also replaced those digits inside the historical pinned APK hash. The accepted baseline JSON and native payloads were unchanged. Restore only that literal from the verified prior commit, retain the assertion, and add a source regression matching the verifier's pin to the unchanged baseline record. No candidate from that failed run was installed or delivered. The new regression was observed failing before restoring the literal.

## v240 follow-up: native album heart layout discovered during physical regression

The signed v239 Lyrics Queue was physically tested: the actual entry is centred below Play/Pause; Flow hides it; an actual upcoming album row changed the native track while preserving queue order; Back returned to the same Lyrics Activity. A still screenshot showed the preserved gaps and readable button. The same testing exposed structured native heart-read failures, not only an HA receipt failure.

One bounded read-only offscreen inspection identified the cause: native album mode has ONE favourite at the far left, followed by shuffle, previous, play/pause, next and repeat in the middle. It has NO native dislike control. The prior Flow-oriented rule required two left hearts and therefore correctly refused the album layout instead of accidentally clicking shuffle. This is a pre-existing album-mode limitation exposed by the new listening workflow, not a reason to disable stale-state or geometry checks.

v240 extends the version-pinned native rules for recognised album_partner/playlist_partner and the complete observed six-control geometry. Favourite maps to the single left heart. Dislike in that finite layout remains unavailable, never substituted with shuffle, unfavourite or Next. Flow's accepted two-heart mapping is unchanged. The native context type and ID are now checked alongside session/track identity before dispatch so a same-track mode switch cannot reuse the wrong control shape. Glyph classifier and BOOP accent colours are unchanged.

The new15-assertion finite-layout test failed first on the real single-heart album geometry, then passed after the targeted fix; it includes missing/shifted/duplicate controls, mode mismatches, Flow regression and rejecting nonexistent album dislike. A source contract covers the new context guard. The diagnostic disconnected and removed its temporary JAR and one screenshot from the Shield; raw geometry/tree/image evidence remains private. No favourite mutation was sent by this inspection. Shield version advances to240 because239 was installed; companion Wall stays208 because no208 phone install occurred. Final signed build, new APK verification and album-heart physical retest are pending this source commit.
