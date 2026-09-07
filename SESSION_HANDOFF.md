# BOOP Wall Native Chat + eye hue handoff — 2026-09-07

Owner: isolated combined Wall candidate on `boop-wall-native-chat-eye-hue`.
Package: `com.boop.alpha1`.
Candidate version: versionCode 38 / `0.4.18-wall-eye-hue-local-intent`.

## Current UX

Eye colour is not in Voice Settings. The v36 two-eye one-second gesture physically failed and is superseded. The first v37 voice implementation also physically failed because its matcher required an exact phrase, so `BOOP, change eye colour` and recognizer wording could fall through to Free Chat/assistant handling.

v38 fixes that by treating eye colour exactly like Voice Settings: a tolerant **local intent** checked at the start of `handleRecognizedSpeech(...)`, before Home Assistant, OpenCode, Native Chat or Free Chat routing. It accepts `change eye colour`, `change eye color`, wake-word-prefixed wording such as `BOOP, change eye colour`, plural eyes, `eye hue`, and a narrow recognizer homophone form where `eye` is heard as `I`.

When matched, BOOP opens the single hue slider underneath the visible eyes. The eyes update live while the slider moves. Tap anywhere outside the slider area to dismiss it.

Hue remains 0..359 degrees. The accepted cyan/blue default is 190 degrees and deliberately applies no ColorFilter. Non-default values hue-rotate the existing shared `boop_eyes` Paint, so both eyes use the original artwork/geometry/render path. Hue persists in SharedPreferences `boop_eyes` / `hue_degrees`.

Native Chat/OpenAI relay, browser/free-chat mode, wake path, idle blink, thinking, shake, Member Berry, Chat-mode hold, caller-owned Wall -> Launcher transition, black background, package and permanent BOOP signer are preserved. No mouth or extra visual controls were added.

## Verification

GitHub Actions run `34093926250` built the v38 candidate successfully from `3c29d4b28f5430710d7b189a9cf10a2929ca986d`.

The focused local-intent JVM harness explicitly passed `change eye colour`, `change eye color`, `BOOP, change eye colour`, natural wording, plural eyes, `eye hue`, and recognizer `I color`, while rejecting unrelated colour questions/house commands. Existing hue/Chat/shake harnesses, source guards, materialization, effective Native Chat/OpenAI integration checks, Android unit tests, signed build, package/version inspection and permanent signer continuity also passed.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v38`, artifact ID `10007905660`.
Extracted APK SHA-256: `4e82b828c3eafcc6f650b9e76bd5d27c34973845158bb174d70a4c68741ca28e`.

CI green is not physical green. Physical acceptance still needs: install v38 over the current Wall build; test tap-to-talk `change eye colour` and wake-word `BOOP, change eye colour`; verify neither route opens Free Chat; confirm slider placement below visible eyes, live colour changes, outside-tap dismissal and persistence after restart; then regression-check wake/sleep, Chat-mode hold, tap-to-speak, thinking, shake, Member Berry, Launcher swipe and Native Chat conversation.

The protected physical Wall checkpoint remains unchanged until Ryan physically accepts the candidate.
