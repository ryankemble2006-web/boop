# Win7ify status

2026-09-12. Owner: `boop-win7ify-v01`. Version 0.1.1.
Built source `bb554f64e3c01bae09559366e7c431cf2e552285`.
GitHub run `34671365939` SUCCESS; artifact `10290934035`.
EXE SHA-256: `2a03f288c6e7c1c661ab561438cbd97e843939348b591abb149c90eb5c165751`.

## Proven

21 functional checks passed on GitHub and as the downloaded self-contained harness
on the user's laptop. Includes protected-write/no-op cases, partial-restore retry,
backup validation and actual typed Windows registry round-trip in a disposable subtree.
EXE compilation, asset integrity, hash receipt and upload passed. The exact GUI
opened and closed cleanly on both CI and the laptop. No settings were applied by
those launch tests. The original user backup remains byte-identical.

## Not yet proven

User acceptance of 0.1.1 Apply/Restore visible effects, the resized interface,
experimental context menu, and separately confirmed Explorer refresh.
No Windows 7 Start/taskbar replacement or Aero is implemented. Windows EXE unsigned.

## Known first-build failure, now covered

v0.1 tried rewriting protected Widgets data even when it was already correct.
That stopped the batch and made recovery vulnerable to the same needless write.
0.1.1 skips matching values, isolates denials, verifies each result and retains
original backup data after any incomplete restore. No permission bypass added.

See ../SESSION_HANDOFF.md for receipts and CODEX_HANDOFF.md for the next phase.
