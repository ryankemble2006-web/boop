# BOOP Alpha 1

First physical BOOP face for the spare Pixel 7 Pro.

## Alpha 1 contract

- True-black full-screen BOOP eyes.
- No menus or settings screens.
- Tap anywhere on the face to speak.
- Uses Android on-device speech recognition when available, with the normal recognizer as fallback.
- BOOP repeats the best transcript aloud: `You said, ...`.
- Tap again while listening to cancel.
- Keeps the screen awake while the face is open.
- No Home Assistant token is stored in this build.
- No Home Assistant actions are executed in this build.

That last pair is intentional. Alpha 1 proves the puppet interaction before we add permissions to touch the house.

## Next baby step: Alpha 2

1. Home Assistant instance discovery / address entry.
2. Proper Home Assistant OAuth login and refresh-token storage.
3. Send the Alpha 1 transcript to `POST /api/conversation/process`.
4. Speak Home Assistant's answer back through BOOP.
5. Keep ordinary controls immediate; require a plain-English confirmation before creating, changing, or deleting an automation.

## Build

The project is intentionally dependency-light: Java + Android platform APIs only.

Current project settings:

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- compileSdk / targetSdk 37
- minSdk 29
- Java 17

A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`. On a repository push or manual workflow run it builds an installable debug APK and publishes it as the `BOOP-Alpha1-debug` artifact.
