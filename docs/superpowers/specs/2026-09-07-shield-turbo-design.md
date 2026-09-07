# SHIELD TURBO Design

Date: 2026-09-07
Status: Approved in chat
Owner: independent utility housed in the BOOP repository

## Goal

Build a separate Android TV utility for NVIDIA Shield named SHIELD TURBO. It measures what the Shield actually exposes before offering optimisation actions. It must not modify the BOOP APK, package, runtime, permissions or unified app lineage.

## Product contract

SHIELD TURBO is an independent application under `shield-turbo/` with its own package identity. It is remote-first, large and readable at television distance, and deliberately avoids fake RAM-cleaner or percentage-boost claims.

The first release is a capability and telemetry foundation. Optimisation controls are added only when they have a measurable effect and a clearly understood rollback.

## Privilege tiers

The UI exposes three capability states:

1. `STANDARD` — ordinary Android application privileges.
2. `ADB TURBO` — optional power-user state enabled by a one-time ADB setup. The design favours permissions that persist after setup so routine use does not require an attached computer or permanently open ADB session.
3. `ROOT` — detected and reported where useful, but root is not required for the intended product experience and v0.1 performs no root-only mutation.

Every feature reports the tier it requires. Unsupported probes fail visibly and safely rather than pretending to work.

## v0.1 scope

The home screen provides an `ANALYSE SHIELD` action and a dashboard of factual observations. Where Android/Shield permits, probes cover:

- Shield/device identity and Android build information;
- memory totals and current availability;
- internal storage totals and current availability;
- CPU information/frequency data exposed to the app;
- thermal status and exposed temperature/frequency sources;
- network transport and useful local connection information;
- current privilege/capability state.

Results distinguish `available`, `restricted`, `unsupported` and `error`. Missing kernel/sysfs data is normal capability evidence, not an application failure.

v0.1 does not overclock, change kernel governors, kill arbitrary protected processes, clear other applications' private data, alter BOOP, or make unsupported performance claims.

## Architecture

`shield-turbo/` is a self-contained Gradle Android application. A small probe interface isolates each telemetry source so Shield/Android-version differences cannot turn the activity into a pile of device-specific conditionals.

A capability service runs the probes and produces immutable result models. The TV UI renders those models and never infers success from button presses. A future action layer can consume the same capability model before enabling an optimisation.

Suggested units:

- `MainActivity` / TV dashboard: D-pad navigation and presentation only.
- `ShieldAnalyzer`: orchestrates probes and returns one analysis snapshot.
- `Probe<T>`: narrow interface for one observable capability.
- device, memory, storage, CPU, thermal and network probes.
- `PrivilegeDetector`: determines STANDARD/ADB TURBO/ROOT evidence without performing privilege changes.
- result models with explicit status and human-readable evidence.

## UI

Android TV / Leanback launchable application with a dark, uncluttered dashboard and strong focus treatment. Primary focus lands on `ANALYSE SHIELD`. D-pad and centre/Enter are sufficient for all v0.1 navigation.

Analysis shows useful facts first and limitations plainly. If nothing actionable is wrong, the app is allowed to say so rather than manufacturing warnings.

## ADB TURBO setup

v0.1 detects whether planned elevated permissions are present and displays the exact setup state. The setup mechanism must never embed a device address, password, private key or signing material in the repository.

Any later ADB-granted permission must satisfy all of these before becoming a Turbo action: it is supported on the target Shield software, materially useful, safely reversible or non-destructive, and its effect can be measured.

## Signing and CI

Add a dedicated GitHub Actions workflow for the independent project. Reuse the repository's established secret-backed signing infrastructure where compatible. Never publish, replace or copy signing keys into source. The workflow builds/tests the independent APK and uploads a clearly named SHIELD TURBO artifact.

A green CI build proves build/test/signing status only. Physical Shield acceptance remains a separate state.

## Isolation from BOOP

No changes to `com.boop.alpha1`, unified source materialisation, BOOP permissions, BOOP device routing, or BOOP runtime behavior are required. SHIELD TURBO may live in the same Git repository solely because repository creation is unavailable through the current connector.

The utility must have a distinct package/application identity and be independently installable/removable.

## Testing

Unit tests cover probe status mapping, unavailable/restricted sources, privilege-state derivation and analyzer aggregation. Android tests cover TV launchability and focus/navigation fundamentals where practical. CI must assemble the APK and validate package identity/artifact integrity.

Physical verification on an NVIDIA Shield is required before any claim that a probe or future optimisation works on real hardware.

## Deferred work

After v0.1 physical capability evidence exists, evaluate Quick Boost, Media Mode, BOOP Mode, safe storage cleanup, background-package controls, deeper network diagnostics and Tegra-specific controls individually. Each action needs a measurable benefit and explicit privilege/rollback story before implementation.