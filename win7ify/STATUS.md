# Win7ify 0.2.0 status

2026-09-12. Owner `boop-win7ify-v01`, source `win7ify/`.
Built source `ee744dbb8f1bae6851916801eed6163a67541344`.
GitHub run `34674142722` SUCCESS. Main artifact `10291618453`.
EXE SHA256 `ab65fe6c7b3891cf3a31fac40a9edca5a7bce80226b397c92d65432706546f67`.

## Delivered capability

Actual official Open-Shell 4.4.198 StartMenu-only integration, Windows 7 menu profile,
Aero menu skin, effective export and runtime checks, BOOP error codes/reports and
recoverable install/undo. Not an OS downgrade, full desktop Aero or replacement taskbar.
Existing Open-Shell installs are preserved. Both EXEs remain unsigned.

## Proven

41 functional tests pass. Real disposable install/profile/repeat setup/menu lifecycle/
uninstall/repeat undo pass. Normal Explorer-hook start, repeated open, clean stop and
owned uninstall PASS on hosted Windows Server; this probe was not skipped.
Exact EXE GUI and read-only diagnostics pass on Yoga; original backup unchanged.
The verified EXE is on Yoga's Desktop as `BOOP Win7ify 0.2.0.exe`.
No GitHub appearance tests, no independent-review claim.

## Still awaiting target approval

Yoga's remote process is not elevated. No unattended installer or UAC request was
started there. Actual Yoga Windows 11 Insider installation/menu appearance and
user visual acceptance are unverified. Normal Windows approval is still required.
A real failure produces an E2xx stage and local report, not a false success.

See ../SESSION_HANDOFF.md for exact tests, repaired failures, artifacts and boundaries.
