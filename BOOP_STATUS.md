# BOOP unified status

Updated 2026-09-07. Owning branch `boop-unified`.

## Current candidate

- One APK: package `com.boop.alpha1`.
- VersionCode 41 / `1.0.0-unified-alpha1`.
- Permanent BOOP signer preserved.
- Wall, Launcher and Shield are now compiled into one application from their exact latest pinned GitHub heads listed in `unified/SOURCE_HEADS.md`.
- Automatic routing: Android TV/Leanback -> Shield; Pixel 7 Pro -> Wall; other handheld Android -> Launcher.
- Wall/Launcher transitions are internal to the one APK.

## Build evidence

Green build head: `bb4797de5005952d0d27a6647ea17c15781b76f7`.
Workflow run: `34104002238`.
Artifact: `BOOP-Unified` / ID `10011710184`.
APK SHA-256: `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

Passed: unified source-head contract checks, preserved Wall guards, Launcher tests/lint, Shield tests with the approved eye materialization used by its own CI, unified Android unit tests, signed build, exact package/version/entry checks, manifest presence for all three bodies, signer continuity and archive integrity.

## Physical status

- Wall input lineage includes the physically approved v38 eye-colour UX and Ryan's positive physical v40 landscape-eye report.
- Launcher input is the latest 0.3.7 source; widget picker was physically reported better but later polish was deferred.
- Shield input is the latest v3 friendly Deezer-access/full-screen puppet source; unified Shield behavior is not physically accepted yet.
- Unified v41 itself is CI/signer green only until it is installed and tested on real Wall, handheld and Shield bodies.

## Migration note

The unified package keeps Wall's `com.boop.alpha1` identity. Existing separate Launcher and Shield packages keep their own private Android state and grants; moving to unified BOOP may require one-time HOME selection, Shield special-access setup and other device-local setup. This is expected migration work, not evidence that the unified APK failed to compile.

## Release discipline

Once v41 or a successor is physically accepted, mark that exact Git commit/tag + artifact as the last-good checkpoint. Future versions make one intentional functional change at a time. Roll back by exact checkpoint, not by local filenames. GitHub is the archive; deployment folders keep current `BOOP.apk` plus optionally one last-good APK after acceptance.

## Official yellow hands, 2026-09-07

Design approved and locked across all BOOP bodies and animations. See `BOOP_YELLOW_HANDS.md` and `unified/assets/boop-yellow-hands/manifest.json`. The approved master is a hands-only transparent RGBA PNG, 1774 x 887, with five digits per hand and no arms. The master checksum is recorded; its binary transfer to GitHub is still pending manual upload from the supplied ZIP. No runtime integration, new build or deployment has been performed. Existing candidate and verification status above are unchanged.
