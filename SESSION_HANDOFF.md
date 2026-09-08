# BOOP unified handoff

Updated 2026-09-08. Canonical app branch `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged. Fresh main owns shared contracts; this file owns current unified implementation/evidence.

## Latest physical retest: partial success, NOT an accepted release

Ryan tested the repair APK from code `949f1085328a3e815d9bc57747425f1f930c48db` and reported:

- **PASS, user-confirmed:** HA device names no longer show `null`, and Home buttons actually control his devices. Preserve this working HA path; do not refactor it during the remaining eye/assistant investigation.
- **FAIL, user-confirmed:** Shield eye rendering became worse. The supplied photograph shows horizontal, comb-like tearing at the inner upper eyelid edges. No visible blink was reported. The photo stays private and is not committed.
- **FAIL, user-confirmed:** Android still reports `Assistant choice was not changed`. The default assistant has NOT been successfully changed. Remote-button activation and remote-microphone audio remain unverified.
- Phone acoustic wake was not retested in this latest message. Earlier failure remains unresolved, not a newly confirmed pass or fail for this repair.

This evidence supersedes physical-pending or repair-success wording from the build-time notes. A green build and an RGBA file header did not establish a correct eye silhouette or working assistant integration.

### Investigation of the delivered code

Live heads checked before investigation: unified `a1b3e549db1104d7503787b668fc0a39626dd12d`; main `dd38cfc72eb5d00bc42121f88c633cb237805009`. The local copy of the supplied APK was hashed again and matches the SHA-256 below. The artifact receipt confirms its code commit. Windows `C:/Users/ryank/Documents/Codex/BOOP` and its WSL mount are not mounted in this Chat container; connected GitHub remains the authority. No Windows synchronization is claimed.

1. `scripts/make-locked-eyes-transparent.py` does not use an approved transparency mask. It guesses each row's opaque span from RGB brightness (`VISIBLE_THRESHOLD = 3`, two-pixel expansion). Dark eyelid pixels are indistinguishable from the black background in that rule; independently changing row endpoints can create the reported horizontal tearing. Do not attempt another arbitrary threshold adjustment or describe this guessed mask as the exact locked silhouette. The original eye art must not be regenerated; phone iris-only colour and headphones/puppetry remain protected.
2. `scripts/patch-unified-assistant-button.py` declares a VoiceInteractionService whose metadata omits `android:recognitionService`. AOSP Android 11/12 VoiceInteractionServiceInfo rejects a missing recognitionService. This is a concrete integration defect, not proof that Shield firmware blocks all official assistant routes. Adding an ACTION_ASSIST filter alone did not repair that service declaration. References: https://android.googlesource.com/platform/prebuilts/fullsdk/sources/android-30/+/refs/heads/androidx-sharetarget-release/android/service/voice/VoiceInteractionServiceInfo.java and https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/services/voiceinteraction/java/com/android/server/voiceinteraction/VoiceInteractionManagerService.java . Do not invent a dummy recognizer, add a competing microphone stack or blame firmware before valid official routing is checked.
3. Shield's `eyeBlinkAllowed()` explicitly vetoes blink when ValueAnimator reports animations disabled, the animator-duration scale is zero, or power saving is active, as well as applying display/visibility/lifecycle gates. The supplied still photograph cannot identify which runtime gate is active. Ask whether system animations were disabled, including through Turbo, before guessing another animation repair. Do not silently change system settings or remove motion/accessibility safeguards.

No new app repair or APK was produced in this investigation. Only documentation of the actual physical result and source findings is being published. No screenshot/golden test, aesthetic source-string check, emulator/device installation or automated visual acceptance was run.

## Current signed repair candidate, build receipt

Code commit `949f1085328a3e815d9bc57747425f1f930c48db`, versionCode 45 / `1.1.2-unified-assist-repair`. GitHub Actions run `34201200463` completed successfully and uploaded artifact `BOOP-Unified` ID `10045928699`.

Extracted APK SHA-256: `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`. Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Artifact ZIP digest reported by GitHub: `sha256:393cf5b5f9263afed6566fe6ce8287e4043830e757bbbd112f09484695ce1c32`.

Build-time non-visual verification: 58 Shield focused tests and 66 unified wake/routing/assistant tests, zero failures/errors/skips; non-visual contracts; Launcher lint; compilation; permanent signing; package identity; manifest component presence; APK ZIP integrity. These are historical checks for that exact artifact, not new tests or proof of the failed physical behaviours.

## Earlier repairs and protected boundaries

The preceding candidate `6dab12aa3232e821fed52b64e39f65e499b6c574` had literal-null device labels, no phone BOOP/custom wake, an opaque eye border/no blink and unsuccessful assistant selection. The current candidate repaired JSON null/name fallback, removed the advisory speech-support probe as a wake-arming veto, added the subsequently rejected brightness-derived eye alpha, and added ACTION_ASSIST eligibility. The latest physical results above distinguish what actually worked.

Existing idempotent Shield density scaling, room-switch teardown/rebuild, device-only HA filtering, coordinator-owned wake-name reload/re-arm, permanent BOOP fallback, iris-only hue, headphones and puppetry remain protected. Foreground wireless charging permits phone wake; undocked phone stays tap-to-talk. HA discovery/membership stays read-only and fail-closed; no Favourites or whole-house fallback.

The assistant design remains reversible explicit user selection through official Android routes, handing into BOOP's existing one-shot ownership. No Google disable, silent defaults/grants, privileged/ADB hack, third-party Button Mapper, OpenAI API integration, second microphone stack or microphone in the visual overlay. Do not add a local KEYCODE_ASSIST fallback without real firmware key-delivery evidence.

Physical assistant success requires BOTH button invocation and audio from THAT remote, local media/HA handling, response, clean recording end/cancel/repeat and previous-app return where appropriate. Opening BOOP alone is not success. Room switching, repeated-open scale, phone wake and the remaining eye/blink behaviour are not newly accepted by this report.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Do not replace that rollback with this partially successful candidate. No automatic installs/grants, Windows sync or unattended monitoring.
