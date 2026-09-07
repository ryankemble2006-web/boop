# SHIELD TURBO status

Updated 2026-09-07. Owning branch: `shield-turbo-v01`.

## Current candidate

**v0.1.0 (versionCode 1): built, signed and emulator smoke-tested. Physical Shield acceptance pending.**

Package: `com.boop.shieldturbo`. Independent of BOOP's `com.boop.alpha1` app.

| Evidence | Result |
| --- | --- |
| Built source | `7bb3bf8fce1910f20165b3a7649a70a634528dab` |
| GitHub Actions | Run `34124583278`, job `101750141430`: success |
| Kotlin unit tests | 15 passed, 0 failures/errors/skips |
| Source safety guards | 4 passed |
| Android lint | 0 errors, 6 non-blocking warnings |
| Signed release | Existing secret-backed `boop-dev` signer; certificate match passed |
| APK checks | Package/version/Leanback entry, non-debuggable release and archive integrity passed |
| Installed release smoke test | API 30 emulator, handheld pixel_2 profile: launch, centre-to-analyse, D-pad card focus, Back and relaunch passed |
| Physical NVIDIA Shield | Not yet tested or accepted |
| Performance improvement | Not measured or claimed |

Signed artifact: [SHIELD-TURBO](https://github.com/ryankemble2006-web/boop/actions/runs/34124583278/artifacts/10019673866), artifact ID `10019673866`. This is a ZIP containing the APK at `shield-turbo/app/build/outputs/apk/release/app-release.apk`, plus build receipts. Extract the APK; the ZIP is not itself installable.

APK SHA-256: `b203358f8babc094c274096ec9852dd4015769bafb168ecfd8486307d4dad24f`.

Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Test artifact: `SHIELD-TURBO-TESTS`, ID `10019674284`. Detailed history and verification receipt are in `SESSION_HANDOFF.md`. Decisions and the physical acceptance checklist are in `MEMORY.md`. Main's `SHIELD_TURBO_START_HERE.md` routes this separate project.

## Implemented scope

On-demand local read-only device, RAM, storage, CPU 0 frequency, exposed thermal-zone and network-transport diagnostics. Large remote-focusable cards, scan off the UI thread, cancellation when leaving the app, source-specific unavailable/restricted states and exception containment. No continuous monitoring, background service, wake lock, camera, microphone, internet request, data cleanup, app killing or settings change. Only ACCESS_NETWORK_STATE is requested.

Capability labels distinguish app authority, not guessed root status. ADB setup/helper and tuning controls are not implemented in this release. The approved one-time-ADB direction still requires real Shield capability/persistence evidence; a retained permission must not be confused with permanent shell access.

## Remaining work

First perform the physical Shield checklist in MEMORY.md and record Ryan's result against the exact built APK. Do not create a physically accepted rollback point from emulator success. Six non-blocking lint warnings remain for SDK currency, fixed orientation, pluralisation, newer backup rules and a dedicated app icon. No arbitrary clock/governor/cache/background-app tuning has been added.

The approved plan is a design reference, not a claim every originally proposed intermediate test was run. Current build uses observed repository tooling: AGP 9.4.0, Gradle 9.6.0, Java 17 and SDK 36, with Groovy build scripts and built-in Kotlin support. Emulator smoke automation uses the installed release rather than the originally proposed AndroidX instrumentation test. Exact executed checks are recorded above.

Documentation-only commits after the built-source SHA do not change or replace the verified APK. All project work is published through GitHub; no laptop checkout synchronization or physical deployment is claimed.
