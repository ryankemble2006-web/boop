# BOOP current status

## Ryan accepted Startup Manager v135, 2026-09-12

Ryan reported that the installed Startup Manager works perfectly after using it
to disable his unwanted packages, including the stock launcher, and prevent that
launcher's startup. This is user-reported physical acceptance of v135's Startup
Manager UI and the package controls he exercised. Preserve his selected setup.

Accepted source: `7408ab85c8c58b4478a3a64f18af834160efb346`; signed run
`34673227727`; artifact `10291770657`; APK SHA256
`b50823e9b902de049ee7e19d919947bcd2062d24bd953ad1616792bfa0485d8f`.
Use this exact v135 artifact as the accepted Startup Manager rollback reference.
The exact disabled-package inventory was not supplied. Do not infer a universal
Android/NVIDIA debloat recipe, measured performance gains, a completed Restore
round trip or reboot persistence from this report. Lyrics remains excluded.
This acceptance update is documentation only; the Shield is left untouched.

Updated 2026-09-12. Branch: `boop-v125-animation-integration`.

## Current delivery

**v135 Startup Manager is signed, installed and user-accepted. Lyrics is excluded.**
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
- Ryan accepted the Startup Manager and his exercised disable/startup controls.
  A complete Restore round trip, reboot and unrelated whole-app acceptance remain
  separate; no universal debloat package list was certified.
- No active track at completion, so no music skip was sent or playback started.

Read [SESSION_HANDOFF.md](SESSION_HANDOFF.md) for rollback, scope and next steps.
Prior detailed status is preserved in Git and
[docs/history/startup-v133/BOOP_STATUS.md](docs/history/startup-v133/BOOP_STATUS.md).
