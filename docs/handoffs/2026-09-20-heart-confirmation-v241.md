# Shield v241: confirmed hearts, clean Lyrics heading, Pixel 7 installation

## User request and starting point

Ryan reports that v240 favourite clicks can really save the track in Deezer while BOOP says it could not confirm and leaves the heart unfilled, from Lyrics and HOME. He accepts the Lyrics Queue placement and large list scale, and asks only to remove the decorative NOW PLAYING heading above the artwork. Preserve all accepted spacing, the right-hand lyrics, list sizing, Flow exclusion and controls. He explicitly requests the existing Wall update on Pixel 7 Pro.

Started from live 11fa06f952306f45be6853eda2716b1638cbbf2c on boop-shield-weather-focus-v221, in the existing clean task-owned .worktrees/boop-deezer-invisible-v235. Main was fetched; retired shared instruction files are absent there and were not restored. The unrelated primary checkout was preserved.

## Phone completed separately

Pixel 7 Pro was paired using the user-provided ephemeral code and discovered pairing endpoint, then model Pixel 7 Pro/device cheetah and installed Wall207 were read back. The exact final Wall208 built with Shield240 was SHA256/signature verified, installed with adb install -r and version208 / 1.2.208-wall verified. APK SHA2566f5bdbe2897342e1891fb60714b07c445f73d4169cba905b3967081a2cd847aa,160567545 bytes, permanent BOOP signer unchanged. Existing app data retained. The daily Pixel10 was not connected or changed. No phone screenshot, new permissions or voice/model changes. The pairing code and network identifiers are not retained here. Physical voice-command retest is still Ryan's next check, not claimed by installing the update.

## Confirmation correction

Source inspection found that the native helper sampled the heart exactly once after a fixed1200ms sleep, then discarded uncertain state. A deterministic delayed-state fixture reproduces why an eventual real save after that sample cannot be reported correctly by that strategy. The precise timing of Ryan's original press was not captured, so this is a reproduced source-level failure mechanism rather than an asserted measured network delay on his press.

The helper now waits on incoming frame notifications for a bounded post-click state transition, examining only the small in-memory glyph, not repeatedly dumping the UI tree. It rejects pre-click/out-of-order/late samples, supports cancellation, and still verifies the native track/context and current node before returning confirmed state. Frame age uses local arrival time instead of assuming the producer timestamp shares the system clock. The actual favourite action is dispatched once only.

If the result remains uncertain after a dispatched toggle, the backend performs at most ONE read-only reconciliation with a separate nonce on the same hardware/track. It never replays a toggle. A matching observed target confirms the original request; the opposite observed state refreshes state as STALE_STATE rather than inventing success. Missing/invalid receipts remain unknown. This also covers an already-hidden control after a real change. Existing app30s and helper12s deadlines, hardware binding and no-optimistic-fill rules remain. Existing Flow/dislike semantics and native geometry/classifier are untouched.

## Presentation

Only the decorative eyebrow label is removed. Existing art/title/artist/progress/buttons/Queue coordinates,38px music-column translation, right-hand lyrics, marquee and focus styling are unchanged.

## Verification before build

All three new tests failed for the expected missing behavior before implementation.32 focused test functions pass with11 confirmation and15 readback checks plus the existing regression suites; the actual Lyrics Activity boundary has47 assertions. Tests cover slow save, removal, stale/invalid samples, unknown state, deadline/cancellation, single readback and no mutation replay. Source helper compiles/dexes. Review is self-review, not an independent reviewer. Signed CI and final Shield241 runtime validation are pending at source publication; do not claim completed installation or full physical acceptance yet. No recorder or emulator was started. Pixel7 already has the specified208 from240, not the subsequently regenerated compatibility-only Wall artifact.
