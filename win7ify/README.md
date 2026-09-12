# BOOP Win7ify 0.1.1

A BOOP-themed settings makeover for Windows 11, not an operating-system downgrade
or a full Windows 7 Start menu/taskbar replacement.

## Use it

Open the EXE normally. The window opens maximised with a scrollable options area
and permanently visible actions. Opening it does not apply settings.

- **MAKE WINDOWS 7-ISH** saves the original values, then processes the selected settings.
- **PUT WINDOWS 11 BACK** restores that saved baseline, not guessed factory defaults.
- **REFRESH DESKTOP...** separately asks permission to restart your current Explorer desktop. Finish copying/moving files first; Explorer windows may close.
- **TASKBAR SETTINGS** opens Windows' own taskbar options for settings Windows protects.

Results distinguish **saved and read back**, **already correct**, **blocked**, and
**not verified**. A blocked setting does not abandon the rest. Read-back confirms
a registry value, not that Windows rendered a particular appearance.

## Included options

Left taskbar alignment; separate taskbar buttons/labels request; hide Search,
Task View and Widgets; Show Desktop corner; Explorer opens to This PC; classic
Computer, user files, Network, Control Panel and Recycle Bin desktop icons.
The old full right-click menu remains Experimental, off by default, and requires
confirmation when selected. Windows updates can ignore legacy settings.

## What failed in 0.1

The first real laptop test hit a protected Widgets value (`TaskbarDa`). It was
already set as requested, but 0.1 needlessly tried to rewrite it. Windows denied
the write and the whole batch stopped. Restore had the same needless-write flaw.
0.1.1 skips identical values and reports genuine denials without forcing permissions.
The original nine tests had not covered that protected-value failure.

## Backup and safety

Your original settings remain at `%LOCALAPPDATA%\BOOP\Win7ify\backup-v1.json`.
Existing 0.1 backups are compatible and must not be deleted before retrying.
Repeated Apply preserves the first baseline. Backup updates use a flushed temporary
file and atomic replacement. Unsupported/corrupt/out-of-scope backups stop the
operation before writes. Restore keeps the complete original backup if any value
cannot be restored or verified; retrying remains possible. Empty registry keys
are left alone because the old format never recorded who created those keys.

Changes remain in the current user's registry. No elevation, permission rewrites,
Windows security/update/activation changes, system-binary patching, third-party
shell installation, or telemetry is added. Logs are local, written only during
user-requested operations under `%LOCALAPPDATA%\BOOP\Win7ify\logs`.

The approved BOOP eyes are embedded byte-for-byte unchanged. This Windows EXE is
not Authenticode-signed. Android BOOP packages and signing are unrelated and untouched.

## Build and verification

Workflow: `.github/workflows/win7ify-build.yml` on branch `boop-win7ify-v01`.
.NET 10, Windows x64, self-contained single-file EXE. GitHub runs functional tests,
asset integrity, compilation, and an opening/closing process smoke test. It does
not judge appearance or click Apply on a real user's desktop. The real-registry
test uses only its own randomly named disposable test subtree.

Main artifact: `BOOP-Win7ify-v0.1.1-win-x64`. Separate developer test artifact:
`BOOP-Win7ify-checks-v0.1.1`. See `../SESSION_HANDOFF.md` for exact build/test receipts,
`STATUS.md` for current evidence, and `CODEX_HANDOFF.md` for the advanced phase.
