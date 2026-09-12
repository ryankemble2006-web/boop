# BOOP Win7ify 0.2.0 handoff

Updated 2026-09-12. Owning branch: `boop-win7ify-v01`. Source: `win7ify/`.
This is an adjacent Windows utility, not an Android APK or the shared-main app.
Use the existing `.worktrees/boop-win7ify-v01` checkout; do not switch another task's
checkout. Fetch this branch and main; read shared rules plus this handoff,
`win7ify/STATUS.md`, `win7ify/MEMORY.md` and `win7ify/CODEX_HANDOFF.md`.

## User-approved scope

The settings-only 0.1.1 fixed the first Widgets-write failure, but the user reported
no visible Windows 7 makeover. He approved the proposed official Open-Shell Windows
7-style Start menu integration, diagnostics and autonomous test/fix cycles while
sleeping, without more consultation. Full desktop Aero and replacement taskbar
remain a later Codex phase. Do not call a registry-only build the finished makeover.

## Current downloadable executable

- Version: **0.2.0**, self-contained Windows x64 GUI executable.
- Built source: `ee744dbb8f1bae6851916801eed6163a67541344`.
- Workflow: `.github/workflows/win7ify-build.yml`.
- GitHub run: `34674142722`, SUCCESS; job `103500833230`.
- Main artifact: `10291618453`, `BOOP-Win7ify-v0.2.0-win-x64`.
- Artifact ZIP SHA256: `1e7573009b91c939ba5d11b5dc55c516ccef206c33166107400c1787bfa5c04c`.
- EXE bytes: `127049318`.
- EXE SHA256: `ab65fe6c7b3891cf3a31fac40a9edca5a7bce80226b397c92d65432706546f67`.
- Evidence artifact: `10291961093`, `Win7ify-v0.2.0-integration-evidence`.
- Source review artifact: `10291816283`, `Win7ify-source-review`.
- The exact EXE is also on the laptop Desktop as `BOOP Win7ify 0.2.0.exe`.
- BOOP and the pinned upstream setup program are NOT Authenticode-signed.

The artifact ZIP and internal EXE receipt were independently hashed after download;
PE headers identify x64. Renaming the copied EXE did not alter its bytes.
Approved eye SHA256 stays `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.
Never regenerate or destructively modify the approved master.

## Implemented

The EXE embeds the unmodified official Open-Shell 4.4.198 installer, pinned to
9924608 bytes and SHA256 `a4d2d4459de55b5e962ba2a14f7bb794170511649138173dfa72949837b48c3f`.
Upstream source ref: `b2070640ffbe047791de25f83063fb62bbc7d22f`.
Install only the StartMenu feature, NOSTART=1, no reboot, at the exclusive
Program Files/BOOP Win7ify Open-Shell location. Normal Windows UAC remains in charge.
No ClassicExplorer, ClassicIE, update agent, protected system-binary patch, permission
rewrite, security/activation/update-service changes, or third-party silent downgrade.

The actual profile selects Win7, Windows Aero menu skin, replacement Start button,
left taskbar alignment, classic menu on mouse/Windows key, and Windows menu on
Shift-click/Shift+Windows. Open-Shell's own XML export verifies effective values;
the runtime checks a real menu window, not just saved registry data.
The menu is not a full Windows 7 desktop or replacement taskbar.

The new BOOP front page has large install/open/undo actions, stable E2xx diagnostics,
copy/save report, local logs and credits. The old desktop-preferences page remains
separate and no longer says no shell is installed. Opening the EXE changes no OS
settings. There is no automatic Explorer restart and no cloud or microphone path.

Recovery is separate from the original 0.1 baseline:
- `backup-v1.json`: older desktop preferences, kept intact;
- `open-shell-profile-v1.json`: exact original selected Open-Shell preferences;
- `open-shell-session-v1.json`: durable ownership, stage and retry information.
All live under `%LOCALAPPDATA%/BOOP/Win7ify`. Do not delete backups before Undo.
A pre-existing compatible Open-Shell is reused, never owned or uninstalled. Undo
only removes the exact pinned installation this journal created at its exclusive
location. Version/location changes block automatic removal. Incomplete operations
retain the first baseline. Damaged recovery data is rejected before stopping a
working menu. The original desktop recovery model and 21 tests remain intact.

`THIRD_PARTY_NOTICES.txt` is embedded and shipped. This is a free, non-commercial
integration. Upstream source is MIT; its additional asset/trademark terms are
included. Do not market a commercial bundle without reviewing those terms.

## Verified tests and actual failures repaired

- Initial RED source `0d49f37`, run `34672711655`: all 12 new shell contracts failed
  before implementation. Core GREEN source `0436f93`, run `34672991011`.
- First complete 0.2 candidate `4c58127`, run `34673337829`: original 21 plus 12
  shell tests, GUI smoke, real installer/profile/export, repeat setup, standalone
  vendor menu window lifecycle, owned uninstall and repeat Undo all passed.
- Review exposed an Undo preflight flaw. RED source `feb49ea`, run `34673564303`:
  missing/corrupt profile stopped a pre-existing menu. Two tests failed; 18 passed.
  `cf1b7ac` validates the whole original backup before stopping anything.
- The added real Explorer-hook cycle at `cf1b7ac`, run `34673778262`, opened the
  actual menu successfully but Undo hit E241. Recovery stayed available and the
  cleanup retry removed only the owned installation. This failure was NOT hidden.
- Upstream handles MSG_EXIT only when CanShowMenu permits it; an active/in-flight
  menu can prevent exit. `ee744db` first dismisses the vendor menu window belonging
  to the current desktop-shell process, waits for forwarded open commands, then
  uses bounded official -exit requests. It never force-kills Explorer.
- Final `34674142722`: **21 original tests + 20 shell tests = 41, zero failures**.
  Asset/payload integrity, publish and GUI open/close passed. Real official
  install, vendor effective-profile export, repeated setup preserving the backup,
  no unrequested components, standalone menu open/close, owned uninstall, repeat
  Undo and intentional diagnostic failure E298 passed.
- The final normal Explorer-hook probe was **PASS, not SKIPPED**: actual install/
  configure/start, repeated open, graceful stop and owned uninstall all passed
  on the hosted Windows Server 2025 desktop. It is not Yoga visual acceptance.
- No screenshot, golden-image, pixel, geometry or appearance tests were used.
- Scoped source/diff review was performed in-session. No independent reviewer was
  available; do not invent independent review. GitHub action Node deprecation
  warnings are not app compile failures and remain a build-tool maintenance item.

## Actual Yoga evidence and remaining boundary

Yoga was offline initially and reconnected during final testing. The clean owning
worktree was fast-forwarded after fetching/checking both live branches; other
worktrees were preserved. The remote process is NOT elevated. No Open-Shell was
registered in either Windows registry view and no prior menu process was present.

The exact 0.2 EXE was downloaded directly from its GitHub run and hash-verified on
Yoga. --verify-payload and --diagnose returned 0; --diagnostics-selftest returned
its expected 1 with E298 and stack information. The GUI opened as
`BOOP // Win7ify 0.2.0`, reached input-ready and closed normally with exit 0.
The original user's desktop backup stayed byte-identical. No install, Apply,
Restore, Refresh, UAC prompt, reboot or media-control action was performed there.
The EXE was placed on the Desktop for morning use.

**Outstanding target gate:** the real install on Yoga needs the ordinary Windows
approval. That was not bypassed or presented while the user slept. GitHub's real
install/hook/uninstall results are not a claim of Yoga Insider 26220 compatibility
or Ryan's visual acceptance. A target failure will produce its exact E2xx report.

A one-time morning delivery task is scheduled. It should fetch current receipts,
deliver this actual 0.2 artifact, and not silently install anything later.
This branch remains isolated; Android source, packages, permissions, permanent
signer and protected checkpoints are untouched. See CODEX_HANDOFF for advanced
work, and preserve 0.1.1 only as historical settings-recovery provenance.
