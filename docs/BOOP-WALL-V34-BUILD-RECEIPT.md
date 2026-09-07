# BOOP Wall Native Chat v34: exact setup-candidate build receipt

Updated 2026-09-07. Variant: **boop-relay-reviewed-v34**, NOT the concurrently
implemented boop-wall-free-chat-wip variant. Use matching Worker source.

## Provenance

- App/build commit: `453bda9ec5bcd12f44fe50d63c7db5ce7a718295`.
- Application: `com.boop.alpha1`; versionCode 34; `0.4.14-wall-native-chat`.
- Variant: stable-signed debug, not an optimized release.
- Full successful GitHub run: `34084002048`; job: `101624466469`.
- Artifact: `10004727824` / `BOOP-Wall-Free-Chat-candidate`.
- Delivered APK: `BOOP-Wall-v34-Native-Chat-setup.apk`.
- APK bytes: 139485306.
- APK SHA-256: `1bb448d6f458cf4b527a69f7137f65ef3dd5a1a1da7dfab958b5cfe8efaeba97`.
- Artifact ZIP SHA-256: `69c6e02d249f17ba61c1dda6050c150ebbeaa484e028fb5c1cee77d006f4cbcf`.
- Materialized MainActivity SHA-256: `caf5a22aa9fba5f5b9d4b9d09f71068c187f0b58ff5bad9d9da3a765625dce86`.
- Existing signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Visual evidence artifact: `10004724807` / `BOOP-Wall-v34-polish-evidence`.
- Visual evidence ZIP SHA-256: `617606ff22c50841109963a5d5cf6af63857adaf80ecfdf4c1f65acba1581909`.
- Relay configuration receipt: **setup-required**. No live relay configuration.

The completed CI report was reread. The downloaded instrumentation report contains
both its PASS summary and the real raw `INSTRUMENTATION_CODE: -1`. The enlarged
notice and observed landscape blink images were also visually inspected. Source/bridge/JVM/Android unit tests, mock
Worker tests, materialization, package/version/archive and full apksigner checks,
real emulator microphone startup, three-mode menu/persistence/revert/cancellation,
visual notice/natural portrait and landscape blink/original sleep deadline/
background cancellation, and Shield pairing-return all passed on this run.
The exact artifact was downloaded; ZIP hash/integrity, built-commit receipt, APK
hash/archive and public signer receipt were independently checked after download.
Full APK cryptographic signature verification is CI evidence, not claimed as
an independently repeated local apksigner invocation.

## Test environment and boundaries

Final UI evidence uses a disposable Android 16 Pixel 7 Pro profile rendered at
720x1560 / 280 dpi instead of 1440x3120 / 560 dpi. Layout dp/aspect and normal
animation scale stay unchanged. This is not a physical Pixel test or a full-panel
frame-rate claim. The test observes scheduled natural blinks without invoking
runIdleBlink, forcing animation time or removing closed-frame assertions. Eye
images are live View renders at observed natural OnPreDraw phases; toast and sleep
images are UiAutomation screenshots.
177 source/Python/JVM tests and 13 mock Worker tests passed locally. CI installs
and passes the existing bridge dependencies and performs the actual Android build.

The Worker was NOT deployed. No real OpenAI reply, configured API account,
physical Pixel install/update, real-house command, or end-to-end native chat was
verified. Empty-config selection gives the setup message; Free Chat/OpenCode remain
available. Existing signing credentials, permissions and accepted checkpoints
are unchanged. Later documentation commits do not change these APK bytes.

## Matching Worker source bundle

`BOOP-Native-Chat-v34-matching-relay.zip` contains the five tracked relay files
and a source-commit receipt for the same application build commit. No credentials.
Bundle SHA-256: `7c73150b7893fe8d861c57581442df25269706436c058c11c3199ed28bd10652`.
This source bundle is not a Worker deployment.
