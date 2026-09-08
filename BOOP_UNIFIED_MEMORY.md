# BOOP unified memory

Updated 2026-09-08. Canonical app branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts; old primary checkouts are not automatically current.

## Durable Shield clean HOME contract

Ryan's locked rule for the Nvidia Shield launcher is:

**Remove the crap, preserve Shield behavior.**

BOOP may replace the HOME surface, but it must not intentionally replace useful Nvidia/Android system behavior around it. Preserve Shield muscle memory wherever the OS owns the function. In particular, physical acceptance requires:

- single Home returns to BOOP HOME when BOOP is selected as launcher;
- **double-tap Home continues to open Recent Apps / task switcher**;
- Back behaves normally;
- volume and CEC remain system-owned;
- Nvidia/Android system Settings remain reachable;
- existing system remote shortcuts remain system-owned;
- normal app switching remains intact;
- Android system animation scales remain untouched.

If a Shield system shortcut breaks on real hardware, repair that narrow break later. Do not respond by globally intercepting the remote, disabling/replacing the stock launcher package, silently changing secure settings, or reimplementing Shield OS behavior inside BOOP.

The launcher itself defaults to favourites only. Apps and launcher Settings remain. Play Next and app-provided content rows are optional and independently OFF by default. A disabled optional row must not instantiate or fetch its provider. Advertising, sponsored content, Shop and Discover are never restorable. The stock launcher stays installed as a recovery HOME during testing.

## Current Shield HOME implementation/evidence

`UnifiedEntryActivity` is the single exported HOME/LAUNCHER entry. On Shield, HOME routes internally to `com.boop.shieldhome.ShieldLauncherActivity`; ordinary BOOP app launches still route to the existing Shield puppet. The clean HOME code lives under `unified/shield-home/` and materializes into the unified Shield library.

Latest green candidate source: `e2c938ed0a035913b6fb8499aad1c3b89eb3aaac`.
Version: 46 / `1.2.0-unified-shield-home`.
Successful workflow: `34215725283`.
Artifact `BOOP-Unified`, ID `10051749294`.
APK SHA-256 `94f0046a93797606176fdd247c328aa189adb161cb6468346d26f69b8f71cb54`.
Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `e3fb91691c0f828edba8469e15677814fb48ce1fc2955693eb793343356e5cd4`.

Fresh non-visual evidence: 5 integration contracts passed, Launcher lint passed, 58 Shield focused tests passed, 26 Shield HOME focused tests passed, 74 unified focused tests passed, all with zero failures/errors/skips. Signed APK assembly, package/version, HOME manifest presence, internal launcher class, permanent signer and ZIP integrity passed. Ryan still owns real-device and visual acceptance.

## Permanent BOOP visual contracts

The approved paired black-lidded eye master is now materialized into the unified phone/Wall and Shield build path. Preserve approved geometry, supplied alpha and character proportions. Keep the accepted iris-only user hue behavior with blue/cyan default; do not tint whites, pupils, highlights or eyelids. Preserve existing headphones/puppetry and five-digit yellow hands.

Blink is user-confirmed working. Preserve the existing 183 ms curve, 3-7 second delay and motion/power/lifecycle gates. No global animation-setting changes.

## Home Assistant and Shield lifecycle contracts

HA device names and Home control buttons were physically accepted earlier. Preserve that working path. Home remains room-scoped to confirmed physical controllable devices, fails closed, and does not expose helpers/diagnostics/config plumbing or whole-house fallbacks on uncertainty.

Room changes tear down previous-room navigation/dashboard/socket/controller state before storing/rebuilding the new room. Shield UI density scaling remains idempotent from a stable baseline and must never become cumulative or system-wide.

## Wake and assistant contracts

BOOP permanently remains an accepted wake name; custom name is additive. Foreground wireless charging permits continuous phone wake; undocked phone remains tap-to-talk. Preserve coordinator/controller/Sherpa/recording ownership and coordinated reload/re-arm. The unified build now includes the local five-utterance custom-name enrolment path, but CI does not equal physical wake acceptance.

Assistant ownership remains explicit and reversible through supported Android routes. No overlay microphone, competing recorder, Google-disable/default/permission hacks, Button Mapper, privileged ADB ownership or OpenAI API dependency. A local key fallback requires real firmware evidence first. Success requires remote-button invocation plus actual audio from THAT remote, local routing/response, clean recording/cancel/repeat and return behavior.

## Testing and release discipline

GitHub may perform focused non-visual tests, compilation/lint, package/signature/integrity/security checks and artifact upload. Ryan owns screenshots/appearance/animation judgement and all real-device acceptance. No golden screenshots, aesthetic source-string gates or emulator install/launch acceptance.

Keep package `com.boop.alpha1` and the permanent signer. Keep private photographs, credentials, device IPs and raw diagnostics out of the public repository. No automatic installs/grants or false claims of Windows synchronization. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint.
