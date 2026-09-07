# SHIELD TURBO durable decisions

Updated 2026-09-07. Read `SESSION_HANDOFF.md` for the exact current build receipt and `STATUS.md` for the current candidate. This file records durable rules and physical acceptance checks, not a claim of hardware success.

## Identity and ownership

SHIELD TURBO is an independently installed app, package `com.boop.shieldturbo`, in `shield-turbo/` on branch `shield-turbo-v01`. The repository is shared with BOOP for convenience. It is not another BOOP body or a replacement for the unified `com.boop.alpha1` APK. Never merge the old BOOP runtime inherited from main into the concurrent `boop-unified` app branch.

Ryan approved development/signing through GitHub using the existing BOOP development signer. Keep Shield Turbo work confined to its own app/workflow unless explicitly authorised otherwise.

## Product rules

Diagnostics remain honest snapshots on demand. No speed-up scores, blanket RAM cleaning, overclocking, process killing, other-app data clearing or pretend network tweaks. CPU frequency is not load; available memory is not a performance score; a generic thermal zone is not automatically a CPU/GPU sensor.

The picture-brightness control is an explicitly authorised exception to the original read-only diagnostic boundary. It must remain simple, remote-operable and reversible:

- Range is 10–100%.
- 100% means no dim overlay and an untouched picture.
- Below 100% requires Android's display-over-other-apps permission.
- The overlay is owned by the private, non-exported `BrightnessService` and must not intercept focus or touch input.
- The chosen percentage is stored locally so the setting can persist while moving between apps.
- Returning to 100% is the immediate undo path.
- Do not silently broaden this into colour, gamma, HDR, clock/governor or other tuning controls without separate evidence and approval.

Current permissions are `ACCESS_NETWORK_STATE` for diagnostics and `SYSTEM_ALERT_WINDOW` for the authorised dim overlay. No microphone, camera or relay credentials are involved.

## Privilege direction

STANDARD is the expected ordinary app state. ADB TURBO requires actual elevated diagnostic evidence; usage access or enabled debugging is not proof. The current app has no ADB helper/setup/grant action. A one-time grant is not a permanent shell connection, and reboot/disabling-debugging behavior must be physically verified before promises are made.

ROOT is reported only for actual root process authority. The existence of a su executable or root-management app is not authority. Do not invoke su merely to fill in a badge.

Reuse the existing secret-backed `boop-dev` signer and verify public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never create a replacement key, expose secret values, copy private keys into Git, use BOOP relay credentials, or deploy to a physical device without explicit instruction.

## Physical Shield acceptance checklist

Use the exact brightness live-test candidate recorded in `SESSION_HANDOFF.md`: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

1. Install it independently of BOOP. Confirm SHIELD TURBO appears in the Android TV launcher and starts normally.
2. Verify ANALYSE SHIELD and all result cards still work using only the remote. Centre opens details; Back closes them.
3. Focus PICTURE BRIGHTNESS and verify remote Left/Right changes the percentage through the full 10–100% range without trapping focus.
4. At 100%, confirm there is no visible dimming and no overlay permission is required for the untouched state.
5. Move below 100%. Confirm the Shield presents the display-over-other-apps permission flow in understandable form. Grant it, return to SHIELD TURBO and verify dimming can be applied.
6. Test several points, especially 90%, 50%, 10%, then return to 100%. Confirm the dimming is monotonic and 100% completely removes the overlay.
7. While below 100%, leave SHIELD TURBO for launcher, Kodi/media and another app. Confirm the selected dim level persists without stealing D-pad focus, clicks, playback controls or BOOP behavior.
8. Reopen SHIELD TURBO and confirm the displayed percentage matches the active setting. Change it again and confirm the new value persists across app switches.
9. Exercise sleep/wake. If practical during acceptance, reboot once. Record whether dimming returns as expected, disappears safely, or needs the app reopened. Do not promise boot persistence until observed.
10. Deny/revoke display-over-other-apps and confirm the app fails safe: no stuck overlay, no crash, and 100% remains available as the clean state.
11. Run the original diagnostic checks: compare memory/storage/device/transport with device settings and record exact exposed CPU/thermal source paths without guessing sensor identity.
12. Record Ryan's result against the exact source/run/artifact/checksum above. Only explicit positive Shield hardware evidence creates a brightness rollback checkpoint.

The CI emulator is API 30 on a handheld profile with simulated D-pad input. It proved install/launch/basic navigation and absence of an app fatal exception, but it did not exercise the Shield overlay-permission UI or prove actual TV dimming.
