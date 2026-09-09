# BOOP v69 dev menu receipt

Date: 2026-09-09

Canonical branch: `boop-unified`
Package: `com.boop.alpha1`
Built code head: `709c74eb39d28c0d894661e5bde66da18f9ea6cf`
Version: 69 / `1.2.23-unified-dev-menu`

## What changed

v69 adds one internal BOOP developer/demo screen, reachable from the existing Voice settings surface. The screen is not exported outside the app.

The **Animations** shelf exposes the existing BOOP face behaviors without creating replacement animation code:

- Wake
- Think
- Berry
- Shake
- Sleep

The **Notification demos** shelf uses the production `BoopNotificationPuppetView` with local-only presentation fixtures:

- Unlocked
- Locked
- Bundle

Locked demo content is passed through the same privacy-redaction model as a real locked notification presentation. Demo actions manufacture local `BoopNotificationPresentation` objects only. They do not call Android `NotificationManager`, the notification-listener service, or `BoopNotificationRuntime`, and they do not create an Android shade notification.

v69 carries the v68 procedural hue-wire fix and the finished v65 procedural-eye/sclera stack forward unchanged. v68 itself had not yet been physically accepted when v69 was built.

## Test-first evidence

Three deliberate RED stages preceded the final implementation:

- `3bb0aadb8bc15df283be93202853f273a432d467` / workflow `34316174381`: failed because `BoopDevMenuModel` did not exist.
- `b32a328f09a7f5d4c347e48986b685da18afd072` / workflow `34316614087`: failed because `BoopDevNotificationPreview` did not exist.
- `f571345b1ac68980d7877dc028e597d884d74812` / workflow `34317008388`: failed because `.BoopDevMenuActivity` had not yet been wired into the materialized manifest.

The final implementation commit `709c74eb39d28c0d894661e5bde66da18f9ea6cf` then passed the complete canonical build.

## GitHub verification

Main Unified workflow: `34317400589` SUCCESS.
Separate Shield HOME routing workflow: `34317400631` SUCCESS.

Final workflow passed:

- non-visual integration contracts;
- canonical materialization;
- internal-only dev-menu/local-only notification-demo plumbing contract;
- notification presenter/manifest contracts;
- seamless wake-command handoff;
- preserved Launcher lint/checks;
- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 140/140, zero failures/errors/skips;
- permanent signer preparation;
- signed APK assembly;
- package/version/launchable-entry/permanent-signer/APK ZIP integrity;
- artifact upload.

No screenshot, golden-image, visual-layout or animation-appearance acceptance was performed by GitHub.

## Signed artifact

- workflow artifact: `BOOP-Unified`
- artifact ID: `10090644503`
- artifact size: `62,728,418` bytes
- artifact ZIP digest: `sha256:af14acccfa0ae730a1254f518f2210a46645d45fdcd6d2056f9aa4fb4b9449a9`
- APK SHA-256: `31da93c3fdfd7116b8bc9b083fd947dadc5952a77c5c67c5d3808b99f0c57f88`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

The downloaded GitHub artifact was independently extracted and the APK was re-hashed; the result matched the CI APK receipt exactly.

## Physical acceptance boundary

v69 is CI/signer green only. It is not a physical rollback checkpoint yet.

Real-device checks for v69:

1. Open Voice settings and confirm `Dev menu` opens the internal BOOP Dev screen.
2. Exercise Wake, Think, Berry, Shake and Sleep and judge the actual motion on-device.
3. Exercise Unlocked, Locked and Bundle notification demos and confirm they use the BOOP notification puppet presentation.
4. Confirm Locked demo reveals no title/body text before authentication semantics.
5. Confirm notification demos do not create a new Android shade notification.
6. Recheck the carried v68 hue fix: move the hue control across obvious colours and confirm only the procedural iris changes while sclera, pupils, catchlights and black lids remain unchanged.
7. Confirm the finished v65 sclera/white blend remains correct.

Do not create or repoint a v69 rollback checkpoint until Ryan explicitly accepts this exact signed APK on real hardware.
