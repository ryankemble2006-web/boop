# SHIELD TURBO status

Updated 2026-09-07. Owning branch: `shield-turbo-v01`.

## Current live-test candidate

**v0.1.0 (versionCode 1): brightness-enabled source is built, signed, CI-verified and emulator smoke-tested. Physical NVIDIA Shield brightness acceptance is pending.**

Package: `com.boop.shieldturbo`. Independent of BOOP's `com.boop.alpha1` app.

| Evidence | Result |
| --- | --- |
| Built source | `192879ba87082b9daf5275c89a706bfd5f1106d2` |
| GitHub Actions | Run `34129557124`, job `101766129743`: success |
| Kotlin unit tests | 17 passed, 0 failures/errors/skips |
| Source safety contracts | 4 passed |
| Android lint | 0 errors, 9 non-blocking warnings |
| Signed release | Established secret-backed `boop-dev` signer; certificate match passed |
| APK checks | Package/version/Leanback entry, non-debuggable release and archive integrity passed |
| Installed release smoke test | API 30 emulator: install, cold/warm launch, analysis action and D-pad card focus passed |
| Physical NVIDIA Shield brightness | Not yet tested or accepted |

Current live-test artifact: [SHIELD-TURBO Actions artifact](https://github.com/ryankemble2006-web/boop/actions/runs/34129557124/artifacts/10021629767), ID `10021629767`, ZIP size `693490` bytes. APK path inside: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.

APK SHA-256: `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Artifact ZIP SHA-256: `918c3f47787d13b14ab050ab3d22f3bcb33fb9632f17b6f2355019b39c507ac3`.

Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Test artifact: `SHIELD-TURBO-TESTS`, ID `10021630297`.

## Implemented scope

The existing on-demand diagnostics remain local and remote-friendly. The authorised brightness addition provides a focusable 10–100% control: 100% leaves the picture untouched; lower values use Android's display-over-other-apps permission and a private non-exported `BrightnessService` overlay. The selected value is stored locally. Current manifest permissions are `ACCESS_NETWORK_STATE` and `SYSTEM_ALERT_WINDOW`.

No arbitrary CPU/governor/cache/background-app tuning has been added. ADB setup/helper remains deferred. Capability labels still report app authority rather than guessed device root status.

## Live-test boundary

CI verifies source logic, build/signing identity, archive integrity and basic installed-release navigation. It does not verify real NVIDIA Shield overlay behavior, the Shield permission screen, cross-app persistence, sleep/reboot behavior, or perceived picture quality. Those checks are in `MEMORY.md` and must be recorded against this exact artifact before calling the brightness feature physically accepted.

Nine non-blocking lint warnings remain for SDK currency, fixed orientation, pluralisation, backup configuration, missing app icon and percentage-text localisation. No lint errors remain.

The older published `shield-turbo-v0.1.0` prerelease contains the pre-brightness APK from source `7bb3bf8f...`. Do not use that direct release APK for brightness testing and do not repoint the existing tag. Documentation-only commits after source `192879ba...` do not change the verified live-test bytes.
