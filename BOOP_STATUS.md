# BOOP current status

Updated 2026-09-12. Branch: `boop-v125-animation-integration`.

## Current delivery

**v135 is signed and installed on the Shield. Startup Manager only; lyrics excluded.**
Version `1.2.135-startup-manager-large-text`, package `com.boop.alpha1`.
Source `7408ab85c8c58b4478a3a64f18af834160efb346`; successful run `34673227727`; artifact `10291770657`.
APK SHA256 `b50823e9b902de049ee7e19d919947bcd2062d24bd953ad1616792bfa0485d8f`. Installed bytes and permanent signer independently verified.

The fixed sidebar, package filters, action rows and details are live. A physical
screen review confirmed readable sidebar/background states at the user's unchanged
130% text setting and a populated 148-package list without the v128 inventory crash.
The four Startup Manager preference files were byte-identical across installation.
Existing approved art, settings/receipts and excluded-lyrics scope are preserved.

## Verification limits

- Local: Android compilation; 277 Unified tests; 15 Startup suites; linkage gate passed.
- Signed CI: 68 Shield plus 217 Unified focused tests, all zero failures/errors/skips;
  APK identity, permanent signer and ZIP integrity passed.
- Physical: exact install; populated Package Control; scoped large-text/focus review;
  no current-process AndroidRuntime fatal observed.
- Full destructive disable/Restore, reboot and whole-app user acceptance remain
  separately testable and are not inferred from these checks.
- No active track at completion, so no music skip was sent or playback started.

Read [SESSION_HANDOFF.md](SESSION_HANDOFF.md) for rollback, scope and next steps.
Prior detailed status is preserved in Git and
[docs/history/startup-v133/BOOP_STATUS.md](docs/history/startup-v133/BOOP_STATUS.md).
