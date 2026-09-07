# BOOP Wall Native Chat + eye hue handoff — 2026-09-07

Owner: isolated combined Wall candidate on `boop-wall-native-chat-eye-hue`.
Package: `com.boop.alpha1`.
Candidate version: versionCode 39 / `0.4.19-wall-sleepy-close`.

## Current physical result

Ryan installed/tested the v38 local eye-colour intent and reported it was **literally perfect**. That confirms the eye-colour summon/slider UX itself on-device: tolerant local command routing works, the control opens locally instead of Free Chat, and the live hue UI is accepted. Do not regress this path.

This does not automatically promote the older protected Wall checkpoint or imply every unrelated v38 regression item was exhaustively retested.

## Eye-colour contract retained

Eye colour is not in Voice Settings. The v36 two-eye one-second gesture physically failed and is superseded. The first v37 voice implementation also physically failed because its matcher required an exact phrase.

v38+ treats eye colour like Voice Settings: a tolerant local intent checked at the start of `handleRecognizedSpeech(...)`, before Home Assistant, OpenCode, Native Chat or Free Chat routing. It accepts UK/US colour wording, wake-word prefixes, plural eyes, `eye hue`, and the narrow recognizer `I color/colour` homophone.

The single hue slider appears underneath the visible eyes, updates both eyes live, dismisses on outside tap, and persists in SharedPreferences `boop_eyes` / `hue_degrees`. The accepted cyan/blue default is 190 degrees and applies no ColorFilter.

## v39 sleep animation tweak

Ryan asked to replace the old sleep animation with the existing idle blink language, then close the eyes slowly with sleepy charm.

v39 changes only the sleep puppetry:
- the first 183 ms reuses the exact accepted `BoopIdleBlink.openness(...)` geometry;
- the eyes reopen fully after that blink;
- a brief open settle follows;
- the lids droop slowly to about half-open and pause there;
- a longer eased final close settles to a thin 4% eyelid line;
- alpha stays fully visible through 92% of the sequence, then fades gently to black at the end;
- total sleep animation duration is about 1.24 seconds;
- wake animation is unchanged and cancels any in-progress sleepy close safely.

The old 300 ms whole-face squash/fade sleep path is no longer used by the materialized v39 app. Eye artwork, geometry/cropping/render path, hue tint, black background, wake, idle blink, thinking, shake, Member Berry, hitboxes/gestures, Chat mode, Native Chat/OpenAI relay and Launcher swipe are otherwise preserved.

## Verification

GitHub Actions run `34094925623` completed successfully for build commit `240a12869d8871ed4245e7476bec279f36dd75d7`.

Passed gates include:
- focused source guards;
- exact blink-parity sleep harness;
- full reopen after the lead-in blink;
- half-lid pause and >1 second slow close timing;
- near-shut final eyelid line and late fade-to-black;
- materialized `BOOP_SLEEP_CHARM_V1` integration;
- existing eye hue/local-intent, Chat, Member Berry, thinking and shake harnesses;
- Native Chat/OpenAI relay markers and 33 wake mappings;
- Android unit tests;
- stable signed v39 build;
- package/version inspection and permanent BOOP signer continuity.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v39`, artifact ID `10008263855`.
Extracted APK SHA-256: `195e02f914436fcdadfe4cd9fd570499e6f4afa67c394d1020f5dfd89c48b0ce`.

CI green is not physical green for the new sleep animation. Physical acceptance still needs: install v39 over v38, allow/tell BOOP to sleep, judge the blink -> drowsy droop -> slow close timing, then wake it again and confirm wake remains correct. Also spot-check the already-accepted eye-colour command plus Native Chat/Member Berry/thinking/shake as convenient.

The protected physical Wall checkpoint remains unchanged until Ryan explicitly promotes a newer candidate.
