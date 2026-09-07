# BOOP Wall v38: Native Chat + local eye hue intent candidate

Updated 2026-09-07. Owning branch `boop-wall-native-chat-eye-hue`.

## Current candidate

- VersionCode 38 / `0.4.18-wall-eye-hue-local-intent`, package `com.boop.alpha1`.
- Continues the user's Native Chat lineage with permanent BOOP signer continuity.
- Eye colour is not in Voice Settings.
- `change eye colour` / `change eye color` is now a tolerant local intent checked before HA/OpenCode/Native Chat/Free Chat fallback, matching the routing style used by Voice Settings.
- Wake-word-prefixed `BOOP, change eye colour` is explicitly covered.
- The failed v36 two-eye gesture is removed. The too-strict v37 exact matcher is superseded.
- The single hue slider still appears beneath the visible eyes, previews live, dismisses on outside tap and persists the selected hue.
- Default cyan/blue remains 190 degrees with no ColorFilter; non-default hues tint the existing shared `boop_eyes` Paint.
- Existing artwork, geometry, black background, animations, Native Chat/OpenAI relay, wake path, Chat-mode hold, Member Berry, thinking, shake and Launcher swipe remain preserved.

## Verification evidence

GitHub Actions run `34093926250` completed successfully for build commit `3c29d4b28f5430710d7b189a9cf10a2929ca986d`.

Passed gates include the new local eye-colour intent harness, focused source guards, hue math/default path, preserved Chat/Member Berry/thinking/shake harnesses, materialized Native Chat/OpenAI relay verification, Android unit tests, signed v38 build, exact package/version inspection, archive integrity and stable signer continuity.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v38` / ID `10007905660`.
Extracted APK SHA-256: `4e82b828c3eafcc6f650b9e76bd5d27c34973845158bb174d70a4c68741ca28e`.

## Physical status

- v36 two-eye gesture: physically failed to summon the control; superseded.
- v37 exact voice matcher: physically failed. `BOOP change eye colour` opened Free Chat and tap-to-talk `change eye colour` fell through to assistant handling.
- v38 physical acceptance pending. Test both wake-word and tap-to-talk routes, then live hue, dismissal, persistence and the existing Wall regression checklist.

Accepted physical Wall checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`; do not overwrite it until physical acceptance.
