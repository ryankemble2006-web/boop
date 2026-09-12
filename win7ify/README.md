# BOOP Win7ify v0.1

Win7ify is a small BOOP-themed Windows utility that nudges Windows 11 back toward familiar Windows 7 behaviour without replacing protected Windows system files.

## The two big buttons

- **MAKE WINDOWS 7-ISH** applies the selected per-user tweaks after saving their original registry state.
- **PUT WINDOWS 11 BACK** replays that saved state and removes the backup only after a complete successful restore.

The backup lives at `%LOCALAPPDATA%\BOOP\Win7ify\backup-v1.json`.

## v0.1 controls

The safe preset can request:

- Start/taskbar alignment on the left;
- separate taskbar buttons with labels where the current Windows shell still honours the legacy value;
- Search, Task View and Widgets hidden from the taskbar;
- the far-right Show Desktop corner enabled;
- File Explorer opening to This PC;
- classic Computer, User Files, Network, Control Panel and Recycle Bin desktop icons.

The old full context-menu compatibility key is deliberately **Experimental** and off by default.

Windows updates can ignore legacy shell values. Win7ify records what it actually wrote; a successful registry write is not presented as proof that a particular Windows build rendered the requested appearance.

## Safety boundary

v0.1 changes HKCU values only. It does not patch or replace Explorer, DLLs or other protected Windows binaries, does not disable Windows security/update services, does not alter BOOP Android signing, and does not silently install Open-Shell or any other third-party shell.

The approved BOOP eye asset is embedded unchanged. CI verifies its locked SHA-256 before compiling.

## Build

GitHub Actions workflow: `.github/workflows/win7ify-build.yml`.

The workflow runs the dependency-free core safety harness, verifies the approved eye asset, publishes a self-contained Windows x64 `Win7ify.exe`, writes an EXE SHA-256 receipt and uploads both as artifact `BOOP-Win7ify-v0.1-win-x64`.

The EXE is not Authenticode-signed in v0.1, so Windows SmartScreen may warn on first launch. Do not confuse that with BOOP's Android signing lineage.

## Verification levels

- Core registry/backup behaviour: automated.
- Compilation/publish/artifact hash: GitHub CI.
- Visual appearance and actual shell behaviour on a real Windows 11 installation: manual physical acceptance only.

See `CODEX_HANDOFF.md` for the advanced phase.