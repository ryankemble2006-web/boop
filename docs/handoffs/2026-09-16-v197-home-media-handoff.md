# BOOP v197 Home media handoff — 2026-09-16

Owner branch: `boop-hand-colour-v191`

## Goal
Keep the large idle Home BOOP above Shield settings only while Now Playing is absent. When an eligible Now Playing panel appears, the large Home BOOP must yield completely to the smaller Now Playing BOOP. When media disappears, the large Home BOOP returns through the same ownership path.

## Root cause
`ShieldHomeView` already hid the Home assistant for eligible Now Playing state, but the inner `GLSurfaceView` used `setZOrderOnTop(true)` and was not explicitly hidden/paused when the parent puppet lost ownership. This could leave a large GL face visible as a ghost behind the Now Playing puppet.

## Fix
`ShieldNowPlayingPuppetView` now explicitly deactivates the inner GL surface when its puppet is hidden and resumes it when ownership returns. `LayeredPuppetView.setSurfaceActive()` controls surface visibility plus guarded `onPause()` / `onResume()` lifecycle calls.

No Deezer- or Chromecast-specific branch was added. Close media, natural Now Playing expiry, native Deezer force-close and Cast-session disappearance all continue to feed the existing Now Playing eligibility/ownership path.

## TDD / CI
- RED: `c63e66bcae8f837de8819cc69dda3911ddb9f46b`, run `35067647431`, 12 passed / 1 failed on the missing GL release contract.
- Fix: `8d7fdf777ab61309d5dda9429620de1321dae8f6`.
- Fix-only GREEN: run `35067925001`, full workflow success.
- Final v197 build: `c9041dee2da5c71ef03b8d146dcd9f60309e5c78`, run `35068273731`, full workflow success.
- Artifact: `BOOP-Unified-v197-Home-Media-Handoff`, artifact id `10434463728`.
- APK SHA-256: `ea07abfa0b573cfd0d1187a3af73e6465d2ccd4c141f7f1374903f833c5233a2`.

## Device verification
Installed in-place on Shield only: version `197`, name `1.2.197-home-media-handoff`. Accessibility-service configuration stayed unchanged. Pulled installed APK hash matched the signed artifact exactly.

Live ADB screenshots verified two states:
1. Deezer actively playing on BOOP Home: only the small Now Playing BOOP is visible; the large Home BOOP is absent.
2. Native Deezer force-stopped: the Now Playing session/card disappears and the large Home BOOP returns above Shield settings.

Ryan retains final visual acceptance. Close-media, natural expiry and Chromecast disappearance share the same state path but were not individually claimed as live-tested in this receipt.