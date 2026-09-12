# BOOP Win7ify handoff

Updated 2026-09-12. Owning branch: `boop-win7ify-v01`. Source: `win7ify/`.
This is an adjacent Windows utility, NOT the Android Unified APK. The primary
checkout and other concurrent Android worktrees are not this application's source.
Use the dedicated `.worktrees/boop-win7ify-v01` checkout on the development laptop.
Read live main shared rules, this handoff, `win7ify/STATUS.md`, `win7ify/MEMORY.md`
and `win7ify/CODEX_HANDOFF.md` before continuing.

## Current executable and build

Version: **0.1.1**, self-contained Windows x64 GUI executable, not Authenticode-signed.

- Built source: `bb554f64e3c01bae09559366e7c431cf2e552285`.
- Workflow: `.github/workflows/win7ify-build.yml`.
- GitHub run: `34671365939`, SUCCESS.
- Main artifact: `10290934035`, `BOOP-Win7ify-v0.1.1-win-x64`.
- Artifact ZIP SHA-256: `4c4eb51c4b466406f8a90717cba50e2629b7a00f28591777e8da77aac51fca13`.
- EXE bytes: `117059686`.
- EXE SHA-256: `2a03f288c6e7c1c661ab561438cbd97e843939348b591abb149c90eb5c165751`.
- Developer checks artifact: `10290479801`, `BOOP-Win7ify-checks-v0.1.1`.

Approved BOOP eyes are unchanged, SHA-256
`ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.
Do not regenerate, recompress, crop or recolour that source asset.

## First physical failure and why

v0.1 source `0ba351eccfa138b92f34ba9fb7045c351af6c4c9` compiled, but the user's
first Apply on Windows 11 Home Insider build 26220.9343 stopped with
`Attempted to perform an unauthorized operation.` It is NOT physically accepted.
The Widgets setting `TaskbarDa` was already 0, yet Apply needlessly rewrote it.
A same-value-only test reproduced the denial there while `TaskbarAl` succeeded;
neither value changed. Opening the writable parent key itself had succeeded.
Do not misdiagnose this as universally missing admin permission.

The old code abandoned all later settings on one denied write, and Restore had
the same needless-write issue. Its disabled dark restore button was unreadable
in the user's failure screenshot. That private screenshot is not in the repository.
The user's existing original backup was read for diagnosis and left byte-identical.

## Repair

- Apply/Restore skip values already correct, including protected values.
- Every attempted setting returns a named `ChangeResult`: Changed, AlreadyCorrect,
  Blocked or Failed. A denial does not abandon the independent remaining settings.
- Every write/delete is read back. No write success implies visible shell acceptance.
- Backup schema v1 remains compatible; the first baseline is never replaced by a
  later Apply baseline. Partial restore retains the complete original file.
- Backup writes are flushed and atomically replaced; malformed/unknown/out-of-scope
  backups stop the operation before registry writes. Production uses an allowlist.
- Only values are restored; empty keys are left alone because schema v1 does not
  record ownership/existence of whole keys. Investigate this before certifying
  experimental context-menu key cleanup on a future version.
- UI opens maximised, scrolls only the choices, keeps large action buttons visible,
  uses light secondary-button text, and displays failures without a disabled modal.
- Apply/Restore do NOT automatically restart Explorer. REFRESH DESKTOP asks for
  explicit confirmation after file transfers finish, and targets only the current
  session's desktop-shell Explorer. TASKBAR SETTINGS is the ordinary Windows fallback.
- One instance per session; local operation logs only; no startup Apply or elevation.

## Verification

- RED source `810324265acddfa030b44a846ca93b1ba3ef1163`, run `34670905369`:
  five new behavioral failure cases failed as intended; the old nine tests passed.
- Candidate `9be0195` initially failed test compilation because an imported fixture
  name collided with the old top-level test helper. `bb554f6` fully qualifies that
  helper; no regression assertion was removed or weakened.
- Final GitHub run: 21 functional tests passed, EXE publish passed, opening/closing
  smoke passed, asset hash and artifact receipt passed. No visual checks were used.
- Exact self-contained developer harness downloaded to the laptop: **21 tests,
  0 failures**, exit 0. The real-registry test creates/restores/deletes only its
  own random scratch subtree, not the real desktop settings.
- Exact EXE opened on the laptop as `BOOP // Win7ify 0.1.1`, reached input-ready,
  and closed normally with exit 0. No Apply, Restore or Refresh was invoked.
- Original user backup hash before/after all checks matched. It was not deleted,
  replaced, uploaded or committed.
- Downloaded artifact ZIP and EXE hashes independently verified; EXE is PE x64.
- Review: scoped source/diff and tests reviewed in this session; no independent
  reviewer/subagent was available. No claim of independent review.

## Next safe step and boundaries

Deliver 0.1.1 for the user's actual Apply and subsequent Restore test. It is
CI-green and local-launch-green, NOT yet physically accepted as a desktop makeover.
Leave Experimental context menu unchecked for the next ordinary test.
The full Windows 7 Start menu/taskbar/Aero work remains deferred for Codex;
see `win7ify/CODEX_HANDOFF.md`.

No Android app code, permission, signing, installation or protected branch changed.
No Windows permission/security/update changes, silent installs or system DLL patches.
This branch is not merged to main. Private downloaded validation executables remain
in the task's separate local validation folder; they are not repository source.
The previously unattached v0.1 documentation commit `b74261e` is now included in
this branch's published history, followed by this corrected repair handoff.
