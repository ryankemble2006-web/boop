# BOOP unified handoff

Updated 2026-09-08. Canonical app branch `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged. Fresh main owns shared contracts; this file owns current unified implementation/evidence.

## Current signed repair candidate

Code commit `949f1085328a3e815d9bc57747425f1f930c48db`, versionCode 45 / `1.1.2-unified-assist-repair`. GitHub Actions run `34201200463` completed successfully and uploaded artifact `BOOP-Unified` ID `10045928699`.

Extracted APK SHA-256: `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`. Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Artifact ZIP digest reported by GitHub: `sha256:393cf5b5f9263afed6566fe6ce8287e4043830e757bbbd112f09484695ce1c32`.

Fresh non-visual verification: 58 Shield focused tests and 66 unified wake/routing/assistant tests, zero failures/errors/skips; non-visual contracts; Launcher lint; compilation; permanent signing; package identity; manifest component presence; APK ZIP integrity. GitHub performed no emulator install/launch, screenshot/golden test, appearance judgement or aesthetic source-string check. Physical Shield/Pixel acceptance remains Ryan-owned.

## Real-device failures from previous candidate

Ryan physically tested the prior signed candidate and found:
- Shield assistant choice UI appeared, but Android did not actually change the selected assistant.
- Shield Home showed device labels as literal `null`.
- Pixel/phone had no acoustic wake for BOOP or the custom wake name.
- The copied eye PNG was opaque/black-backed and produced a visible black border; blink was not visibly working.

These failures supersede the previous candidate's physical-pending status. Do not describe that older APK as working.

## Repairs in current candidate

- Home Assistant JSON `null` values are preserved as null instead of becoming the literal string `"null"`; physical device names now fall back from `name_by_user` to the registry `name` correctly.
- Wake arming no longer depends on `SpeechRecognizer.checkRecognitionSupport()`, which can return false negatives. On supported Android with an available recognizer, Sherpa is allowed to arm and the existing real recognition start/error path remains the capability authority. Coordinator/controller/Sherpa/audio ownership is unchanged and BOOP fallback remains permanent.
- The canonical eye RGB artwork is not regenerated or recoloured. Build materialization adds an alpha silhouette to the same locked pixels, producing an actual RGBA PNG for phone/Shield use. The packaged APK was structurally confirmed to contain alpha; visual acceptance remains manual. Canonical `BoopEyeLayout` and `BoopIdleBlink` remain in use.
- Assistant eligibility now exposes both Android-supported qualification paths: the existing `VoiceInteractionService` integration and an explicit `ACTION_ASSIST` intent filter. Android still owns the user-confirmed default change. No silent default change, Google disable, privileged/ADB hack, second microphone stack or overlay capture was added.
- Existing idempotent Shield density scaling, room-switch teardown/rebuild, physical-device-only HA filtering, iris-only hue, headphones and puppetry remain unchanged.

## Physical tests still required

Ryan must verify on the new candidate:
- assistant role actually changes when `Use BOOP for the microphone button` is selected, where Shield firmware permits;
- remote mic button invokes BOOP and speech audio arrives from THAT REMOTE'S microphone;
- recording ends cleanly on response/cancel/repeat and returns to the prior app where appropriate;
- BOOP fallback and custom acoustic wake both trigger on the wireless-charging phone;
- eye border is gone, locked eye appearance remains correct, and blink is visibly present;
- Home shows real device names instead of `null`, and room switching remains authoritative;
- repeated Shield activity opens no longer shrink the UI.

If Shield firmware still refuses the assistant role or remote-mic routing, record that exact firmware behaviour and only user-authorised setup required. Do not substitute another microphone and call it working.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic user-device installation, grants, Windows sync or unattended monitoring is claimed.
