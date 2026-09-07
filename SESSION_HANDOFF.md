# BOOP unified handoff — 2026-09-07

Owner: `boop-unified`.
Package: `com.boop.alpha1`.
Current candidate: versionCode 43 / `1.1.0-unified-dock-mirror-shield-settings`.
Built code commit: `950611df0235d3943bf9958153efa470a342036b`.
Last physically accepted unified rollback point: `e746affbb82b577cef2f1cf6e731dff186c8f881`.

## Current v43 handoff

Ryan physically tested the unified APK at `e746aff` and explicitly said it was fine to modify. The dock/mirror + Shield settings/room-scope pass was then implemented as direct descendants of that accepted commit. Concurrent work was preserved: when another BOOP worker advanced the same branch with the requested feature bundle, this session reviewed and repaired its integration rather than overwriting or duplicating it.

The final built-code commit is `950611df0235d3943bf9958153efa470a342036b`. GitHub Actions run `34117631109` is green. Artifact `BOOP-Unified`, ID `10017287954`, has APK SHA-256 `95ba6292c04edaa4db2f1028337f0b3009a7c5006ee9b423e1bc40a9addef4fb`.

Implemented behavior:

- Undocked Wall/handheld BOOP keeps continuous wake-word capture disarmed and remains tap-to-talk.
- Wireless charging is the dock signal that permits foreground wake-word listening. Existing tap/TTS/settings/lifecycle coordinator rules still suspend/release the wake engine.
- Docked eyes may sleep black. A proximity nudge can request a short front-camera presence peek; BOOP does not run continuous idle image recognition while charging.
- Mirror mode accepts natural “BOOP mirror”, “Hey BOOP mirror”, polite/open variants and natural close/stop/exit/back-to-BOOP variants. Explicit mirror mode is the intentional continuous front-camera case.
- Horizontal mirror mode reserves quiet `INSIDE` and `OUTSIDE` side rails. Home Assistant sensor entity mappings are deliberately deferred until the sensors exist; no entities were invented.
- Shield settings were rebuilt as a TV-first BOOP screen: black/cyan visual language, large grouped cards, generous spacing, obvious cyan/white focus, D-pad Up/Down/Left handling and Enter/centre activation, with no decorative settings animation.
- Shield displayed Home controls are fail-closed to the selected HA area. `HomeAssistantRepository` first uses HA `extract_from_target` with the selected `area_id`, so HA can resolve device-level area membership. BOOP then performs a local room-scope filter, rejects mismatched snapshots/cards and refuses unscoped control confirmation. It does not move, rename or edit HA area/device/entity configuration.

Integration fixes during CI were limited to composition/build correctness: the dock materializer was adjusted to coexist with existing chat/eye patches, and `TvSettingsView` used Android's correct `setMinimumHeight` API. No signer replacement, launcher redesign, HA configuration write, or unrelated animation change was made.

Verification in run `34117631109` passed:

- pinned unified source/feature contracts and preserved Wall source guards;
- unified materialization with existing chat/eye patches;
- latest Launcher unit tests and lint;
- latest Shield unit tests, including `RoomScopedEntitiesTest`, expanded dashboard room-safety tests and existing `TvNavigationModelTest`;
- unified unit tests for dock power, wake gating and mirror phrasing;
- permanent BOOP signer setup and signed APK assembly;
- emulator launch through `UnifiedEntryActivity` into the real Shield body route without BOOP fatal exception;
- package/version/manifest/launchable-entry checks, signer continuity, APK archive integrity and artifact upload.

Evidence boundary: v43 is CI/signer/emulator green, not physically green. The next safe step is Ryan's physical test. On handheld/tablet verify undocked mic release, wireless-dock re-arm, sleep/peek behavior, mirror orientation/rails and thermals. On Shield inspect actual remote focus/navigation and confirm real HA inventory shows only the assigned room. If v43 misbehaves physically, preserve the evidence and return to exact accepted `e746aff`; do not guess by APK filename.

Durable memory for this pass: `docs/BOOP-UNIFIED-V43-MEMORY.md`. `BOOP_STATUS.md` is reconciled to the same candidate. No laptop checkout synchronization is claimed from this chat; connected GitHub is the published authority.

## Purpose

This is the first one-APK BOOP lineage. Ryan explicitly chose one canonical APK/branch after the current puppet work because separate Wall, Launcher and Shield branches/build filenames had become too confusing. Future release discipline is one intentional functional change per update, with rollback by the exact last physically accepted Git commit/tag + artifact, never by guessing filenames.

## Exact source inputs

The unified candidate was originally built from live GitHub heads fetched immediately before integration:

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

## Earlier unified verification reference

The original unified integration green build was `bb4797de5005952d0d27a6647ea17c15781b76f7`, GitHub Actions run `34104002238`, artifact ID `10011710184`, APK SHA-256 `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`. Keep this as lineage evidence, not the current v43 artifact.

## Physical status and migration limits

Because the unified package intentionally keeps `com.boop.alpha1`, a current Wall install has the cleanest in-place update path. Existing standalone Launcher (`com.boop.launcher`) and Shield (`com.boop.shieldoverlay`) installs are different Android packages, so their private app data/default-HOME/special-access grants cannot automatically become data or permissions of `com.boop.alpha1`. Expect one-time setup/reselection when the unified APK is first tested on those bodies. Do not delete the historical branches or accepted artifacts; they remain rollback/reference.

## Release rule

After a unified candidate is physically accepted, future work starts from its exact accepted commit/tag and artifact. Make intentional scoped behavior changes and roll back by exact checkpoint, not by local filename. GitHub remains the archive.

## Official yellow hands design lock, 2026-09-07

Ryan approved the side-by-side yellow hands as the official pair for BOOP everywhere. This applies to all three unified bodies and the separate animation lab. Read `BOOP_YELLOW_HANDS.md`; use `unified/assets/boop-yellow-hands/` for the canonical app reference. Preserve the exact plush yellow style, five digits per hand, short cuffs, independent floating hands and real transparency. New poses articulate the same hands, never transform them into creatures. The approved eyes/headphones stay unchanged.

Exact master SHA-256: `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`; 1774 x 887 RGBA PNG, 1541931 bytes. PNG transfer status remains separate from this runtime pass. Animation/runtime hand integration was not part of v43.

## Shield 2.5D direction and pose-study archive, 2026-09-07

Ryan requested saving the hand-pose work and the idea of deliberately using the Shield GPU for 2.5D puppetry. Read `docs/animation/SHIELD_2_5D_HANDS.md`. Keep independent hand/finger acting, earcup grips/adjustments, gaze-led motion, headphone lag/recoil and restrained layered depth as future design work. Every hand must retain four fingers plus one thumb in every pose and transition. Preserve the official master and existing eyes/headphones; no four-digit shortcut.

That animation direction remains deferred and was not changed by the v43 dock/mirror/settings work.