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
