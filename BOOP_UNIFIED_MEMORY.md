# BOOP unified memory

Updated 2026-09-08. Canonical branch `boop-unified`, package `com.boop.alpha1`, permanent signer. Historical/primary checkouts are not automatically current app source.

## Durable decisions

- BOOP forever. One additional spoken wake name may be stored; BOOP fallback remains permanently accepted. Foreground wireless charging may listen continuously; undocked phone remains tap-to-talk. Preserve coordinator/controller/Sherpa/recording ownership and never add competing microphone listeners.
- Shield Home = Room -> real controllable physical devices. No Favourites, helper/diagnostic/config plumbing or loose entities. Use HA target + device registry + entity registry, including device-inherited room membership. Fail closed when physical-device/room identity is uncertain. HA membership is read-only.
- Changing room makes the new room authoritative immediately: dispose old navigation/dashboard/socket/controller state, store the room, then rebuild Home. Preserve D-pad behaviour.
- Locked phone/Wall eye bitmap and accepted landscape proportions are Shield's reference. No regeneration. Reuse canonical `BoopEyeLayout` / `BoopIdleBlink`; blink is 183 ms with the same curve and 3-7 second delay. Phone iris-only colour, whites/pupils/reflections/outline/default blue, headphones and locked puppetry remain unchanged.
- Shield UI density scaling must be idempotent and derived from an unmodified application baseline. Never repeatedly scale current density and never modify system-wide Shield density/resolution.
- Unified CI performs focused functional tests, compilation/lint, package/signature/integrity/security checks and artifact upload only. Ryan owns screenshots/appearance/animation, emulator/device launch/install and all physical acceptance. Do not reintroduce aesthetic source-string guards.

## Shield remote microphone button decision

Approved route is Android's official assistant integration first: explicit reversible user choice, `RoleManager.ROLE_ASSISTANT` where available, `VoiceInteractionService` / session service, then `ACTION_ASSIST` into BOOP's existing one-shot voice path. No microphone capture in the overlay and no second recorder/recognizer stack.

BOOP must never silently disable Google, grant permissions, change the default assistant or claim remote-mic success. `Keep my current assistant` leaves the current assistant alone; if BOOP is already selected, Android settings are opened for an explicit user change.

Do not add a local `KEYCODE_ASSIST` fallback until a real Shield proves firmware delivers that key to BOOP without privileged/ADB hacks. No Button Mapper/third-party app and no OpenAI API integration.

Physical success requires BOTH remote-button activation and actual audio from THAT Shield remote microphone, followed by BOOP's existing local media/HA routing, response, clean recording end/cancel/repeat handling and previous-app return where appropriate. Opening BOOP alone is not success.

## Current verified candidate

v45 code `6dab12aa3232e821fed52b64e39f65e499b6c574`, version `1.1.2-unified-assist-repair`, run `34198363929`, artifact `BOOP-Unified` ID `10044846308`. Extracted APK SHA-256 `77fe8d06223bdaa6a07e232baeb2ddb9162845e98e022477be559fb377915a6b`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh non-visual evidence: 57 Shield focused tests and 64 unified wake/routing/assistant tests, zero failures/errors/skips; Launcher lint; compilation; package/manifest/signature/archive checks. Physical wake, remote mic, eyes/blink, room UI and repeated-open scale remain unaccepted until Ryan tests hardware.

Protected accepted rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic device install/grants, Windows synchronization or unattended monitoring was established.
