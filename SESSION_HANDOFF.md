# BOOP unified handoff

Updated 2026-09-08. Canonical app branch `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged. Fresh main owns shared contracts; this file owns current unified implementation/evidence.

## Current signed v45 candidate

Code commit `6dab12aa3232e821fed52b64e39f65e499b6c574`, versionCode 45 / `1.1.2-unified-assist-repair`. GitHub Actions run `34198363929` completed successfully and uploaded artifact `BOOP-Unified` ID `10044846308`.

Extracted APK SHA-256: `77fe8d06223bdaa6a07e232baeb2ddb9162845e98e022477be559fb377915a6b`. Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Artifact ZIP digest reported by GitHub: `sha256:ad6a5cf4cb0f4bce1af98fa3deaac201f187c178db6d9388d1df1d2c46d79ade`.

Fresh non-visual verification: 57 Shield focused tests and 64 unified wake/routing/assistant tests, zero failures/errors/skips; non-visual contracts; Launcher lint; compilation; permanent signing; package identity; manifest component presence; APK ZIP integrity. GitHub performed no emulator install/launch, screenshot/golden test, appearance judgement or aesthetic source-string check. Physical Shield/Pixel acceptance remains Ryan-owned.

## Repairs included

- Shield scale is idempotent: activity opens derive the target density from the unmodified application baseline instead of repeatedly scaling the current density. This addresses the cumulative shrink source bug without system-wide density changes.
- Shield presentation materializes the exact locked phone/Wall `boop_eyes.png` and canonical `BoopEyeLayout` / `BoopIdleBlink` helpers. Same 183 ms blink curve and 3-7 second delay. No eye regeneration. Phone iris-only tint, headphones and existing puppetry are untouched.
- Home Assistant Home uses read-only area/device/entity registry relationships, including device-inherited area membership. Unconfirmed/loose entities fail closed; category/helper/diagnostic plumbing stays out; `RoomDeviceControls` collapses supported entities to one primary control per confirmed physical device. No Favourites and no whole-house fallback.
- Room changes now tear down previous-room navigation/dashboard/socket/controller state before storing the selected room and rebuilding Home, preventing stale previous-room cards while retaining existing D-pad view/navigation classes.
- Wake-name lifecycle keeps coordinator-owned reload/re-arm after changes. BOOP remains the permanent fallback and custom wake remains additional. Existing Sherpa stream/recording ownership is preserved; no competing listener was added. Acoustic wake still requires hardware verification.

## Shield remote microphone button

Approved official Android assistant route is implemented. On first Shield startup BOOP asks `Use BOOP for the microphone button` or `Keep my current assistant`; the choice is persisted and can be reopened from Shield Settings.

`Use BOOP` requests Android's assistant role through `RoleManager` where available. Android, not BOOP, owns the confirmation/default change. `Keep my current assistant` never silently changes the assistant; if BOOP is already selected it opens Android voice/assistant settings for the user to change explicitly.

The assistant entry is a lightweight `VoiceInteractionService` plus `VoiceInteractionSessionService`. Its session starts BOOP's existing `MainActivity` one-shot voice path via `ACTION_ASSIST`; it does not create another recorder or put microphone capture in the visual overlay. The assist input-device ID is forwarded/logged when Android supplies it. After BOOP's response, the one-shot activity finishes so the voice task can return to the prior app.

No `KEYCODE_ASSIST` fallback was added because there is no real-device evidence yet that Shield firmware delivers that key to BOOP without privileged/ADB tricks. No Google-disable hack, silent permission/default change, Button Mapper dependency or OpenAI API integration was added.

## Physical tests still required

Ryan must verify on Shield: remote mic button actually invokes BOOP; spoken audio actually arrives from THAT REMOTE'S microphone; media/HA command routing works; recording ends after response/cancel/repeated presses; previous-app return; BOOP and custom acoustic wake; exact Shield eyes/blink; repeated-open scale stability; room switching and device-only Home cards. Opening BOOP alone is not mic-button success.

If Shield firmware blocks assistant activation or remote-mic routing, record the exact firmware behaviour and user-authorised setup required. Do not substitute the Shield box mic or another microphone and call it working.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Earlier v44 evidence remains in Git history. No automatic user-device installation, grants, Windows sync or unattended monitoring is claimed.
