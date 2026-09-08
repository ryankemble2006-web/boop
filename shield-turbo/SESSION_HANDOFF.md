# SHIELD TURBO handoff

Owner branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.
Candidate version: `0.1.0`, versionCode `1`.

## Current corrected STANDARD candidate, 2026-09-08

This is the candidate to hand to Ryan next. It contains the STANDARD control centre plus the two fixes discovered during physical bedroom-Shield testing.

- Built source: `d277ebe713cdbe5298f6205ef34fa4d493ea2114`.
- Functional fix commit: `8d48e3b30c51605bbc9d47bdad01e55bf651abb9`.
- GitHub Actions workflow: `Build SHIELD TURBO`, run `34189880390`, job `101945584070`, completed successfully.
- Signed candidate artifact: `SHIELD-TURBO`, ID `10041897001`, ZIP size `708605` bytes, artifact digest `sha256:a0be86bd02e2bcfeeb6eee118559777c1e4e052548806155a490e1b3683716d8`.
- Test artifact: `SHIELD-TURBO-TESTS`, ID `10041897447`, ZIP size `37144` bytes, digest `sha256:c5f60787a2bd3f6ab11528ff7a3a672144a0069de628e4c6880b1286a9602e8d`.
- APK path inside artifact: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.
- Extracted APK SHA-256: `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`.
- Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Verification receipt: 29 Kotlin unit tests passed with zero failures/errors/skips; six source safety/regression contracts passed; Android lint succeeded; release assembly succeeded; package/version/Leanback/non-debuggable checks passed; signer fingerprint and APK archive integrity passed; API 30 installed-release emulator smoke passed; artifact upload succeeded.

## Physical Shield evidence

Brightness has real hardware evidence. Ryan installed the earlier brightness candidate on the bedroom NVIDIA Shield and confirmed the brightness control worked. Preserve that rollback checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Ryan then installed the first STANDARD control-centre candidate from source `91b2e28178d14ba4f2098076b958726ce064a8e1`, run `34187498176`, artifact `10041082348`. He found two concrete defects:

1. In TURBO, `FREE SPACE`, `MANAGE APPS`, and `RESTART TURBO` were visible but not selectable with the Shield remote.
2. In APPS, pressing OK opened a package-name dialog such as `com.android.gallery3d` rather than launching the app.

Root causes were confirmed in source. The last diagnostic card explicitly looped D-pad Down back to itself. APPS normal click explicitly built a package-name dialog before offering LAUNCH / APP INFO.

## What the current candidate changes

TURBO now gives the maintenance buttons generated view IDs and explicit remote navigation. The final diagnostic card routes Down to `FREE SPACE`; Left/Right moves through `FREE SPACE`, `MANAGE APPS`, and `RESTART TURBO`; Up returns to the final diagnostic card. The no-results fallback also routes from the top controls to the maintenance row.

APPS now treats normal OK as launch. It uses `getLeanbackLaunchIntentForPackage` first and Android's ordinary launch intent as fallback. Holding OK opens Android App Info. The package-name dialog is removed from the normal path.

Regression guards were added before the production fix. The intentional RED run was `34189571449`, where the new contracts failed against the old behavior while the existing Kotlin suite stayed green. A first GREEN attempt exposed an over-broad source assertion that also matched unrelated home-row navigation; that test-only false positive was narrowed at `d277ebe7...`. Run `34189880390` is the final green receipt.

## STANDARD scope and next step

STANDARD currently contains:

- TURBO: honest on-demand diagnostics, storage sanity text, safe storage/manage-app routes, restart-this-app.
- PICTURE: physically proven 10–100% brightness overlay plus read-only active display/HDR facts.
- APPS: launch visible TV apps; hold OK for Android App Info.
- NETWORK: connection/reachability status, recheck, Android network settings.
- SHIELD: device/build/uptime facts plus useful Android/Shield settings routes.

No blanket RAM cleaner, process killing, other-app data clearing, overclocking, governor hacks or pretend speed-up score is present.

Ryan's next physical acceptance should specifically verify the maintenance-row D-pad path and direct app launching on this exact APK, then recheck brightness for regression. Any Shield firmware shortcut that fails should be recorded by exact button/action rather than guessed.

ADB TURBO remains the next phase only after STANDARD is physically accepted, unless Ryan explicitly changes that order.

## Signing and publication rules

Continue using the existing secret-backed `boop-dev` signer only. Never publish private key material, replace the signer, or use BOOP relay credentials. SHIELD TURBO remains independent of unified BOOP and all work stays inside `shield-turbo/` plus its workflow unless explicitly authorised otherwise.

The historical published `shield-turbo-v0.1.0` prerelease is pre-brightness and must not be repointed. Use the Actions artifact receipt above for the current candidate.

Documentation-only commits after built source `d277ebe713cdbe5298f6205ef34fa4d493ea2114` do not alter the tested APK. Always distinguish live branch documentation HEAD from the built-source receipt.
