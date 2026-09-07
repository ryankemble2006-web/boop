# BOOP Shield Full-screen Deezer Memory

Updated 2026-09-07.

Current experiment branch: `boop-shield-fullscreen-deezer-wip`.
Base lineage: `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
Build commit: `cd56e3bd0bf4ff6547e8cd2ea45631cde1418c36`.
Green workflow run: `34096866418`.
Artifact ID: `10008967780`.
APK SHA-256: `bb225e7c7f9fbfed13d758155b42208482921c4ef815d509e78dc5bb5edf5b4e`.
Package/signing remain `com.boop.shieldoverlay` with the permanent BOOP development signer.

## Product decision

For Deezer music, test BOOP as a full-screen puppet rather than a corner overlay. Deezer remains the underlying player and source of media-session state. BOOP owns the picture with a pure-black canvas and large centred H1 puppet while existing playback state continues to drive rest/play motion.

## Guardrails

- Preserve the existing Deezer media-session observer/state holder/clock. No new polling or audio analysis.
- Preserve non-focusable/non-touchable application-overlay flags so remote/media input passes through.
- Do not change HA auth/socket, Home, Routines, voice, pairing or stable signing.
- No microphone, accessibility service, UsageStats permission or other foreground-tracking permission was added.
- The current first WIP intentionally becomes full-screen for any eligible Deezer headphone state, including background playback. Exact Deezer-foreground gating is a later decision only if physical testing shows it is needed.
- BOOP Home's existing hide/show path remains the escape from the full-screen puppet.
- Ordinary fallback `EYES` state returns to the existing compact transparent overlay geometry.
- The source `boop-shield-media-puppetry` branch and its physically accepted H1 placement remain untouched.

## Verification boundary

CI passed the full Python source regression suite, complete Shield unit suite, full-screen geometry/envelope tests, stable-signed APK build, package/permission inspection and signer continuity. This is not a physical checkpoint. Ryan must install/test on the Shield and report behaviour before promotion.
