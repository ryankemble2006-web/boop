# BOOP Launcher v1 signed receipt

Date: 2026-09-12. App: BOOP Launcher, `com.boop.shieldlauncher`.
Version: `1 / 1.0.0-launcher-tools`. APK bytes: **7,250,154**.

- Build/source commit: `72dde252f4f73487d9b99af52ea189777822a795`.
- Branch: `boop-shield-launcher-standalone`.
- Successful GitHub Actions run: `34684440925` (Build BOOP Shield Launcher).
- Successful build job: `103528848153`.
- Artifact: `BOOP-Launcher`, ID `10295068406`.
- APK SHA256: `5293f65d53299bf49b45c10911693f1e3b97c6effd1a87a3734c2ee88037bb7b`.
- Artifact ZIP SHA256: `ff7c9451d01058ca00d882ef421fab1a2b762301fe293ed4b582b6f2b1dfaba3`.
- Permanent certificate SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Source input: committed v145 `boop-shield-defaults@503cdb63d64716c9c1a568aadca97ba1d24680cd`.
- Shared main ownership-map update: `af0db837bf9dde26d16c632fe75d8b5bca1f7fc0`.

## Verification

Final local unsigned Android assembly and the fresh permanent-signed CI assembly
passed. Five extraction/component/routing contracts, the local-close adversarial
Java harness, all 15 Startup Manager suites, 27 defaults coordinator scenarios,
eight safety scenarios and profile/journal checks passed. These tests cover
nonvisual functionality and recovery, not real-device appearance or acceptance.

The downloaded signed APK was independently checked using Android apksigner;
the certificate matches the pinned permanent BOOP signer. Independent aapt checked
package, version, label, entry and declared permissions. Its hash matches both the
CI receipt and the separately downloaded attachment. Artifact ZIP digest matches
GitHub. Actual APK ZIP/DEX checks confirm required launcher/startup/defaults/close
classes, and no Unified/phone/HA session/voice runtime packages or native libraries.
The signed code lists only INTERNET, RECEIVE_BOOT_COMPLETED and MODIFY_AUDIO_SETTINGS
as uses-permissions. Media/accessibility services retain their Android binding
permissions and still require separate user enablement.

## Delivered scope and physical limits

Standalone Home, favourites, app drawer, Now Playing, local media and album browsing,
existing audio handling, Startup Manager/Clean Start, package controls, BOOP defaults
and exact device-local Undo. No voice/wake/models, Wall/phone, Home Assistant login,
overlay media puppet, Animation Lab or v146 integration/lyrics transplant.
Home's existing Now Playing puppet remains. Both app icon and TV banner are declared.
No replacement signing key, art regeneration or source-Shield settings/key copying.

No install, Android permission/role change, device cleanup, Apply/Undo or playback
command occurred in this task. The user's other Shield has not yet been physically
tested. First-launch setup, Home-button behavior, media access, native/Cast close,
defaults Apply/Undo/reboot and visual acceptance remain receiving-device checks.
The permanent-signed APK is a build-verified standalone candidate, not a fabricated
physical checkpoint. Keep Unified and old standalone rollback histories unchanged.

## Local-only files

Ignored generated sources, Gradle output/logs and a copy of the exact signed artifact
remain in this worktree's work/ and shield-launcher build directories. No source
changes or required handoffs are intentionally left unpublished. No private keys,
credentials, device addresses, private captures or build caches were committed.
