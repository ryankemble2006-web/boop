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

Wall-to-Launcher and Launcher-to-Wall are now internal activity hops inside the same APK rather than separate-package launches. The unified materializer also reproduces Shield's own CI behavior of supplying the approved BOOP eye bitmap before Shield build/tests.

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

## Official yellow hands design lock, 2026-09-07

Ryan approved the side-by-side yellow hands as the official pair for BOOP everywhere. This applies to all three unified bodies and the separate animation lab. Read `BOOP_YELLOW_HANDS.md`; use `unified/assets/boop-yellow-hands/` for the canonical app reference. Preserve the exact plush yellow style, five digits per hand, short cuffs, independent floating hands and real transparency. New poses articulate the same hands, never transform them into creatures. The approved eyes/headphones stay unchanged.

Starting remote heads checked for this documentation task: `boop-unified@8cfb14e001b946f0bdb33011c8cbe3778ed59019`, `animation-freddie-mercury@3548e6aeb787556b1994cbafb3bffb4c6c017d05`, `main@4323bb747f1ddaced24a5372854377acccaa4228`.

Exact master SHA-256: `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`; 1774 x 887 RGBA PNG, 1541931 bytes. Local inspection verified alpha transparency. PNG transfer is still pending: this commit publishes design notes and a checksum manifest, not the PNG binary. `boop-official-yellow-hands.zip` supplies the unchanged master in both branch destination layouts for manual upload. No editable generator layers were supplied.

No app code, runtime resource usage, build configuration, version, signing, permissions or deployment changed; no new APK or physical acceptance is claimed. The laptop checkout/receipt is not mounted in this chat environment and the local Git transport could not resolve GitHub, so no laptop synchronization is claimed. Next safe step: upload the master to the documented folder on each branch, verify its checksum, then update the transfer record. Animation/runtime integration requires a separate request.

## Shield 2.5D direction and pose-study archive, 2026-09-07

Ryan requested saving the hand-pose work and the idea of deliberately using the Shield GPU for 2.5D puppetry. Read `docs/animation/SHIELD_2_5D_HANDS.md`. Keep independent hand/finger acting, earcup grips/adjustments, gaze-led motion, headphone lag/recoil and restrained layered depth as future design work. Every hand must retain four fingers plus one thumb in every pose and transition. Preserve the official master and existing eyes/headphones; no four-digit shortcut.

Starting live heads for this save: unified `0eedc515edda55ec9a8c6c5584f27b985e0b66e2`, art lab `193b7b7c798cdf5dfec44cc32a152e1461b52f1c`, main `4a7036a6a4066d19a3a7c5c5af3862c242c8a426`. The laptop path and private receipt were checked but are not mounted here; Git transport cannot resolve GitHub. Publication uses the connected GitHub tools and makes no laptop-sync claim.

Archived image: `unified/assets/boop-yellow-hands/pose-studies/five-digit-open-pose-preview.webp`, a labelled 448 x 448 lossy preview of the corrected open-hand study. Uploaded blob identity matches local `854fd36b870f1c6e647c5fdce853003cd05e5e2f`; SHA-256 and original-source hashes are in its adjacent manifest. The exact approved master was reverified from the ZIP without changing its bytes. Full-resolution master/source PNG upload remains pending. Other illustrated pose sources were not available as separate files and are not falsely listed as archived.

Verification scope: image byte counts, dimensions, hashes, preview blob identity and documentation/evidence guards. No rig, Shield benchmark, new APK or device acceptance. Existing app candidate/rollback evidence is unchanged. No app code, live resources, workflows, signing, permissions or deployment changed. Next safe step is exact-PNG transfer and checksum verification; later implementation must profile the real device, protect media playback and preserve lifecycle/reduced-motion safeguards.
