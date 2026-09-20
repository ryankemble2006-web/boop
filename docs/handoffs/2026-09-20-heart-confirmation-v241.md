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

## Final build and device record

## Current: Shield v241 installed; Pixel 7 Wall v208 installed; heart retest pending

Source/build45c49833cf1be594645b030029ddea29beaee88b on boop-shield-weather-focus-v221. Shield241 is installed with data preserved and its package version read back. The decorative NOW PLAYING heading in Lyrics is removed; all accepted artwork/title/artist/progress/transport/Queue coordinates and38px column translation are unchanged. Ryan already accepted the prior Queue placement and list scale. No acceptance of the new heart correction is inferred from that UI feedback.

The old fixed1200ms favourite sample is replaced by a bounded wait on fresh frame arrivals. An uncertain applied toggle gets at most ONE read-only reconciliation, never a second mutation. Actual observed state, not requested state, determines fill and confirmation. Track/context/hardware/nonce guards remain.32 focused test functions and the full signed build passed;11 new frame-confirmation and15 readback assertions cover slow success, unfavourite, stale/unknown/late data and no replay. Source helper compilation/dex passed. Review was self-review only.

Successful signed run35539554369/job106154632096. Artifact10614292196 retained the OLD outer label BOOP-Shield-v240-Wall-v208-Signed, but its manifest/receipt/file correctly contain BOOP-Shield-v241.apk. ZIP SHA256dc1b14f1ccf51d5f57d1c218138d7e67e9d3f5efa6731a80fe8e75a5cdbb44b7. Shield APK160567657 bytes, SHA2563480e5378290db5c32be7322323523cda642063753f462f360c5b37e5d12479e. The final metadata-only cleanup corrects the workflow display/artifact label for the next run without rebuilding/re-signing or changing this verified app source. Permanent signer is unchanged;16 native libraries and18 assets match240. Source/CRC/hash/size and full APK signature were independently verified. Copy: Desktop/APKBOOP/BOOP-Shield-v241.apk;240 rollback retained.

Pixel7 Pro was explicitly paired and identified before updating207 to the final Wall208 from the v240 build, SHA2566f5bdbe2897342e1891fb60714b07c445f73d4169cba905b3967081a2cd847aa. Installation/signature/version208 checks passed and data was retained. This is the original requested phone fix, not the regenerated compatibility-only208 from the241 workflow. That newer companion was NOT installed or delivered over the phone copy. Pixel10 was not connected, inspected or changed. Phone voice-command retest is still pending.

Runtime limits: after241 installation, first read logged hardware-binding IOException and a later native heart-read reported unavailable. Two read-only BOOP-heart inspections did not find a confirmed ready state; no favourite toggle was sent by this continuation's diagnostic tool. A still image captured native album browsing rather than Lyrics, so it is NOT visual acceptance of the new heading or confirmation behavior. Inspection stopped instead of competing with navigation. The original applied-but-unconfirmed album symptom is addressed in code and regression tests, but successful physical retest on241 is not claimed. Do not mark the heart fault resolved on hardware until Ryan confirms it or a bounded isolated test does. No recorder or emulator started, no extra track skip, and no voice/model/permission changes. Diagnostic helper disconnected and its owned on-device JAR was removed.

Detailed chronology: docs/handoffs/2026-09-20-heart-confirmation-v241.md. Earlier240 native finite-layout fix and208 voice-receipt correction remain below as history.
