# BOOP Shield Full-screen Deezer Memory

Updated 2026-09-07.

Current experiment branch: `boop-shield-fullscreen-deezer-wip`.
Base lineage: `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
Current candidate: versionCode 2 / `0.2-fullscreen-puppetry`.
Build commit: `0d9f5e6f2cc3249541667976a41405efd686a52a`.
Green workflow run: `34098619403`.
Artifact ID: `10009620031`.
APK SHA-256: `ea9fa94f0868943cf559b5b8e1406dcf28e07264a415aed7d58e7cc61c292a18`.
Package/signing remain `com.boop.shieldoverlay` with the permanent BOOP development signer.

## Product decision

For Deezer music, BOOP is being tested as a full-screen puppet rather than a corner overlay. Deezer remains the underlying player and source of media-session state. BOOP owns the picture with a pure-black canvas and large centred H1 puppet.

Ryan liked the usefulness of the large debug/diagnostic presentation and asked that it be kept handy for later. Do not delete useful diagnostics during puppet-polish work merely to make the screen cleaner.

## Current puppetry contract

- PLAYING uses a richer 3.6-second periodic `FullscreenPuppetMotion.groove`: gentle primary sway/nod plus small secondary listening/head-lag movement. It is deterministic and bounded, not audio/beat analysis.
- PAUSED captures the current groove pose and settles to neutral over 520 ms.
- Resume continues the accumulated `MediaPuppetFrameLoop` clock instead of restarting the groove at phase zero.
- Explicit Deezer skip states 9/10/11 trigger a 700 ms perk-up/lift/tilt accent, peaking at roughly 180 ms, then return to neutral until the next confirmed state arrives.
- Other REST states stay neutral. Non-skip REST-to-REST updates remain equivalent/quiet as before.
- Acting geometry tests cover groove, settle and skip accent using the measured H1 alpha bounds on common TV modes.

## Guardrails

- Preserve the existing Deezer media-session observer/controller ownership and media clock. No polling, metadata storage or audio analysis was added.
- Preserve non-focusable/non-touchable application-overlay flags so remote/media input passes through.
- Do not change HA auth/socket, Home, Routines, voice, pairing or stable signing.
- No microphone, accessibility service, UsageStats permission or other foreground-tracking permission was added.
- The WIP intentionally becomes full-screen for any eligible Deezer headphone state, including background playback. Exact Deezer-foreground gating is a later decision only if physical testing shows it is needed.
- BOOP Home's existing hide/show path remains the escape from the full-screen puppet.
- Ordinary fallback `EYES` state returns to the existing compact transparent overlay geometry.
- The source `boop-shield-media-puppetry` branch and its physically accepted H1 placement remain untouched.

## Verification evidence

TDD RED run `34097940701` failed at Shield unit-test compilation because the motion test referenced the intentionally absent `FullscreenPuppetMotion` class.

GREEN run `34098619403` passed the full Python source regression suite, complete Shield unit suite, fullscreen motion/state/geometry tests, stable-signed APK build, package/permission inspection and signer continuity for build commit `0d9f5e6f2cc3249541667976a41405efd686a52a`.

This is not a physical checkpoint. Ryan must install/test v2 on the Shield and report the acting quality before promotion.
