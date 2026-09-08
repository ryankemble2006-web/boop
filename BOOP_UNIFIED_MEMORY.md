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

Approved route is Android's official assistant integration first: explicit reversible user choice, `RoleManager.ROLE_ASSISTANT` where available, `VoiceInteractionService` / session service only as required, or `ACTION_ASSIST` into BOOP's existing one-shot voice path. No microphone capture in the overlay and no second recorder/recognizer stack.

BOOP must never silently disable Google, grant permissions, change the default assistant or claim remote-mic success. `Keep my current assistant` leaves the current assistant alone; if BOOP is already selected, Android settings are opened for an explicit user change.

Do not add a local `KEYCODE_ASSIST` fallback until a real Shield proves firmware delivers that key to BOOP without privileged/ADB hacks. No Button Mapper/third-party app and no OpenAI API integration.

Physical success requires BOTH remote-button activation and actual audio from THAT Shield remote microphone, followed by BOOP's existing local media/HA routing, response, clean recording end/cancel/repeat handling and previous-app return where appropriate. Opening BOOP alone is not success.

## Latest real-device evidence: HA works; eyes/blink and assistant do not

On the repair APK from `949f1085328a3e815d9bc57747425f1f930c48db`, Ryan confirmed HA names no longer show `null` and Home buttons actually control his devices. This HA result is physically accepted; freeze this working path while repairing the remaining failures. It does not accept all room-switching scenarios or the whole APK.

Ryan rejects the new eye rendering as worse. His private photo shows horizontal comb-like tearing at the inner upper eyelid edges. No visible blink is reported. Android still says `Assistant choice was not changed`, so assistant takeover and remote-mic success must not be claimed. The latest message does not retest phone acoustic wake; preserve its unresolved earlier failure rather than inventing an outcome.

The alpha script guesses a separate opaque span on every image row from RGB brightness threshold 3 plus two-pixel expansion. This is not the locked silhouette and cannot distinguish black background from all dark eyelid pixels. Do not make another threshold-only repair, regenerate the art or modify the accepted phone iris-colour behaviour. An RGBA file header alone is not correctness evidence.

The declared VoiceInteractionService metadata omits `android:recognitionService`. AOSP Android 11/12 parsing rejects that omission; adding ACTION_ASSIST eligibility did not make the service valid. Treat it as an integration defect before blaming firmware. Do not invent a dummy recognizer or competing audio stack to satisfy a manifest field. Primary references and exact source paths are in `SESSION_HANDOFF.md`.

The Shield blink code checks system animator enablement/duration scale, power saving and display/visibility gates. Which gate blocks this physical device is not established. Check the actual animation setting, including any Turbo change, rather than silently enabling system animations or bypassing safeguards.

## Artifact provenance and verification limits

Repair code `949f1085328a3e815d9bc57747425f1f930c48db`, versionCode 45 / `1.1.2-unified-assist-repair`; successful run `34201200463`; artifact `BOOP-Unified` ID `10045928699`. Extracted APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Historical build evidence: 58 Shield focused tests and 66 unified wake/routing/assistant tests, zero failures/errors/skips; Launcher lint; compilation; package/manifest/signature/archive checks. The APK was rehashed during this investigation and matched its receipt. No new app repair/build or fresh functional test pass is claimed. Documentation only records the physical retest and diagnosed defects. The overall candidate is not an accepted rollback.

Earlier code `6dab12aa3232e821fed52b64e39f65e499b6c574` had null HA labels, failed phone wake, opaque eyes/no blink and failed assistant choice. JSON null repair is now physically confirmed; the other fixes did not gain blanket acceptance. Existing coordinator reload/re-arm, permanent BOOP fallback, room-switch lifecycle, idempotent scale and approved puppetry must survive subsequent work.

Protected accepted rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic device installs/grants, Windows synchronization or unattended monitoring. User photos and raw diagnostics stay private.
