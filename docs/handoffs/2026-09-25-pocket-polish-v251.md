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
- Maintained first-stage suite passed in signed CI after deliberately updating the release-number fixture to251/218; local weather/sign retest also passed (9 tests).
- Broad suite originally: 491 passed, 51 failed,2 skipped,18 subtests. Independent base comparison reproduced49 failures at unchanged2a983a1. The two differences (provider ordering and Windows split digest separator) are fixed and their tests pass. Historical/environment failures are not reported as green.
- Real phone-emulator notification fixture confirmed unlocked popup and target ID71 on tap; ONLY_ALERT_ONCE does not reappear after expiry; summaries are ignored while children display; revoking listener permission removes the visible overlay immediately. Locked preview hid message text; delayed authentication beyond8s still dispatched to the source app. A temporary emulator PIN was cleared after that test.
- Phone receiver checked at1280x720/density240/font1.5: all text and Close control visible, selected border matches default Home cyan.
- Build6 TV fixture passed optional-row visibility, scaled borders, DPAD selection/return and navigation-bar pixels. Independent review then found tall-layout room-panel coexistence needed additional coverage; follow-up removed viewport stretching and repaired traversal through optional rows. Build8 passed ordinary/tall/no-optional scenarios, including room-device action callback and returning focus. An independent review found no remaining issue.
- Sender2 border pixels match actual Home accent: cyan#4db8ff and custom magenta#ff4dff; default restored. Signature permission granted to sender; untrusted shell provider query denied.
- Authenticated sender-to-phone emulator Run all displayed all12 production scenarios and finished once. Stop acknowledged; after phone background, its old socket returned no OK response. Manual emulator bridge was used, so physical LAN/mDNS discovery remains unverified.
- Notifications now separate art and details on landscape/short screens; portrait reserves the animated sign envelope. Long text scrolls, taps open the source, and horizontal card swipes cancel the child tap before dismissing. Final visual retest passed: normal portrait, enlarged-text long portrait (heading and end marker reachable), landscape and short-screen fallback. Horizontal dismissal and vertical scrolling remain distinct; repeat source taps reached fixture ID102 with an existing fixture task after its task flags were corrected.
- 48 final focused notification/Home tests passed. Final maintained functional/integration/HA tests, permanent signing, native/frozen-art verification and full Android build passed in CI run36190488635 at2460ab75.

## Signed delivery

Final production/test source: `2460ab75ca880c812b95730322a5b9ad9bfc2fa1`. [Successful signed CI](https://github.com/ryankemble2006-web/boop/actions/runs/36190488635); [PR11](https://github.com/ryankemble2006-web/boop/pull/11) targets the owning notification branch, not main. Later documentation-only commits do not change the built source. Shield251, Wall218 and Test Sender2 were downloaded and independently verified before installation on Yoga emulators5554/5556.

| APK | SHA256 |
| --- | --- |
| BOOP-Shield-v251.apk | 916ad43ec539b5b8cd2b2d3c15f0fa9ee92bd0ac91379d3ba4bd608554191947 |
| BOOP-Wall-v218.apk | 359a0eb752ab357b84530295f94e3318e57cad5cd3bcea1b635fed3e0c5d35bb |
| BOOP-Test-Sender-v2.apk | 609a9bd8222c927fa1082ec066a63cc1a680cadafd91b606f8a638ec2fb7e43e |

All three use permanent certificate SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Installed APK hashes match the downloaded receipts. Each production app retains all16 accepted native libraries exactly; all29 assets are source-equivalent (eight text assets differ only in Windows versus Linux newlines). Sender has no copied art or native libraries.

Final exact-signed runtime smoke passed: expanded Home fixture ordinary/tall/no-optional scenarios; sender default border RGB77,184,255; signature permission granted to sender and provider query denied to untrusted shell; real phone Android notification popup and tap to fixture111; authenticated sender-to-phone Facebook preview and Stop acknowledgment. Earlier complete12-preview, custom Home colour, lock/privacy and large-text checks used local emulator builds during development; the exact signed smoke checks above confirm the final delivery. Physical LAN discovery and device acceptance remain separate.

Delivery folder: `C:/Users/ryank/Documents/Codex/2026-09-25/fan/outputs/BOOP-pocket-polish`. It contains all three APKs, CI/independent receipts, the baseline-failure audit, emulator acceptance receipt and actual screenshots. No physical app was installed or modified. Future physical updates must preserve data with `adb install -r`.

## Remaining physical acceptance

These are emulator and source results. Pixel10 notification listener/overlay grants require the normal Android consent screens. Actual OEM background/wake behaviour, microphone recognition and acoustic music selection need Ryan's later device check. Android keeps original notifications; silence chosen native categories to avoid duplicate sounds. Real-device installation is not part of the TV-safe emulator pass.
