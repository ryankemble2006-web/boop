# BOOP Wall v39: Native Chat + eye hue + sleepy close candidate

Updated 2026-09-07. Owning branch `boop-wall-native-chat-eye-hue`.

## Current candidate

- VersionCode 39 / `0.4.19-wall-sleepy-close`, package `com.boop.alpha1`.
- Continues the user's Native Chat lineage with permanent BOOP signer continuity.
- v38 eye-colour UX was physically reported by Ryan as **literally perfect**. Preserve its tolerant local command routing and slider behavior.
- Eye colour remains a local intent checked before HA/OpenCode/Native Chat/Free Chat fallback; slider remains beneath visible eyes, live-previewed, outside-tap dismissible and persistent.
- v39 changes only sleep puppetry.
- Sleep now begins with the exact accepted 183 ms idle blink geometry, fully reopens, pauses briefly, droops to half-lidded, pauses, then closes slowly to a thin eyelid line before a late fade to black.
- Total sleep sequence is ~1.24 s; wake animation is unchanged and safely cancels an in-progress sleep close.
- The old 300 ms whole-face squash/fade sleep animation is superseded in the materialized v39 build.
- Existing eye artwork, hue path, black background, wake, idle blink, Native Chat/OpenAI relay, Chat-mode hold, Member Berry, thinking, shake, hitboxes/gestures and Launcher swipe remain preserved.

## Verification evidence

GitHub Actions run `34094925623` completed successfully for build commit `240a12869d8871ed4245e7476bec279f36dd75d7`.

Passed gates include sleepy-close timing/blink-parity harness, focused source guards, preserved eye-colour local-intent/hue/Chat/Member Berry/thinking/shake harnesses, effective Native Chat/OpenAI materialization checks, Android unit tests, signed v39 build, exact package/version inspection, archive integrity and stable signer continuity.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v39` / ID `10008263855`.
Extracted APK SHA-256: `195e02f914436fcdadfe4cd9fd570499e6f4afa67c394d1020f5dfd89c48b0ce`.

## Physical status

- v36 two-eye eye-colour summon: physically failed; superseded.
- v37 exact eye-colour voice matcher: physically failed; superseded.
- v38 eye-colour local-intent/slider UX: physically reported perfect by Ryan; retain it.
- v39 sleepy-close animation: CI green, physical acceptance pending. Test sleep timing/charm and wake-from-sleep first.

Accepted protected Wall checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`; do not overwrite it solely from CI or this animation experiment.
