# SHIELD TURBO v0.1

Independent read-only NVIDIA Shield / Android TV analyser. Package `com.boop.shieldturbo`, developed through connected GitHub in chat; GitHub Actions builds and signs it. Work mode is not required.

## Use

Open SHIELD TURBO from the TV app launcher. Press **ANALYSE SHIELD**, then use the remote to move through the readings. Centre opens reading details. **ACCESS DETAILS** explains the privilege boundary; it grants nothing. No computer is needed for this release.

One on-demand snapshot reports device/Android identity, available and total RAM, internal storage, exposed CPU 0 frequency, a readable thermal zone, network transport/validation and this app's capability tier. Reads run off the UI thread and stop on leaving the app. There is no background service, continuous polling or keep-screen-on flag.

## Honest boundaries

- `STANDARD`: normal app authority. Expected on a normal installation, even if an unrelated root manager exists.
- `ADB TURBO`: only actual elevated diagnostic permission evidence qualifies. v0.1 does not request that permission or implement an ADB helper/setup. Usage access and enabled network debugging are not shell authority.
- `ROOT`: actual UID 0 authority in the app process, not a guessed root state from a file. No `su` command or root-manager request is made.

One-time ADB setup remains the approved future direction, subject to real Shield capability testing. Persistent permission grants and a live shell/helper connection are different things. This build does not promise that a shell helper survives reboot or that disabling debugging retains shell commands.

Temperature uses the Linux thermal-zone millidegree Celsius ABI. The source path is shown; a zone is not labelled CPU or GPU without sensor-identity evidence. CPU frequency is not CPU load. Internet validation is not throughput measurement. Available RAM is not wasted RAM. Restricted/missing readings do not diagnose a broken Shield.

v0.1 does not overclock, alter governors, kill apps, clear data, apply settings or communicate with BOOP. Only `ACCESS_NETWORK_STATE` is requested; there is no internet permission, microphone, camera, service or boot receiver.

## Build and verification

The dedicated workflow is `.github/workflows/shield-turbo.yml`. It runs Kotlin unit tests, source safety guards, Android lint, signed release assembly, package/certificate/archive checks and installed-release remote-input smoke tests. It uses AGP 9.4.0, Gradle 9.6.0, Java 17, SDK 36 and minSdk 28, matching the repository's observed working build tooling rather than the original plan's SDK 35 draft. Groovy build files use the plugin's built-in Kotlin support.

The existing secret-backed `boop-dev` signer is used, without copying private keys into source. Expected public certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The emulator uses Android API 30 with a handheld hardware profile and D-pad input. It is an app/remote-input smoke test, not a physical Shield or Tegra telemetry test. Read `SESSION_HANDOFF.md` for actual build and physical-verification status.
