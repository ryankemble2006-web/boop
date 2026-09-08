# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical acceptance

- Confirmed: v0.4.1 Startup Manager action menu opens; Kodi forks still launch manually.
- Failed outcome: after reboot, Ryan still sees the forks in the Shield task manager and reports noticeable improvement after swiping/force-closing them. The startup suppression requirement is not met. Do not repeat the already answered task-manager versus App Info question.
- Prior confirmations retained: bedroom brightness, corrected STANDARD maintenance selectability, Developer Options opening.
- Parked: native Display & Sound and Accessibility routes.

The latest delivered candidate remains **v0.4.1 / code 6**, built from `0961153e5dea94c38027cdf31530f500c5b29573`. Run `34204102153`, job `101989443983`, artifact `10047107169`. APK SHA-256 `c7bc147a70378dfe62a14e542aeb5dcb1036fbe6818551e98ddcf6fe061793fa`, 2283246 bytes.

Historical verification for that APK: 58 JVM tests, 13 API/safety checks, lint 0 errors / 22 warnings, permanent-signer/package/archive and nonvisual launch checks passed. That does not establish effective boot blocking. Exact ZIP/test-artifact/signer receipts are in SESSION_HANDOFF.md.

## Next direction, not shipped

CLEAN START proposal: actual selected-package force-stop with process/stopped-state verification first, then optional bounded post-boot cleanup of a reviewed app group with protected essentials and explicit KEEP RUNNING exceptions. Initial case remains the four user-selected Kodi forks. No blanket system disable, no permanent polling killer, no guessed package IDs, and no disruption to manually started foreground apps or playback.

Post-boot cleanup must not be advertised as preventing every initial boot launch. Manual opening releases stopped state; ordinary ADB cannot enforce a universal permanent manual-only policy for every package while keeping normal launching unchanged. The desired clean-boot policy is recorded; effectiveness and exact firmware mechanisms are not yet proven.

This feedback/research update is documentation-only. No app code, boot receiver, service, permission, signing, workflow or device state changed; no new APK was produced. CI is skipped. GitHub visual tests remain prohibited. Handoff, status and memory are reconciled; main and other BOOP work are untouched.
