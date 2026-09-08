# SHIELD TURBO durable decisions

Updated 2026-09-08. Read `SESSION_HANDOFF.md` for exact build receipts and `STATUS.md` for the current candidate. This file records durable rules and physical evidence.

## Physical evidence now established

Ryan physically installed SHIELD TURBO on the bedroom NVIDIA Shield and confirmed the brightness control worked. Preserve the original working brightness checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Ryan also physically installed the first STANDARD control-centre candidate, source `91b2e28178d14ba4f2098076b958726ce064a8e1`, run `34187498176`, artifact `10041082348`. Two Shield-only UX defects were observed: the TURBO maintenance row could not be reached with D-pad Down, and APPS OK showed the package name instead of directly launching the app. Those observations are authoritative physical feedback and must not be lost.

The current machine-verified correction is source `d277ebe713cdbe5298f6205ef34fa4d493ea2114` with functional change at `8d48e3b30c51605bbc9d47bdad01e55bf651abb9`, run `34189880390`, artifact `10041897001`, APK SHA-256 `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`. It routes the last TURBO result down to `FREE SPACE`; the three maintenance controls navigate horizontally and back upward; APPS OK launches directly and hold-OK opens Android App Info. Hardware acceptance of these two corrections is pending Ryan's retest.

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

## STANDARD control-centre behavior

STANDARD is designed around a Shield remote. All useful controls must be genuinely focusable from D-pad navigation. A visible control that cannot be reached is a functional failure, not cosmetic polish.

The APPS surface is primarily a launcher: pressing OK on an app launches it immediately using the Leanback launch intent when available, then the ordinary Android launch intent as fallback. Holding OK may open Android App Info. Do not interpose package-name dialogs on normal launch.

TURBO's maintenance controls are safe routes only: storage/free-space settings, Android manage-apps settings, and restarting SHIELD TURBO itself. They are not process killers or cleaners.

## Privilege direction

STANDARD is the expected ordinary app state. ADB TURBO requires actual elevated diagnostic evidence; usage access or enabled debugging is not proof. The current app has no ADB helper/setup/grant action. A one-time grant is not a permanent shell connection, and reboot/disabling-debugging behavior must be physically verified before promises are made.

Do not begin ADB TURBO expansion until the current STANDARD candidate is physically accepted unless Ryan explicitly changes that order.

ROOT is reported only for actual root process authority. The existence of a su executable or root-management app is not authority. Do not invoke su merely to fill in a badge.

Reuse the existing secret-backed `boop-dev` signer and verify public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never create a replacement key, expose secret values, copy private keys into Git, use BOOP relay credentials, or deploy to a physical device without explicit instruction.

## Current Shield acceptance checklist

Use the current corrected STANDARD candidate recorded above.

1. Install/update SHIELD TURBO and confirm it opens normally.
2. Open TURBO. After results render, D-pad to the final reading and press Down. Confirm focus lands on `FREE SPACE`.
3. Confirm Left/Right traverses `FREE SPACE`, `MANAGE APPS`, and `RESTART TURBO`; Up returns to the diagnostic results.
4. Activate `FREE SPACE` and `MANAGE APPS` and confirm Android opens the intended settings surfaces. Confirm `RESTART TURBO` restarts this app cleanly.
5. Open APPS and press OK on several entries, including a system/media app if visible. Confirm the selected app launches directly and no `com.android...` package dialog appears.
6. Hold OK on an app and confirm Android App Info opens if long-press is delivered by the Shield remote.
7. Recheck the already-proven brightness control and confirm the STANDARD changes did not regress it.
8. Exercise NETWORK and SHIELD shortcuts with the remote and record any firmware-specific routes that Android refuses to expose.
9. Only after the above passes should STANDARD be treated as physically accepted and ADB TURBO become the next development phase.

The CI emulator is API 30 on a handheld profile with simulated D-pad input. It proves install/launch/basic navigation and absence of an app fatal exception, but physical Shield feedback remains authoritative for TV focus behavior and NVIDIA firmware settings routes.
