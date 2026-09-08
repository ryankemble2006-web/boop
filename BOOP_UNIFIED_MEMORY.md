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

## Latest real-device evidence and repair

Ryan physically tested the prior v45 candidate and found four failures: Android presented BOOP's assistant-choice UI but did not actually change the selected assistant; Shield Home rendered physical-device labels as literal `null`; neither BOOP nor the custom wake name triggered acoustically on phone; and the copied eye PNG was opaque/black-backed with a visible border and no visible blink. These results override the earlier physical-pending wording for that APK.

The current repair code commit is `949f1085328a3e815d9bc57747425f1f930c48db`, still package `com.boop.alpha1` with the permanent signer. Green run `34201200463`, artifact `BOOP-Unified` ID `10045928699`, extracted APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Repairs: HA JSON null values no longer become the text `null`, so device names can fall back to the real registry name; Sherpa wake arming is no longer vetoed by advisory `SpeechRecognizer.checkRecognitionSupport()` false negatives, with the real recognition attempt/error path retained as authority; the locked eye RGB pixels are not regenerated or recoloured but are packaged with an alpha silhouette so the PNG is actually transparent outside the eyes; and assistant eligibility now includes both the existing `VoiceInteractionService` path and explicit `ACTION_ASSIST` eligibility. No second microphone stack, silent default change, Google disable or privileged fallback was added.

Fresh non-visual evidence: 58 Shield focused tests and 66 unified wake/routing/assistant tests, zero failures/errors/skips; Launcher lint; compilation; package/manifest/signature/archive checks. The packaged eye asset is structurally RGBA with real alpha, but appearance/blink remain Ryan-owned physical acceptance.

Current physical retest still required: actual assistant takeover where Shield firmware permits; remote microphone button plus audio from that remote; BOOP/custom acoustic wake while wireless charging; correct transparent eye appearance and visible blink; real HA device names/room switching; repeated-open Shield scale stability.

Protected accepted rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic device install/grants, Windows synchronization or unattended monitoring was established.
