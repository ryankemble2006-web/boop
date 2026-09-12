# BOOP Win7ify session handoff — 2026-09-12

## Ownership

This branch is `boop-win7ify-v01`, an isolated Windows utility branch created from live `main`. It is adjacent to BOOP's Android products and does not replace or modify `boop-unified`, `boop-canonical-rebuild`, Wall, Launcher, Shield, Turbo, Android permissions or signing.

Source lives under `win7ify/`. Design and plan live under `docs/superpowers/`.

## Implemented

BOOP Win7ify v0.1 is a .NET 10 WinForms x64 utility with black/cyan BOOP presentation, the exact approved BOOP eyes, large controls and two primary actions:

- `MAKE WINDOWS 7-ISH`;
- `PUT WINDOWS 11 BACK`.

Safe preset scope: left taskbar alignment; never-combine/labels request; hide Search, Task View and Widgets; Show Desktop corner; Explorer -> This PC; classic desktop icons. The classic full context-menu compatibility key is Experimental and off by default.

The core uses `IRegistryStore`, `BackupService` and `Win7ifyService`. Before a value is first changed, its original existence/type/data is captured at `%LOCALAPPDATA%\BOOP\Win7ify\backup-v1.json`. Repeated Apply calls do not overwrite the baseline. Restore replays originals, removes originally absent values and deletes the backup only after complete success.

No protected Windows binaries are patched/replaced. v0.1 does not change security/update/activation services and does not silently install a third-party shell.

## Automated evidence

TDD evidence was preserved in GitHub Actions:

- RED run `34669603123`: first core test contract failed before implementation.
- GREEN core run `34669693420`: initial four backup/restore contracts passed; publish still intentionally failed because the GUI did not exist yet.
- RED run `34669758958`: catalog/Windows-registry conversion contracts failed before their implementation.
- GREEN run `34669833051`: catalog/conversion tests passed; publish still intentionally awaited GUI.
- RED run `34669933576`: default-preset safety contract failed before `PresetCatalog` existed.
- GREEN run `34669994610`: all nine safety tests passed; publish still intentionally awaited GUI.

First complete build/publish/upload green after GUI integration:

- source commit `0ba351eccfa138b92f34ba9fb7045c351af6c4c9`;
- workflow run `34670164392`;
- approved-eye hash check PASS;
- all nine core tests PASS;
- single-file self-contained win-x64 publish PASS;
- SHA receipt PASS;
- artifact upload PASS;
- artifact ID `10290356901`, name `BOOP-Win7ify-v0.1-win-x64`;
- artifact ZIP digest `sha256:f44c475676aa29ee9426114305396654e11401caa9bfe7c94dbebcad455a73c8`;
- `Win7ify.exe` SHA-256 `781a446060b13477762ae1ed3be286bb472b96d907c283029552197f2f30a0d9`.

The downloaded artifact was independently unpacked and the receipt hash matched the EXE byte-for-byte. `file` identified it as a PE32+ x86-64 Windows GUI executable. This is CI/build evidence only, not physical Windows visual/shell acceptance.

Approved BOOP eye SHA-256 remains `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. The workflow fails if the embedded asset differs.

## Next safe step

Run the GitHub artifact on Ryan's Windows 11 laptop and physically verify Apply then Restore. Do not turn a CI-green result into a physical checkpoint before that test.

After v0.1 physical acceptance, the advanced phase is documented in `win7ify/CODEX_HANDOFF.md`: true Windows 7 Start/taskbar experience, Explorer chrome, Aero/glass, Windows-build adaptation, Windows Sandbox regression coverage, release hardening and optional consent-driven Open-Shell integration.

## Current limitations

- Some legacy taskbar/shell values may be ignored by current/future Windows 11 builds.
- Context-menu compatibility is Experimental.
- v0.1 is not Authenticode-signed, so SmartScreen may warn.
- No physical Windows acceptance has been recorded yet.
- This branch is intentionally not merged into `main` or an Android app branch.