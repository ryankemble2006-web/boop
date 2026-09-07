# BOOP Wall v37: Native Chat + voice eye hue candidate

Updated 2026-09-07. Owning branch `boop-wall-native-chat-eye-hue`.

## Current candidate

- VersionCode 37 / `0.4.17-wall-eye-hue-voice`, package `com.boop.alpha1`.
- Built from the user's Native Chat lineage with permanent BOOP signer continuity.
- Eye colour is not in Voice Settings.
- Voice command `change eye colour` opens the single hue slider underneath the visible eyes; `color` is also accepted.
- Tap outside the slider dismisses it.
- Default cyan/blue remains 190 degrees with no ColorFilter; non-default hues tint the existing shared `boop_eyes` Paint.
- Existing artwork, geometry, black background, animations, Native Chat/OpenAI relay, wake path, Chat-mode hold, Member Berry, thinking, shake and Launcher swipe remain preserved.

## Verification evidence

GitHub Actions run `34093159649` completed successfully for build commit `07c6751afdbdb50ccf96a9b1809d29c68ffdf2f7`.

Passed gates include focused source guards, hue math/default-path checks, preserved Chat/Member Berry/thinking/shake harnesses, materialized Native Chat/OpenAI relay verification, removal of the failed two-eye hue trigger, Android unit tests, signed v37 build, exact package/version inspection, archive integrity and stable signer continuity.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v37` / ID `10007619095`.
Extracted APK SHA-256: `31aba85aef107940eeac68d574d7a16b28ae959a79925cb68bc9bd53497ba248`.

## Physical status

v36 two-eye gesture: physically failed to summon the control and is superseded.

v37 physical acceptance pending. Test voice summon, live hue changes, outside-tap dismissal, persistence after restart, then wake/sleep, Chat mode, tap-to-speak, thinking, shake, Member Berry, Launcher swipe and Native Chat.

Accepted physical Wall checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`; do not overwrite it until physical acceptance.
