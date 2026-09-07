# BOOP Wall Native Chat + eye hue handoff — 2026-09-07

Owner: isolated combined Wall candidate on `boop-wall-native-chat-eye-hue`.
Package: `com.boop.alpha1`.
Candidate version: versionCode 37 / `0.4.17-wall-eye-hue-voice`.

## Current UX

Eye colour is not in Voice Settings. Ryan physically reported that the v36 two-eye one-second gesture did not summon the control, so that trigger is superseded and removed from the materialized code.

Say **“change eye colour”** to summon the hue slider underneath the eyes. UK and US `colour/color` spellings are accepted. The eyes remain visible and update live while the slider moves. Tap anywhere outside the slider area to dismiss it.

Hue remains 0..359 degrees. The accepted cyan/blue default is 190 degrees and deliberately applies no ColorFilter. Non-default values hue-rotate the existing shared `boop_eyes` Paint, so both eyes use the original artwork/geometry/render path. Hue persists in SharedPreferences `boop_eyes` / `hue_degrees`.

Native Chat/OpenAI relay, browser/free-chat mode, wake path, idle blink, thinking, shake, Member Berry, Chat-mode hold, caller-owned Wall -> Launcher transition, black background, package and permanent BOOP signer are preserved. No mouth or extra visual controls were added.

## Verification

GitHub Actions run `34093159649` built the v37 candidate successfully from `07c6751afdbdb50ccf96a9b1809d29c68ffdf2f7`. Focused source guards, hue/Chat/shake JVM harnesses, materialization, effective Native Chat/OpenAI integration checks, Android unit tests, signed build, package/version inspection and permanent signer continuity all passed.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v37`, artifact ID `10007619095`.
Extracted APK SHA-256: `31aba85aef107940eeac68d574d7a16b28ae959a79925cb68bc9bd53497ba248`.

CI green is not physical green. Physical acceptance still needs: install over the current Wall build; say “change eye colour”; verify slider placement below visible eyes; live colour changes; outside-tap dismissal; persisted colour after restart; then regression-check wake/sleep, Chat-mode hold, tap-to-speak, thinking, shake, Member Berry, Launcher swipe and Native Chat conversation.

The protected physical Wall checkpoint remains unchanged until Ryan physically accepts the candidate.
