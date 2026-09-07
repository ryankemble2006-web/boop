# BOOP unified memory

Updated 2026-09-07.

## Canonical direction

Ryan chose to converge BOOP Wall, BOOP Launcher and BOOP Shield into one APK after the active puppet update. The reason is operational simplicity: one branch, one signed APK lineage, one intentional functional change per release, and deterministic rollback to the last physically accepted Git checkpoint/artifact.

Do not infer rollback from filenames. Git commit/tag + artifact is authority. After physical acceptance, local deployment folders keep the current signed `BOOP.apk` and optionally one last-good APK; GitHub retains historical builds/source.

## Initial unified source receipt

- Wall: `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3`
- Launcher: `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2`
- Shield: `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7`

Keep those source branches/history intact for reference and rollback. Do not delete or repoint their prior checkpoints merely because a unified candidate exists.

## Unified identity and routing

Canonical candidate branch: `boop-unified`.
Final package: `com.boop.alpha1`.
Initial unified version: versionCode 41 / `1.0.0-unified-alpha1`.
Permanent BOOP signer preserved.

Automatic profile contract:
- Android TV / Leanback / television UI mode -> Shield.
- Pixel 7 Pro -> Wall.
- Other handheld Android, including Pixel 10 Pro XL -> Launcher.
- Internal persistent override exists for recovery/debug, but normal routing is automatic.

The Wall application is the final app core. Latest Launcher and Shield sources are compiled as internal library modules. Wall-to-Launcher and Launcher-to-Wall are internal activity hops rather than separate-package transitions.

## First green build

Build source head: `bb4797de5005952d0d27a6647ea17c15781b76f7`.
Workflow run: `34104002238`.
Artifact: `BOOP-Unified`.
Artifact ID: `10011710184`.
APK SHA-256: `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

CI passed preserved Wall guards, latest Launcher tests/lint, latest Shield tests, unified app tests, signer setup, signed build, package/version/entry validation, manifest checks and archive integrity.

## Physical/migration boundary

CI green is not physical green. The unified APK still requires real-device checks.

Because the final package is Wall's `com.boop.alpha1`, Wall has the cleanest in-place update path. Standalone Launcher (`com.boop.launcher`) and Shield (`com.boop.shieldoverlay`) are separate Android package identities, so their private data, launcher-default selection and special-access grants do not migrate automatically. Expect one-time device setup when unified BOOP is first installed on those bodies.

Do not mark a unified checkpoint physically accepted until Ryan explicitly accepts the relevant real-device behavior.

## Official yellow hands, locked 2026-09-07

Ryan chose the approved mirrored, hands-only PNG as BOOP's official yellow hands everywhere. Preserve the exact yellow plush material, rounded shapes, proportions, short cuffs and five digits on each floating hand. No arms, sleeves, extra faces, animal transformations or replacement BOOP character. Articulation is allowed; redesign is not. In the Blah Blah Blah concept, the four fingers and thumb perform a talking-hand gesture and remain recognisably hands.

Full contract and exact checksum: `BOOP_YELLOW_HANDS.md`. Canonical app reference folder: `unified/assets/boop-yellow-hands/`. Animation counterpart: `animation-lab/shared-assets/boop-yellow-hands/` on `animation-freddie-mercury`. The master is 1774 x 887 RGBA with SHA-256 `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`.

This session saved documentation/manifest records only; PNG upload remains pending from the checked ZIP. No generator layers exist, and the master must not be replaced with rejected earlier creature images. This approval does not implement hands or Easter eggs in the APK and changes no physical acceptance state.
