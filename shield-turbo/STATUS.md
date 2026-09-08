# SHIELD TURBO status

Updated 2026-09-08. Branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.

## Current candidate

**v0.2.1 (versionCode 3) is built and signed with a direct TV Display & Sound activity target. Required release checks passed. Exact NVIDIA firmware destination is not yet physically accepted.**

| Evidence | Receipt |
| --- | --- |
| Built source | `0e00f7c44758aa4192e7664b61d477b11494942d` |
| Workflow | Run `34192942800`, job `101954559616` |
| Required release gates | Unit tests, source safety contracts, lint, signed assembly and package/version/signer/archive verification succeeded |
| Signed artifact | `SHIELD-TURBO`, ID `10042893415`, ZIP `709601` bytes |
| Artifact ZIP SHA-256 | `2b4e8870ac728f44b5708c58710ad7844802fb3686a2113e8b85f3c3d8161331` |
| Delivered APK | `Shield-Turbo-v0.2.1.apk`, `2198446` bytes |
| APK SHA-256 | `0572aa2e66481703a91490078f6291350824bd266cbd44a834bfc4015cfada27` |
| Signer certificate SHA-256 | `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` |
| Nonvisual emulator check | Separate install/launch check ran after artifact upload; still in progress at initial APK delivery. Consult the run for its later conclusion |
| Visual acceptance | Ryan, on the physical NVIDIA Shield; not GitHub |

The downloaded ZIP and extracted APK were checked against the build receipts, including CRC integrity, source, package/version and signer metadata. No screenshots, UI hierarchy assertions or visual judging were used.

## Hardware evidence

Ryan confirmed v0.2.0's ADVANCED -> Developer Options shortcut opens correctly. That route and all advanced control code are unchanged in v0.2.1.

Ryan rejected v0.2.0's SHIELD -> DISPLAY + SOUND behavior: it opens general Android Settings instead of the proper Shield page. v0.2.1 targets `com.android.tv.settings/com.android.tv.settings.device.displaysound.DisplaySoundActivity` explicitly and removes all unrelated fallbacks. The component is evidenced in AOSP TV Settings, not captured from Ryan's firmware. The native NVIDIA destination requires his retest.

Brightness was previously confirmed on the bedroom Shield. Maintenance-row selectability was confirmed after the STANDARD correction. Neither the one-time ADB permission grant nor 0.5x/off/normal animation behavior has been physically confirmed by the latest feedback.

## Scope and acceptance

Only the destination policy and route adapter changed at runtime. No permissions, signing identity, brightness, app-launch, advanced-action or UI layout changes. CI uploads the verified signed candidate before its slower nonvisual install/launch check, following the current manual-acceptance rule.

Install over v0.2.0 and retest DISPLAY + SOUND. Do not accept a broader settings surface as success. See `SESSION_HANDOFF.md` for primary-source provenance and historical build/rollback receipts.

ADB TURBO retains its optional manual `WRITE_SECURE_SETTINGS` grant and three animation-scale presets only. No root, permanent ADB connection, clock tuning, process killing, blanket cleaners or other-app data clearing.

The historical `shield-turbo-v0.1.0` prerelease remains unchanged. All signing uses the existing secret-backed `boop-dev` signer.
