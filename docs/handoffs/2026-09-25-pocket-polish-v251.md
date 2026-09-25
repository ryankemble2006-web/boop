# Pocket polish v251 / v218 / sender2

## Scope and boundaries

Owning branch `boop-pocket-polish-v251`, based on live notification branch `2a983a1752d6ddb9d5e9b92d5ddafbb21433180c`. Ryan requested autonomous Yoga emulator work while watching TV. The initial emulator pass left physical Pixel10, Shield, HA and AV state untouched; the subsequent authorised installation is recorded below. Existing preferences, approved artwork and native libraries must remain intact.

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

Delivery folder: `C:/Users/ryank/Documents/Codex/2026-09-25/fan/outputs/BOOP-pocket-polish`. It contains all three APKs, CI/independent receipts, the baseline-failure audit, emulator acceptance receipt and actual screenshots. At the end of the emulator pass, no physical app had been installed or modified. The subsequently authorised in-place updates are recorded below.

## Physical installation — 25 September 2026

Ryan then authorised deployment to the real devices. At21:29UTC on25September, Pixel10 was updated217→218, Shield249→251 and its separate Test Sender1→2 using `adb install -r`. All three installed APK SHA256s match the signed receipts. Application UIDs and first-install times are preserved; all saved user settings are preserved. Pixel's active notification listener refreshed only `observed_channels` discovery metadata; no consent, appearance, voice or playback preference changed. The sender's Home-accent signature permission is granted. YouTube remained the Shield's foreground activity in the same task; no app activity was launched and no AV command was sent.

Rollback APKs and private preference snapshots are retained locally under task `work/real-device-deployment/`; the user-facing installation receipt is `outputs/BOOP-pocket-polish/physical-installation.json`. Pixel already has notification-listener access and BOOP's master switch enabled. The new All apps option remains off, preserving the existing selected-app policy. Overlay app-op is still default, with no explicit grant recorded; use Profile → Notifications to finish the Android overlay/all-app setup when wanted. No Android permission or consent setting was changed. Real-device visual/acoustic, all-app popup/background/wake and physical LAN discovery acceptance remain pending.

## Physical notification pairing recovered — 25 September 2026

The real Shield sender initially found a stale `BOOP Phone sdk_gphone64_x86_64` mDNS record. Its Android11 NsdService resolve remained active after SERVICE_LOST, leaving the later Pixel advertisement queued and no selectable phone. A read-only direct LAN PING confirmed the current Pixel server and PIN worked. Force-stopping/reopening only `com.boop.notificationlab` cleared the stale resolver; the Pixel then resolved to its actual LAN endpoint and appeared in the sender. Shield UI reported accepted previews and the physical Pixel `BOOPNotifyLab` log confirmed FACEBOOK, WHATSAPP, GMAIL and X. The user continued Run all. This establishes physical authenticated transport; it does not infer visual/art or acoustic acceptance.

No source/APK change was made for this recovery. Sender2 still lacks a timeout/recovery for an Android resolver that never calls back; force-stop/reopen is the verified recovery. Do not claim that limitation permanently fixed. A future bounded discovery recovery should cover lost services and silent resolver callbacks on older Android.

Important diagnostic boundary: `uiautomator dump` on this Pixel triggers Switch Access's setup wizard after the read, backgrounds the lab and invalidates its session. Stop using that method on Pixel10. Direct `adb shell screencap`, activity-state reads and socket/log checks were verified without that interruption. Switch Access settings were not disabled or changed. Shield UI dumps did not trigger the phone issue.

## Real Gmail notification accepted — 25 September 2026

Ryan initially reported an email alert without BOOP. Live checks found listener access connected, overlay permission allowed, master/all-app mode enabled and no exclusions. The earlier miss was not reproduced or assigned a proven cause. No source/APK, permission or user-setting change was made during this diagnosis.

A harmless local Android notification (outside the preview lab) reached the production listener and opened `BoopNotificationLockActivity` on the dozing Pixel. Ryan then sent a fresh email and left it unread. Android's event log recorded Gmail's group summary at22:54:04.314 and real child at22:54:04.330; BOOP's lock activity started at22:54:04.762 with `BAL_ALLOW_SAW_PERMISSION`, its window became visible, and the phone became awake. The Gmail notification remained present. Ryan explicitly confirmed both “that worked that time” and “Yes, BOOP appeared”. This is real Gmail-to-BOOP physical visual acceptance, separate from the earlier synthetic lab pass.

The metadata-only capture is bounded and ends after120seconds. No email body or sender/recipient address was captured in the saved diagnostic report. No Pixel UIAutomator or accessibility-setting change was used. The temporary shell test notification was subsequently absent from Android's active list. Other app-specific delivery and the unexplained first miss are not claimed fixed by this one successful test.

## Remaining physical acceptance

Runtime scenarios above are emulator/source evidence; the deployment section separately records verified physical installation. Pixel10 already has listener access; overlay/all-app setup still needs completion through the normal settings screens. Actual OEM background/wake behaviour, microphone recognition and acoustic music selection need Ryan's later device check. Android keeps original notifications; silence chosen native categories to avoid duplicate sounds. The later authorised physical installation is complete; it does not by itself establish runtime acceptance.
