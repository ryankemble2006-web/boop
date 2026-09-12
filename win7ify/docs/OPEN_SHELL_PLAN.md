# BOOP Win7ify 0.2: real Start menu

Approved scope, 2026-09-12: complete the previously proposed optional Open-Shell Windows 7 Start integration, diagnostics, test/fix cycles and a downloadable EXE without further consultation. Keep full desktop Aero/taskbar replacement for Codex. The user reported no visible makeover in 0.1.1; do not describe registry-only changes as a shell replacement.

## Architecture and safety

Use the existing core backup/restore engine, a separate Open-Shell profile backup and durable ownership journal, an injectable shell host, and a new large BOOP front page. Embed the exact official stable Open-Shell 4.4.198 installer during CI, with upstream MIT notices. Pin size 9924608 and SHA256 a4d2d4459de55b5e962ba2a14f7bb794170511649138173dfa72949837b48c3f. The upstream stable installer is unsigned: checksum provenance is NOT Authenticode signing. Never bypass SmartScreen or UAC. Only the official installer is elevated, using normal Windows consent. Install StartMenu only, NOSTART=1, no reboot. Do not add ClassicExplorer, ClassicIE, update agents, protected binary patches, security changes or permission rewrites.

Reuse an existing compatible Open-Shell without claiming ownership. Stop/restart only its current-session menu. Undo restores exact saved settings and uninstalls only a pinned installation this session created. An interrupted installation has a durable pending journal and an exclusive BOOP install location; do not guess ownership from a process name. Preserve the original 0.1 backup separately. Install and undo must be repeatable. Every incomplete stage keeps recovery state and reports a stable BOOP code.

Use upstream settings: MenuStyle=Win7, SkinW7=Windows Aero, EnableStartButton=1, StartButtonType=AeroButton; mouse/Windows key open classic menu and Shift-click retains Windows menu. Save all original values first, including ShowedStyle2 and taskbar alignment. Validate the profile through Open-Shell's own settings export, then test the runtime window rather than merely a registry write. This is not appearance acceptance.

## Execution

- Write failing core tests for hash tampering, profile scope, initial/repeated install, pre-existing ownership, blocked settings, interrupted recovery, failed-install diagnostics, and retryable undo.
- Implement the session coordinator and exact typed profile, then rerun all existing 21 tests and new tests.
- Implement the Windows host: pinned embedded installer, ordinary elevation, exact install location checks, bounded waits, export verification, graceful current-session runtime control, guarded uninstall.
- Add large stage/error banner, Copy diagnostics, Save report and Open log folder. Preserve existing desktop settings page with honest wording. No auto-apply on launch.
- CI prepares the pinned payload, runs tests, compiles a self-contained EXE, runs launch/close smoke, then exercises the real installer/profile/uninstaller in its disposable Windows runner. No screenshot/golden/pixel/appearance tests.
- Inspect failed jobs, make focused fixes, rerun, then publish the verified binary and exact source/artifact receipts. Update handoff/status/memory, check live branch HEAD. Do not merge Android lineages.

## Environment boundary

Yoga was not connected when this phase started. The Linux session has no outbound DNS. Connected GitHub read/write and hosted Windows runners work. A source/installer probe was run in GitHub, not on Yoga. Do not claim a Yoga deployment or visual acceptance while offline. A morning delivery task is scheduled; no further questions are required.
