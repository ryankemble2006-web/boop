# BOOP Win7ify v0.1 Design

## Goal

Build a small, reversible Windows 11 utility that makes the desktop behave more like Windows 7 without replacing protected Windows binaries. GitHub Actions must produce a self-contained `Win7ify.exe` for Windows x64.

## Product boundary

Win7ify is an adjacent BOOP utility, not part of the Android Unified APK. It lives on branch `boop-win7ify-v01` and under `win7ify/`. It must not alter Unified, Launcher, Wall, Shield, Turbo, signing, permissions, or existing protected checkpoints.

## User experience

The application uses BOOP visual language: black background, cyan accents, large controls, plain English, and the exact approved BOOP eye asset reused unchanged. It presents one obvious primary action, `MAKE WINDOWS 7-ISH`, one obvious escape hatch, `PUT WINDOWS 11 BACK`, and individual chunky checkboxes for each tweak.

The first version is deliberately a restoration console rather than a shell replacement. It tells the user what changed, labels unreliable tweaks as experimental, and never claims Windows blocked behavior succeeded.

## v0.1 tweak scope

All v0.1 changes are per-user registry values under HKCU so the normal preset does not require elevation:

- left-align taskbar buttons (`TaskbarAl=0`);
- request never-combine taskbar buttons with labels (`TaskbarGlomLevel=2`);
- hide taskbar Search (`SearchboxTaskbarMode=0`);
- hide Task View (`ShowTaskViewButton=0`);
- hide Widgets (`TaskbarDa=0`);
- restore the far-right Show Desktop corner (`TaskbarSd=1`);
- open File Explorer to This PC (`LaunchTo=1`);
- show Computer, User Files, Network, Control Panel, and Recycle Bin desktop icons;
- optional experimental classic full context-menu compatibility key.

Windows 11 versions may ignore some legacy values. Win7ify records that a value was written, not that Microsoft necessarily rendered the requested shell behavior.

## Reversibility

Before writing a registry value for the first time, Win7ify captures its exact original existence, type, and data in `%LOCALAPPDATA%\BOOP\Win7ify\backup-v1.json`. Later Apply operations add newly touched values to the same baseline but never overwrite values already backed up.

Restore replays every saved value exactly. Values that did not originally exist are removed. The backup is deleted only after a complete successful restore so a failed restore does not burn the escape hatch.

## Architecture

`Win7ify.Core` owns tweak definitions, backup/restore, registry abstraction, and application logic. `Win7ify` is the WinForms shell plus Explorer restart behavior. `Win7ify.Tests` is a dependency-free console test harness using an in-memory registry implementation, keeping registry behavior testable without changing the CI runner.

The production registry adapter is the only code that talks directly to `Microsoft.Win32.Registry`. The UI never writes registry values itself.

## Safety rules

- Do not patch, replace, own, or rename Windows system DLLs or executables.
- Do not require admin for the v0.1 preset.
- Do not silently download or install Open-Shell or another third-party shell.
- Do not change Windows activation, security services, update services, Defender, account policy, or telemetry in v0.1.
- Do not use automated screenshot tests as BOOP visual acceptance.
- Do not modify the approved BOOP eyes asset.
- Restart Explorer only after a user-requested Apply or Restore operation.

## Build and evidence

Use .NET 10 LTS and WinForms. GitHub Actions on `windows-latest` runs the core test harness, publishes a self-contained single-file `win-x64` executable, writes a SHA-256 receipt, and uploads both as a workflow artifact.

CI-green means the tests/build/publish pipeline passed. It does not mean a physical Windows 11 machine accepted every shell tweak visually. Physical behavior remains a later manual acceptance step.

## Deferred Codey scope

The later advanced phase may tackle a true Windows 7 Start menu, deeper taskbar replacement, Explorer chrome, Aero/glass effects, Windows-build adaptation, Windows Sandbox regression coverage, signed releases, and optional consent-driven Open-Shell integration. Those features must remain outside v0.1 unless separately approved.