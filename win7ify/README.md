# BOOP Win7ify 0.2.0

A real Windows 7-style Start menu through the official Open-Shell 4.4.198 component,
with BOOP's setup/recovery interface. This is not an OS downgrade, a full Aero
desktop, or a replacement taskbar. Windows remains Windows 11.

## Use

Open `Win7ify.exe` normally. Press **INSTALL WINDOWS 7 START MENU**. The official
installer is included, checksum-pinned and checked again before execution. Windows
may request normal administrator approval; BOOP does not bypass UAC or SmartScreen.
Only the Start-menu component is installed. The menu is configured to Windows 7,
Windows Aero menu skin, replacement Start button, and left alignment. Shift-click
Start or Shift+Windows retains the Windows menu. The installed menu's appearance
and Explorer integration still depend on the installed Windows build.

**OPEN WINDOWS 7 MENU** explicitly opens and checks the menu window.
**PUT WINDOWS 11 BACK** restores saved settings and removes only the exact pinned
Open-Shell installation this BOOP session created. Existing Open-Shell installations
are not uninstalled; their original preferences and running state are restored.
If somebody updates or relocates the package, automated removal refuses to guess.
The optional **DESKTOP SETTINGS** window retains the earlier reversible preferences.
Opening either window makes no setup changes. No automatic Explorer restart.

## Diagnostics

Large BOOP E2xx codes identify the failed stage. **CHECK / REPORT**, **COPY REPORT**,
**SAVE REPORT**, and **OPEN LOGS** provide local evidence, including underlying
exceptions. Reports are not uploaded automatically. Review before sharing.
Local state: `%LOCALAPPDATA%\BOOP\Win7ify`.
The original `backup-v1.json` is kept separate from `open-shell-profile-v1.json`
and `open-shell-session-v1.json`. Do not delete recovery files before Undo.
Partial installation/removal is retryable; corrupt state blocks new changes.

Useful codes: E200 platform; E201 operation already running; E210 installer integrity;
E212 incompatible installed version; E214 recovery data; E221 declined/failed install;
E226 installer still running; E230 blocked preferences; E234 effective settings not
accepted; E240 menu did not open; E251/E261 incomplete Undo. No success is assumed
from an installer exit code or registry write alone.

## Boundaries

This is a free, non-commercial integration of unmodified Open-Shell. See
`THIRD_PARTY_NOTICES.txt`, also available inside the EXE through **CREDITS**.
Both BOOP's EXE and the pinned upstream installer are unsigned. Checksums are not
Authenticode signatures. No security/activation/update-service changes, permission
rewrites, protected Windows binary patches, cloud dependency, or silent downgrade
of an existing menu are included. The installer is a normal machine-wide Windows
Installer component, with its own Start-menu autorun; Undo uses its official uninstaller.
BOOP Android packages, signing and artwork masters are untouched.

## Build and verification

Branch `boop-win7ify-v01`, `.github/workflows/win7ify-build.yml`.
Run `win7ify/tools/Prepare-Payload.ps1` before a local publish to obtain the pinned
upstream installer. Then `dotnet publish win7ify/src/Win7ify -c Release -r win-x64
--self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true`.
CI runs original recovery tests and new shell contracts, GUI opening/closing smoke,
a real disposable install/profile-export/repeated-setup/standalone-menu/uninstall
cycle, and binary integrity. No screenshot or appearance tests.

The hosted Windows Server runner is NOT Yoga's Windows 11 Insider desktop. The
standalone menu smoke uses Open-Shell's official `-nohook` mode; Explorer-hook and
physical appearance acceptance remain separate. Runtime error E240 must be diagnosed
on the target, not relabelled as success. Full desktop Aero and deeper shell work
remain in `CODEX_HANDOFF.md` for the advanced phase.
