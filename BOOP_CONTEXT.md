# BOOP Launcher v1: signed standalone candidate

Updated 2026-09-12. Owner `boop-shield-launcher-standalone`; project `shield-launcher/`.
Package `com.boop.shieldlauncher`, version `1 / 1.0.0-launcher-tools`.
Signed source `72dde252f4f73487d9b99af52ea189777822a795`; Actions run `34684440925`
succeeded, artifact `10295068406` (BOOP-Launcher), 7,250,154-byte APK.
SHA256 `5293f65d53299bf49b45c10911693f1e3b97c6effd1a87a3734c2ee88037bb7b`.
Permanent signature, downloaded package/version/hash, actual APK dependency closure,
Android build and focused startup/defaults/close tests verified. No physical install
or acceptance on the other Shield. Failed integration work was not imported.
Read docs/verification/2026-09-12-shield-launcher-v1.md for the full receipt and limits.
Shared main product exception published at `af0db837bf9dde26d16c632fe75d8b5bca1f7fc0`.


## Inherited history below, not this branch routing

## Current v145 delivery, 2026-09-12

The user-approved Overview row repair is signed and installed. Source
`8fbe84411ec211f3adfec4409e41f3d49b2607fa`, run `34679678333`, artifact `10293642500`.
Manual Shield review confirms both BOOP defaults buttons and all four Overview
cards are visible; Use BOOP defaults was left with visible cyan focus, unpressed.
The settings/receipts, disabled list, background limits and 130% text setting were
unchanged across installation. Only the six flexible Overview child heights and
app version changed; preset logic, artwork and other branches remain untouched.
Local and signed gates passed. See SESSION_HANDOFF.md for full hashes and evidence.
Earlier v143 not-installed/failed-visibility entries below are historical. The
preset's full Apply/Undo/reboot and user acceptance are still separate.

## BOOP defaults signed feature, 2026-09-12

The isolated candidate143 is signed and verified: source `696a1b2249b2549c12c4832baf7bc2c30b913aab`,
run `34676381862`, artifact `10291778955`. No merge or device deployment
has occurred. Its real-device preset/UI acceptance remains separate. Read the
owning boop-shield-defaults SESSION_HANDOFF.md rather than an older app branch.

## Scoped BOOP defaults implementation, 2026-09-12

The isolated boop-shield-defaults branch implements the BOOP-branded preset and
pre-preset Undo. Candidate143 preserves the accepted v135 base; it is not the
latest combined app. See SESSION_HANDOFF.md. Do not merge or deploy over ongoing
work implicitly. Frozen actions are portable; private device baselines are not.

## BOOP defaults: isolated feature preparation, 2026-09-12

Own this work on `boop-shield-defaults`, at `.worktrees/boop-shield-defaults`.
Base is `582bd0d404a3d4ca61abd718551f7af5ef20aabe`, preserving accepted v135.
The product labels are **Use BOOP defaults** and **Undo BOOP defaults**; no personal
name in the feature. The prior read-only inspection is the source for the proposed
nine confirmed disables and separately recorded startup actions. Three older
unattributed/pre-existing disables and Kodi's old single restriction are excluded
from automatic inclusion. Details and exact IDs:
`docs/superpowers/specs/2026-09-12-boop-shield-defaults.md`.

This checkpoint creates/publishes the requested branch and documentation only.
Preset UI/controller implementation is not started. Baseline verification passed:
15 Startup Manager suites and Android 11 linkage. No app code, version, signing,
permissions, installed packages or Shield settings were changed. ADB was confirmed
connected. Keep the ongoing integration, lyrics and animation worktrees untouched.
Do not copy this device's Restore records to another user; preserve per-device and
pre-preset baselines. Do not install this branch by implication.

The inherited receipts below describe the accepted BASE, not a built preset.

# BOOP context

Updated 2026-09-08. Fresh main owns shared product/ownership contracts. `SESSION_HANDOFF.md` owns implementation/evidence. Normal app work uses `boop-unified`, package `com.boop.alpha1` and the permanent signer. Protect historical/physically accepted checkpoints.

## Permanent face and current replacement request

Ryan has explicitly approved the final paired-eye PNG with BLACK eyelids and no blue/cyan eyelid accent lines as BOOP's permanent default. All animations must take this same form; accessories come later as separate additions. He now requests replacement on BOTH phone/Wall and Shield. This approval supersedes older bitmap-source restrictions for this one master, not permission to redesign future poses.

Read `BOOP_EYES_MASTER.md`: source `glossy_cartoon_eyes_with_black_eyelids.png`, 1774 x 887 RGBA, 936803 bytes, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. Keep its existing alpha and proportions. Do not apply the rejected row-brightness mask, restore blue eyelid lines or regenerate the character. Preserve accepted iris-only colour selection and the default blue/cyan, plus existing headphones/puppetry and five-digit yellow hands.

The master is locally available and Ryan has a backup, but it is not yet committed/integrated. Transfer via the exposed GitHub local-file capability is blocked; direct container GitHub networking also failed. The user was asked to upload the PNG to the root of `boop-unified`. This checkpoint is documentation, not image replacement or a new APK.

## Blink is working

Ryan clarified he had switched blink off himself and it works. Earlier missing-blink claims are superseded. Leave the working blink implementation, canonical 183 ms curve, 3-7 second delay and system-motion/power/lifecycle safeguards alone. No system-setting changes.

## Approved app behaviour

Home = Room -> real supported controllable physical devices using TV/D-pad-friendly controls. No Favourites, helpers, diagnostics, config inventory or loose unconfirmed entities. HA discovery stays read-only and fail-closed using target/device/entity relationships, including inherited area. Never rename/move HA entities/devices or expose whole-house controls when membership is uncertain.

A room selection is authoritative immediately. Dispose previous-room dashboard/socket/controller/navigation ownership before persisting and rebuilding Home. Preserve D-pad navigation/focus. HA null-name repair and working Home buttons are physically confirmed and must not be refactored during the bitmap replacement.

BOOP forever: custom spoken name is additional only. Foreground wireless charging permits phone wake; undocked phone stays tap-to-talk. Preserve coordinator/controller/Sherpa/recording ownership and coordinator-owned reload/re-arm. No competing listeners. Phone acoustic wake has no new acceptance from the artwork/blink messages.

Shield density scaling remains idempotent from the unmodified application baseline, never current-density accumulation or a system-wide setting change.

## Shield remote microphone button

Use official Android assistant routes first, with explicit reversible `Use BOOP for the microphone button` / `Keep my current assistant` choice. Supported assistant entry must hand directly into existing one-shot ownership, without a second microphone stack or microphone capture in the overlay.

The last physical result remains `Assistant choice was not changed`. Previous source investigation found required recognitionService metadata missing from the shipped VoiceInteractionService; ACTION_ASSIST eligibility alone did not establish correct system selection. Do not blame firmware or invent a dummy recognizer. Exact findings/primary references remain in `SESSION_HANDOFF.md` and the prior investigation commit.

Never silently disable Google, grant permissions or change defaults. No privileged/ADB hack, Button Mapper, third-party dependency or OpenAI API integration. Local KEYCODE_ASSIST fallback requires real firmware delivery evidence. Success requires remote-button invocation AND speech from THAT remote microphone, local media/HA routing, response, clean recording end/cancel/repeat and previous-app return. Launching BOOP alone does not count.

## Acceptance and evidence

Ryan owns visual and real-device acceptance. No GitHub screenshots/golden checks, appearance/layout/animation judging, aesthetic source-string guards, emulator installation or device-launch acceptance. Keep focused non-visual tests, compile/lint, package/signature/archive/security checks and immediate signed artifact delivery.

Last delivered repair: code `949f1085328a3e815d9bc57747425f1f930c48db`, version 45 / `1.1.2-unified-assist-repair`, successful run `34201200463`, artifact `10045928699`.
APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Historical checks: 58 Shield + 66 unified focused tests, zero failures/errors/skips; Launcher lint, compilation, package/manifest/signature/archive checks. HA names/buttons and corrected blink are user-confirmed; delivered eye rendering is rejected, replacement pending, assistant and phone wake unresolved. No new blanket acceptance for room switching, repeated-open scale or remote-mic audio. No fresh code test/build is claimed by this documentation checkpoint.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Publish no secrets/private diagnostics/photos. GitHub work does not imply Windows synchronization, automatic installs/grants or unattended monitoring. Prior investigation detail is retained in Git at `438c3076875a56338ef26bd430f744f0a0cace32`.
