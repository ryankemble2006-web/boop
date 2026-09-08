# SHIELD TURBO status

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.

## Current STANDARD live-test candidate

**v0.1.0 (versionCode 1): STANDARD control-centre bugfix candidate is built, signed, CI-verified and emulator smoke-tested. Real Shield confirmation of the two bug fixes is pending.**

| Evidence | Result |
| --- | --- |
| Built source | `d277ebe713cdbe5298f6205ef34fa4d493ea2114` |
| Functional fix commit | `8d48e3b30c51605bbc9d47bdad01e55bf651abb9` |
| GitHub Actions | Run `34189880390`, job `101945584070`: success |
| Kotlin unit tests | 29 passed, 0 failures/errors/skips |
| Source safety contracts | 6 passed |
| Android lint | success |
| Signed release | Established secret-backed `boop-dev` signer; certificate match passed |
| APK checks | Package/version/Leanback entry, non-debuggable release and archive integrity passed |
| Installed release smoke test | API 30 emulator install/launch/analysis/D-pad smoke passed |
| Signed artifact | `SHIELD-TURBO`, ID `10041897001` |
| Test artifact | `SHIELD-TURBO-TESTS`, ID `10041897447` |
| APK SHA-256 | `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1` |
| Signer certificate SHA-256 | `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` |

## Physical evidence and current fixes

Ryan physically installed the earlier brightness candidate on the bedroom NVIDIA Shield and confirmed brightness worked. That establishes real Shield evidence for the 10–100% dimming feature.

Ryan then installed the first STANDARD control-centre candidate from source `91b2e28178d14ba4f2098076b958726ce064a8e1` / run `34187498176` and reported two concrete defects:

1. TURBO's `FREE SPACE`, `MANAGE APPS`, and `RESTART TURBO` controls were visible but unreachable by D-pad focus.
2. APPS normal click opened a package-name dialog such as `com.android.gallery3d` instead of launching the selected app.

The current candidate fixes both root causes. The final diagnostic result now routes D-pad Down to `FREE SPACE`, with horizontal maintenance navigation and an Up path back to the final result. APPS normal OK directly launches the selected app; holding OK opens Android App Info.

These two fixes are machine-verified but not yet physically accepted on the Shield. Do not mark them hardware-proven until Ryan installs this exact candidate and confirms them.

## Implemented STANDARD scope

The control centre contains TURBO diagnostics and safe Android settings routes, PICTURE brightness plus read-only display facts, APPS launch/App Info, NETWORK state/settings, and SHIELD information/settings shortcuts. No fake RAM cleaner, process massacre, overclocking, arbitrary governor tuning, or other-app data clearing is present.

ADB TURBO remains deferred until STANDARD is physically accepted. Brightness remains the only intentionally picture-changing feature: 10–100%, with 100% removing the dim overlay. Current manifest permissions remain `ACCESS_NETWORK_STATE` and `SYSTEM_ALERT_WINDOW`.

## Historical receipts

The first physically proven brightness source was `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

The first full STANDARD candidate was source `91b2e28178d14ba4f2098076b958726ce064a8e1`, run `34187498176`, artifact `10041082348`. It passed CI but the two Shield UX defects above were found in physical use.

The older published `shield-turbo-v0.1.0` prerelease is pre-brightness and remains historical only. Do not repoint that existing tag.
