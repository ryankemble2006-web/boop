# BOOP unified handoff — 2026-09-07

Owner: `boop-unified`.
Package: `com.boop.alpha1`.
Candidate: versionCode 41 / `1.0.0-unified-alpha1`.

## Purpose

This is the first one-APK BOOP lineage. Ryan explicitly chose one canonical APK/branch after the current puppet work because separate Wall, Launcher and Shield branches/build filenames had become too confusing. Future release discipline is one intentional functional change per update, with rollback by the exact last physically accepted Git commit/tag + artifact, never by guessing filenames.

## Exact source inputs

The unified candidate was built from live GitHub heads fetched immediately before integration:

- Wall: `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3` — v40 landscape eye-match lineage. Ryan physically reported the corrected landscape face was much better and that BOOP now reads as the same character turning around rather than changing character.
- Launcher: `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2` — 0.3.7 widget-picker candidate; latest physical report was that the picker was better, with later polish deferred.
- Shield puppet: `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7` — v3 friendly Deezer-access/full-screen puppet lineage; its branch-level CI was green, physical acceptance still lineage-specific.

Pinned source receipt: `unified/SOURCE_HEADS.md`.

## Architecture

One final application keeps package `com.boop.alpha1` and the existing permanent BOOP signer. Wall remains the application core. Latest Launcher and Shield sources are included as internal Android library modules so their existing package namespaces/resources can remain largely intact while producing one APK.

`UnifiedEntryActivity` is the single exported launcher/HOME/Leanback entry and chooses a device profile automatically:

- Android TV / Leanback / television UI mode -> Shield mode.
- Pixel 7 Pro -> Wall mode.
- Other handheld Android devices, including Pixel 10 Pro XL -> Launcher mode.
- A persistent internal override exists for recovery/debugging, but normal operation is automatic.

Wall-to-Launcher and Launcher-to-Wall are now internal activity hops inside the same APK rather than cross-package launches. The unified materializer also reproduces Shield's own CI behavior of supplying the approved BOOP eye bitmap before Shield build/tests.

## Verification

Green build head: `bb4797de5005952d0d27a6647ea17c15781b76f7`.
GitHub Actions run: `34104002238`.
Artifact: `BOOP-Unified`.
Artifact ID: `10011710184`.
APK SHA-256: `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

The successful run passed:
- pinned live-source receipt/unified-shell checks;
- preserved Wall source guards;
- unified materialization;
- latest Launcher unit tests + Android lint;
- latest Shield unit tests after reproducing its approved-eye materialization step;
- unified app Android unit tests including device-profile routing;
- permanent BOOP signer setup;
- signed unified APK build;
- package/version/launchable-entry checks;
- manifest checks for Wall, Launcher and Shield activities/services;
- signer continuity, APK archive integrity and artifact upload.

## Physical status and migration limits

CI/signer green is not physical acceptance. This first unified APK must still be installed and judged on real devices.

Because the unified package intentionally keeps `com.boop.alpha1`, a current Wall install has the cleanest in-place update path. Existing standalone Launcher (`com.boop.launcher`) and Shield (`com.boop.shieldoverlay`) installs are different Android packages, so their private app data/default-HOME/special-access grants cannot automatically become data or permissions of `com.boop.alpha1`. Expect one-time setup/reselection when the unified APK is first tested on those bodies. Do not delete the historical branches or accepted artifacts; they are rollback/reference until unified BOOP is physically accepted.

## Release rule

After unified BOOP is physically accepted, future work starts from its exact accepted commit/tag and artifact. Make one intentional behavior change per version. If a candidate breaks, return to the exact last accepted checkpoint instead of trying to identify an old local APK by filename. GitHub remains the archive; deployment folders should retain only the current signed `BOOP.apk` and optionally one last-good APK after acceptance.
