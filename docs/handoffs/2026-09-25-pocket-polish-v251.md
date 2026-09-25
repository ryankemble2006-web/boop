# Pocket polish v251 / v218 / sender2

## Scope and boundaries

Owning branch `boop-pocket-polish-v251`, based on live notification branch `2a983a1752d6ddb9d5e9b92d5ddafbb21433180c`. Ryan requested autonomous Yoga emulator work while watching TV. Physical Pixel10, Shield, HA and AV state are untouched. Existing preferences, approved artwork and native libraries must remain intact.

## Changes

- Voice settings and developer controls grow for enlarged text; the phone developer and receiver screens scroll in short layouts. Animation controls preserve their full labels.
- Optional Home rows scroll into view for DPAD focus without painting over the navigation bar. Focused card scaling is included in the reveal area.
- Both notification labs use the selected Home accent with dark fill and white text. The separate sender reads one colour through a signature-protected, read-only provider; no home data or settings are exposed.
- Music accepts conversational wrappers, listen/hear forms, either title/artist order, possessives and polite suffixes. Literal catalogue names win before politeness stripping. Ambiguous/short artist names still require confirmation; no alternative recording is substituted. Common HA device phrases fall through. Arbitrary custom device names remain a limit of lexical classification.
- Portable notifications have an opt-in all-apps mode including future categories, with persistent app/category exclusions. Existing selected-mode settings and consent survive. Settings expose the required Android grants.
- Runtime skips group summaries and only BOOP's own Android overlay-status notification, avoids repeat interruptions for ONLY_ALERT_ONCE updates, removes surfaces on access revocation/removal, and keeps unlock requests alive past presentation timeout. Dispatching a PendingIntent does not remove the original Android notification because dispatch alone cannot prove Android opened the target.
- Generic signs identify the sending app; the four established sign styles retain exact real/dev mappings (including Gmail and X). Existing sign art/geometry are unchanged.

## Verification ledger

- Production music probe: 419 checks, zero failures; neighbouring receipt/race and command tests pass. Metadata-only catalogue checks used no physical playback.
- Focused runtime, all-app state/codec, lab focus/provider and sign identity regression probes pass. Real Android36 compilation and local debug builds pass.
- Maintained first-stage suite: 173 passed plus one stale release-number assertion; updated the assertion to251/218 and reran weather/sign tests (9 passed).
- Broad suite originally: 491 passed, 51 failed,2 skipped,18 subtests. Independent base comparison reproduced49 failures at unchanged2a983a1. The two differences (provider ordering and Windows split digest separator) are fixed and their tests pass. Historical/environment failures are not reported as green.
- Real phone-emulator notification fixture confirmed unlocked popup and target ID71 on tap; ONLY_ALERT_ONCE does not reappear after expiry; summaries are ignored while children display; revoking listener permission removes the visible overlay immediately. Locked preview hid message text; delayed authentication beyond8s still dispatched to the source app. A temporary emulator PIN was cleared after that test.
- Phone receiver checked at1280x720/density240/font1.5: all text and Close control visible, selected border matches default Home cyan.
- Build6 TV fixture passed optional-row visibility, scaled borders, DPAD selection/return and navigation-bar pixels. Independent review then found tall-layout room-panel coexistence needed additional coverage; follow-up removed viewport stretching and repaired traversal through optional rows. Build8 passed ordinary/tall/no-optional scenarios, including room-device action callback and returning focus. An independent review found no remaining issue.
- Sender2 border pixels match actual Home accent: cyan#4db8ff and custom magenta#ff4dff; default restored. Signature permission granted to sender; untrusted shell provider query denied.
- Authenticated sender-to-phone emulator Run all displayed all12 production scenarios and finished once. Stop acknowledged; after phone background, its old socket returned no OK response. Manual emulator bridge was used, so physical LAN/mDNS discovery remains unverified.
- Notifications now separate art and details on landscape/short screens; portrait reserves the animated sign envelope. Long text scrolls, taps open the source, and horizontal card swipes cancel the child tap before dismissing. Final visual retest follows the top-anchored scroll-child correction.
- 48 final focused notification/Home tests passed; first signed CI run36188788473 passed at20d9d789. Final source/signature/artifact receipts are pending below.

## Remaining physical acceptance

These are emulator and source results. Pixel10 notification listener/overlay grants require the normal Android consent screens. Actual OEM background/wake behaviour, microphone recognition and acoustic music selection need Ryan's later device check. Android keeps original notifications; silence chosen native categories to avoid duplicate sounds. Real-device installation is not part of the TV-safe emulator pass.
