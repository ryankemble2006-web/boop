# BOOP context

Updated 2026-09-08. Fresh main owns shared product/ownership contracts. `SESSION_HANDOFF.md` owns current implementation/evidence. Normal app work uses `boop-unified`, package `com.boop.alpha1` and the permanent signer. Protect historical/physically accepted checkpoints.

## Approved app behaviour

Home = Room -> real supported controllable physical devices using TV/D-pad-friendly controls. No Favourites, helper/diagnostic/config inventory or loose unconfirmed entities. HA discovery is read-only and fail-closed: use target expansion plus device/entity registries, including device-inherited area membership, but never move/rename HA entities/devices or expose whole-house controls when room/device identity is uncertain.

A room selection is authoritative immediately. Dispose the previous room's dashboard/socket/controller/navigation ownership before persisting the new selection and rebuilding Home. Preserve D-pad navigation/focus.

BOOP forever: a custom spoken call name is additional only; BOOP remains permanent fallback. Foreground wireless charging permits wake listening; undocked Wall/phone stays tap-to-talk. Preserve the existing coordinator -> controller -> Sherpa stream -> recording ownership, including coordinator-owned reload/re-arm after wake-name changes. Do not create competing listeners.

## Locked art, blink and Shield scaling

Approved phone/Wall eyes, corrected landscape proportions, five-digit yellow hands and headphones remain locked. No eye regeneration. Shield uses the exact locked eye RGB artwork plus canonical `BoopEyeLayout` and `BoopIdleBlink` namespace-adapted source, sharing the 183 ms blink curve and 3-7 second delay. Overlay packaging may add transparency around the same locked pixels but must not regenerate/recolour the eyes or eat dark eyelid pixels. Phone iris-only colour behaviour, whites/pupils/reflections/outline/default blue, headphones and existing puppetry stay unchanged.

Shield activity density scaling is idempotent: derive the target from the unmodified application baseline, never repeatedly scale the current density and never alter system-wide Shield density/resolution.

## Shield remote microphone button

Approved integration is Android's official assistant route first. On Shield first startup the user chooses `Use BOOP for the microphone button` or `Keep my current assistant`; the choice is reversible from Settings.

Where Android exposes it, BOOP requests `RoleManager.ROLE_ASSISTANT` through Android's user-confirmed flow. The assistant implementation is narrowly scoped `VoiceInteractionService` + `VoiceInteractionSessionService`, with explicit `ACTION_ASSIST` eligibility; the session delegates into BOOP's existing one-shot voice path. It does not create another microphone stack and the visual overlay never captures audio.

Never silently disable Google, grant permissions, change defaults or claim success without hardware evidence. A local `KEYCODE_ASSIST` fallback is permitted only if real Shield firmware proves BOOP itself receives that key without privileged/ADB hacks. No third-party Button Mapper dependency and no OpenAI API integration.

Physical success requires remote-button activation AND speech audio arriving from THAT Shield remote microphone, then existing local media/HA routing, BOOP response, clean recording end/cancel/repeat behaviour and previous-app return where appropriate. Opening BOOP alone is not success. If firmware blocks either side, document the exact limitation and user-authorised setup required; do not substitute another microphone.

## Manual acceptance rule

Ryan owns visual and real-device acceptance unless explicitly reversed. Unified CI has no screenshot/golden-image checks, appearance/layout/animation judgement, aesthetic source-string guards, emulator installation or device-launch acceptance. Keep focused non-visual functional tests, compilation/lint, package/signature/archive/security checks and immediate artifact upload.

## Current evidence

The prior signed candidate was physically rejected: assistant choice did not actually change the Shield assistant, Home displayed device names as `null`, phone acoustic wake failed for BOOP and custom name, and the opaque eye PNG showed a black border with no visible blink.

Current repair code is `949f1085328a3e815d9bc57747425f1f930c48db`, version `1.1.2-unified-assist-repair`, green run `34201200463`, artifact `10045928699`. Extracted APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Repairs target the observed roots: preserve HA JSON null semantics so real registry names can be used; allow Sherpa wake to arm without an advisory speech-support probe veto; preserve locked eye RGB while adding real alpha around the eye silhouettes; advertise both supported Android assistant eligibility routes. Fresh non-visual verification passed 58 Shield focused tests and 66 unified wake/routing/assistant tests with zero failures/errors/skips, plus Launcher lint, compilation and package/manifest/signature/archive checks. Physical Shield/Pixel retest remains required for every reported device failure, especially assistant takeover, remote-mic audio, acoustic wake and eye/blink appearance.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Publish no secrets/private diagnostics. GitHub work does not imply Windows synchronization, automatic device installation/grants or unattended monitoring.
