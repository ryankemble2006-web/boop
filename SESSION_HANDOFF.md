# BOOP unified handoff

Updated 2026-09-08. Canonical app branch `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged. Fresh main owns shared contracts; this file owns implementation/evidence. Earlier investigation detail remains in this file at commit `438c3076875a56338ef26bd430f744f0a0cace32`.

## Latest user instructions and correction

Ryan approved the final `glossy_cartoon_eyes_with_black_eyelids.png` as BOOP's permanent default. All animations must use this same form; accessories come later as additions. He has downloaded a backup and now explicitly requests replacing the base artwork on BOTH phone/Wall and Shield. Read `BOOP_EYES_MASTER.md` for the exact master identity and boundaries. This explicit approval supersedes the old bitmap-source lock for this replacement, not the ban on future unapproved redesigns.

**Blink is working, user-confirmed.** Ryan says he had turned it off himself. Do not continue treating blink as a code defect or change its timing, curve, delay, lifecycle or accessibility/system-animation gates. This correction supersedes the earlier missing-blink investigation below.

HA device names and Home control buttons remain physically accepted. Keep that working code unchanged. Phone iris-only colour behaviour, existing headphones/puppetry, hands, wake ownership, room-switch lifecycle and idempotent Shield density remain protected.

## Current task: asset replacement pending transfer, no new APK

Exact approved master: 1774 x 887 RGBA, 936803 bytes, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. Local hash/format/alpha range were checked, not automated appearance acceptance.

The PNG is in the chat sandbox but is NOT yet committed to GitHub or integrated into phone/Shield. Do not mistake the approved preview, a blob of documentation, a Canva thumbnail or these notes for a binary upload. The GitHub connector's text/API writes do not expose a mounted-file upload argument. This container's direct GitHub request failed DNS resolution; neither the Windows checkout nor its WSL mount is mounted here. Connected GitHub reads/writes still work. A Canva upload attempt saved a copy there but supplied no original-file download route; it did not solve GitHub transfer.

Ryan has been asked to add his saved PNG to the ROOT of `boop-unified`, retaining its existing filename. Next safe step: fetch/check live unified and main again, locate the uploaded file, validate exact hash, store the canonical master under `unified/assets/boop-eyes/`, then wire both bodies to it. Preserve supplied alpha, update source rectangles for this new 2:1 image and preserve its proportions. Remove the rejected brightness-derived alpha conversion from the active path rather than trying another threshold. No old-art APK should be offered as this replacement.

Only documentation is published in this checkpoint. App code, images in the APK, workflows, signing, permissions and device settings remain unchanged. No build or fresh functional-test pass is claimed. No Windows sync, automatic installs/grants or background monitoring.

## Physical state of the last delivered candidate

Ryan tested code `949f1085328a3e815d9bc57747425f1f930c48db`:

- HA names no longer show `null`; Home buttons control his devices. PASS, preserve.
- The old delivered eye rendering is rejected: private photo showed comb-like tearing at inner upper eyelid edges. Replacement with the newly approved master is pending.
- Blink is now confirmed working after Ryan's settings correction. No code fix is needed.
- Android still reports `Assistant choice was not changed`. Default selection, remote-button activation and actual remote-microphone audio remain unaccepted.
- Phone acoustic wake has no new retest; its earlier unresolved failure is not resolved by a bitmap change. Room-switching scenarios and repeated-open scale have no new physical acceptance.

Overall candidate remains only partially physically accepted; do not replace the protected rollback or label the APK all-fixed.

## Existing investigation findings to retain

`scripts/make-locked-eyes-transparent.py` derives independent row spans from RGB brightness threshold 3 plus two-pixel expansion. This is not an approved silhouette and can cut dark eyelid pixels. Do not reuse it on the new master.

The shipped `scripts/patch-unified-assistant-button.py` service metadata omits `android:recognitionService`. The previous investigation identified this as an Android 11/12 service-parsing defect, not evidence that Shield firmware blocks every official route. Adding ACTION_ASSIST eligibility did not establish a valid service/default selection. Keep this separate from the bitmap-only task. Do not invent a dummy recognizer or competing microphone stack. Primary references retained from that investigation:

- https://android.googlesource.com/platform/prebuilts/fullsdk/sources/android-30/+/refs/heads/androidx-sharetarget-release/android/service/voice/VoiceInteractionServiceInfo.java
- https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/services/voiceinteraction/java/com/android/server/voiceinteraction/VoiceInteractionManagerService.java

## Last delivered signed artifact, historical evidence

Code: `949f1085328a3e815d9bc57747425f1f930c48db`.
Version: 45 / `1.1.2-unified-assist-repair`.
Successful run: `34201200463`; artifact `BOOP-Unified`, ID `10045928699`.
APK SHA-256: `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`.
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP digest: `sha256:393cf5b5f9263afed6566fe6ce8287e4043830e757bbbd112f09484695ce1c32`.

Historical non-visual checks: 58 Shield and 66 unified focused tests, zero failures/errors/skips; non-visual contracts, Launcher lint, compilation, package/manifest presence, permanent signature and ZIP integrity. These results belong to that APK, not the new unintegrated master. No emulator install/launch, screenshot/golden check or aesthetic source-string acceptance ran.

## Protected contracts

HA discovery/membership remains read-only and fail-closed: rooms and confirmed physical devices only, one primary device card, no Favourites/helpers/diagnostics/config plumbing or whole-house fallback. Room changes dispose old dashboard/socket/controller/navigation state before storing/rebuilding; preserve D-pad.

BOOP is a permanent wake fallback; custom name is additional. Foreground wireless charging permits phone wake; undocked phone is tap-to-talk. Preserve coordinator-owned reload/re-arm and coordinator/controller/Sherpa/recording ownership, without competing listeners.

Assistant selection remains explicit and reversible via supported Android routes into existing one-shot ownership. No Google disable, silent defaults/grants, privileged/ADB hacks, Button Mapper, OpenAI API integration or microphone in the visual overlay. Local KEYCODE_ASSIST fallback requires real firmware delivery evidence. Success requires invocation AND audio from THAT remote, local media/HA handling, response, clean recording/cancel/repeat and previous-app return.

Protected physical rollback: `e746affbb82b577cef2f1cf6e731dff186c8f881`. Keep private photographs, credentials and raw diagnostics out of this public repository.
