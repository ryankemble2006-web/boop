# BOOP Wall Native Chat + eye hue handoff — 2026-09-07

Owner: isolated combined Wall candidate on `boop-wall-native-chat-eye-hue`.
Package: `com.boop.alpha1`.
Candidate version: versionCode 36 / `0.4.16-wall-native-chat-eye-hue`.

## Current UX

Eye colour is no longer in Voice Settings. Hold both eyes simultaneously for 1 second to summon one hue-only slider underneath the eyes. The eyes remain visible and update live while the slider moves. Tap anywhere outside the slider area to dismiss it. The gesture consumes its release and is kept separate from the existing single-eye Chat-mode hold, tap-to-speak, Member Berry and Launcher swipe paths.

Hue remains 0..359 degrees. The accepted cyan/blue default is 190 degrees and deliberately applies no ColorFilter. Non-default values hue-rotate the existing shared `boop_eyes` Paint, so both eyes use the original artwork/geometry/render path. Hue persists in SharedPreferences `boop_eyes` / `hue_degrees`.

Native Chat/OpenAI relay, browser/free-chat mode, current wake path, idle blink, thinking, shake, Member Berry, caller-owned Wall -> Launcher transition, black background, package and permanent BOOP signer are preserved. No mouth or extra visual controls were added.

## Verification

GitHub Actions run `34091824054` built the v36 candidate. Source guards, hue/Chat/shake Java harnesses, materialization and effective integration checks, Android unit tests, stable signing, package/version inspection and signer continuity passed. Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v36`, artifact ID `10007135614`.

Extracted APK SHA-256: `f40099957c0ca95ad428559312a91f487486de37338fb9c2422409b55ed63ee1`.

CI green is not physical green. Physical acceptance still needs: install over current Native Chat build; verify two-eye 1s summon; slider placement below visible eyes; live colour changes; outside-tap dismissal; persisted colour after restart; single-eye Chat-mode hold; tap-to-speak; wake/sleep; thinking; shake; Member Berry; Launcher swipe; Native Chat conversation.

The protected physical Wall checkpoint remains unchanged until Ryan physically accepts the candidate.
