# BOOP context

Updated 2026-09-08. Fresh main owns shared product/ownership contracts. `SESSION_HANDOFF.md` owns current implementation/evidence. Normal app work uses `boop-unified`, package `com.boop.alpha1` and the permanent signer. Protect historical/physically accepted checkpoints.

## Approved app behaviour

Home = Room -> real supported controllable physical devices using TV/D-pad-friendly controls. No Favourites, helper/diagnostic/config inventory or loose unconfirmed entities. HA discovery is read-only and fail-closed: use target expansion plus device/entity registries, including device-inherited area membership, but never move/rename HA entities/devices or expose whole-house controls when room/device identity is uncertain.

A room selection is authoritative immediately. Dispose the previous room's dashboard/socket/controller/navigation ownership before persisting the new selection and rebuilding Home. Preserve D-pad navigation/focus.

BOOP forever: a custom spoken call name is additional only; BOOP remains permanent fallback. Foreground wireless charging permits wake listening; undocked Wall/phone stays tap-to-talk. Preserve the existing coordinator -> controller -> Sherpa stream -> recording ownership, including coordinator-owned reload/re-arm after wake-name changes. Do not create competing listeners.

## Locked art, blink and Shield scaling

Approved phone/Wall eyes, corrected landscape proportions, five-digit yellow hands and headphones remain locked. No eye regeneration. Shield uses the exact locked `boop_eyes.png` plus canonical `BoopEyeLayout` and `BoopIdleBlink` namespace-adapted source, sharing the 183 ms blink curve and 3-7 second delay. Phone iris-only colour behaviour, whites/pupils/reflections/outline/default blue, headphones and existing puppetry stay unchanged.

Shield activity density scaling is idempotent: derive the target from the unmodified application baseline, never repeatedly scale the current density and never alter system-wide Shield density/resolution.

## Shield remote microphone button

Approved integration is Android's official assistant route first. On Shield first startup the user chooses `Use BOOP for the microphone button` or `Keep my current assistant`; the choice is reversible from Settings.

Where Android exposes it, BOOP requests `RoleManager.ROLE_ASSISTANT` through Android's user-confirmed flow. The assistant implementation is narrowly scoped `VoiceInteractionService` + `VoiceInteractionSessionService`; the session delegates through `ACTION_ASSIST` into BOOP's existing one-shot voice path. It does not create another microphone stack and the visual overlay never captures audio.

Never silently disable Google, grant permissions, change defaults or claim success without hardware evidence. A local `KEYCODE_ASSIST` fallback is permitted only if real Shield firmware proves BOOP itself receives that key without privileged/ADB hacks. No third-party Button Mapper dependency and no OpenAI API integration.

Physical success requires remote-button activation AND speech audio arriving from THAT Shield remote microphone, then existing local media/HA routing, BOOP response, clean recording end/cancel/repeat behaviour and previous-app return where appropriate. Opening BOOP alone is not success. If firmware blocks either side, document the exact limitation and user-authorised setup required; do not substitute another microphone.

## Manual acceptance rule

Ryan owns visual and real-device acceptance unless explicitly reversed. Unified CI has no screenshot/golden-image checks, appearance/layout/animation judgement, aesthetic source-string guards, emulator installation or device-launch acceptance. Keep focused non-visual functional tests, compilation/lint, package/signature/archive/security checks and immediate artifact upload.

## Current evidence

Signed v45 code `6dab12aa3232e821fed52b64e39f65e499b6c574`, version `1.1.2-unified-assist-repair`, green run `34198363929`, artifact `10044846308`. Extracted APK SHA-256 `77fe8d06223bdaa6a07e232baeb2ddb9162845e98e022477be559fb377915a6b`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh non-visual verification passed 57 Shield focused tests and 64 unified wake/routing/assistant tests with zero failures/errors/skips, plus Launcher lint, compilation and package/manifest/signature/archive checks. Physical Shield/Pixel acceptance remains pending for remote mic/button, acoustic wake, exact eyes/blink, repeated-open scale stability, room switching and device-only Home cards.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Publish no secrets/private diagnostics. GitHub work does not imply Windows synchronization, automatic device installation/grants or unattended monitoring.
