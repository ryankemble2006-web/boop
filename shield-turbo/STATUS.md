# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; independent package `com.boop.shieldturbo`.

## Current signed candidate

**v0.4.0 / versionCode 5 is built, signed and machine-verified. Startup Manager physical Shield acceptance is pending.**

| Evidence | Receipt |
| --- | --- |
| Built source | `1358925716cf2c834171b767dd94f08d0c49e013` |
| Actions run / job | `34201159209` / `101980019981`: success |
| JVM tests | 51 passed, 0 failures/errors/skips |
| Source/security contracts | 10 passed |
| Android lint | 0 errors, 22 warnings; gate passed |
| Signed artifact | `SHIELD-TURBO`, ID `10045926945`, ZIP `744061` bytes |
| Artifact ZIP SHA-256 | `239a58277ed5eed513da6c23a334a11ece67271470ec004b5bb668c834ef8380` |
| Test artifact | `10045969922`, SHA-256 `faf31d577df7708fe5f3871b8a044c4dbb8d2c7da6b86a98b0201503320f0e4b` |
| Delivered APK | `Shield-Turbo-v0.4.0.apk`, `2281902` bytes |
| APK SHA-256 | `cf12ccdfec929424ad89f6f5302c86f7b1331ef809ceef336fc0bda5d344657a` |
| Permanent signer certificate SHA-256 | `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` |
| Package/signature/archive checks | Passed; expected package/version/Leanback entry, non-debuggable release |
| Nonvisual emulator smoke | Install/cold launch/process/Back/warm launch/no-fatal passed |
| Visual tests | None; Ryan owns visual acceptance |

## Startup Manager

ADVANCED now contains **STARTUP MANAGER** for the user's specific four-Kodi-fork boot problem.

`BLOCK STARTUP / KEEP LAUNCHABLE` is the default recommendation. It saves the app's original Android background app-op modes and package enabled state, applies `RUN_IN_BACKGROUND=ignore` and `RUN_ANY_IN_BACKGROUND=ignore`, and verifies read-back. The package remains enabled for manual launch.

`HARD BLOCK / DISABLE APP` is stronger and separately confirmed. It disables only the selected user package. It does not clear data, caches, files or logins.

Per-app UNDO and `UNDO ALL TURBO STARTUP CHANGES` restore the exact original state recorded before the first Turbo mutation. Ledger entries remain when restore cannot be verified. No boot receiver or background service was added; Turbo relies on persistent Android system state rather than becoming another boot-starting app.

System/updated-system/NVIDIA/Android/Google-core/BOOP packages remain excluded. No QUERY_ALL_PACKAGES, bulk RAM cleaner, fake speed score, app-data clearing, uninstall, root, overclocking or governor changes were added.

## Physical evidence

Already accepted: bedroom brightness works; corrected STANDARD maintenance row selectable; Developer Options opens correctly. Display & Sound and Accessibility remain unresolved and were intentionally sidestepped.

No v0.4.0 Startup Manager policy has yet been physically confirmed. First hardware test is one Kodi fork with BLOCK STARTUP, reboot, confirm it does not self-start, then manually launch it normally. Repeat for the other forks only after the first succeeds. HARD BLOCK is not needed for that initial acceptance pass.

Only the existing secret-backed `boop-dev` signer was used. No physical device was modified by CI or this chat. See `SESSION_HANDOFF.md` for the TDD red/green receipts, implementation boundaries and historical rollback points.