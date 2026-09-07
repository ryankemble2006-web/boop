# BOOP Wall Native Chat + Eye Hue Memory

Updated 2026-09-07.

The current Wall lineage is Native Chat on `boop-wall-native-chat-eye-hue`. Preserve package `com.boop.alpha1`, monotonically increasing versionCode and the permanent BOOP signer. The historical v31 hue branch is not an install candidate for this device.

## Eye-colour memory

- v36 two-eye one-second summon physically failed and is superseded/removed.
- v37 exact phrase matcher physically failed because wake-word prefixes/recognizer wording could fall through to Free Chat/assistant handling.
- v38 fixed this by routing eye colour like Voice Settings: tolerant local intent checked before HA/OpenCode/Native Chat/Free Chat fallback.
- Ryan physically tested v38 and called the result **literally perfect**. Treat the v38 local eye-colour summon + slider UX as accepted behavior to preserve.
- Keep the single hue-only slider beneath visible eyes, live preview, outside-tap dismissal, persistence in `boop_eyes` / `hue_degrees`, 0..359 hue range, and default 190-degree cyan/blue with no ColorFilter.
- Both eyes continue to reuse the exact `boop_eyes` bitmap/shared Paint. No mouth, replacement artwork, RGB channels, brightness, saturation, opacity, themes or effects.

## Sleep animation memory

v39 / `0.4.19-wall-sleepy-close` is the current animation candidate.

Ryan asked to copy the gorgeous existing blink into the sleep sequence, then close the eyes slowly with sleepy charm. Implementation contract:
- first 183 ms uses exact `BoopIdleBlink.openness(...)` geometry;
- reopen fully after the blink;
- brief open settle;
- slow droop to ~52% half-lidded;
- short half-lid pause;
- slow eased final close to a 4% eyelid line;
- keep alpha at 1 through 92% of the animation, then fade gently to black;
- total duration ~1.24 seconds;
- preserve the existing wake animation unchanged and allow wake to cancel an in-progress sleep close safely;
- do not reintroduce the old 300 ms whole-view squash/fade sleep animation.

Build commit: `240a12869d8871ed4245e7476bec279f36dd75d7`.
Green workflow run: `34094925623`.
Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v39`.
Artifact ID: `10008263855`.
APK SHA-256: `195e02f914436fcdadfe4cd9fd570499e6f4afa67c394d1020f5dfd89c48b0ce`.

CI verified blink parity, slow-close timing, materialized sleep integration, existing hue/local-intent/Chat/Member Berry/thinking/shake paths, Native Chat/OpenAI relay markers, Android unit tests, package/version identity and signer continuity.

CI green is not physical green for v39. Ryan still needs to judge the actual sleep pacing/charm and wake-from-sleep on the Pixel. Do not overwrite the protected physical Wall checkpoint merely for this animation experiment.

Concurrency note: preserve the earlier concurrent hue implementation on `boop-wall-free-chat-wip@36e3199`; the combined branch already reconciles that history. Do not blindly stack duplicate hue implementations.
