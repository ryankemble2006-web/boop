# SHIELD TURBO v0.1

Independent NVIDIA Shield / Android TV capability analyser housed in the BOOP repository.

## What v0.1 does

`ANALYSE SHIELD` reports device/Android identity, RAM, internal storage, exposed CPU frequency, exposed thermal data, network transport and the current capability tier.

- **STANDARD**: ordinary APK access.
- **ADB TURBO**: reserved for useful permissions granted once with ADB and retained by Android. v0.1 detects the tier but does not apply performance mutations.
- **ROOT**: detected where evidence is present; not required and not used for mutation.

A restricted or unavailable sensor is reported as such. SHIELD TURBO does not invent a speed-up percentage.

## Safety boundary

v0.1 is read-only analysis. It does not overclock, change governors, kill arbitrary apps, clear another app's data or alter BOOP. Package identity is `com.boop.shieldturbo`.

The GitHub workflow uses the repository's existing secret-backed BOOP development signer. Signing material is never stored here.
