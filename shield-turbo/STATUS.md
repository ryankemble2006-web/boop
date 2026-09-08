# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's real force-stop/read-back core remains physically accepted from earlier Shield testing.

The startup notice architecture is now physically proven too:
- v0.5.7 used the brightness-style transparent full-screen overlay host;
- Ryan saw the blue/cyan static notice for about one second;
- diagnostic: permission YES, `DISPLAY_WINDOW_CONTEXT`, add `ADDED`, present `FRAME_COMMITTED`, about 236ms.

v0.5.8 added timing evidence only and kept that notice architecture unchanged. Real-Shield timing:

`notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`

Therefore Turbo's CLEAN START job itself is sub-second. The earlier subjective ~10-second reboot disturbance is outside the timed job boundary. Do not alter the accepted stop/verify or overlay path to chase it without fresh evidence outside the job.

## Current candidate

**v0.5.8 / code 15**, exact built source `ea2c290b5ca66c6a88f1967db91b741e278007b7`.

Release run `34242340853`, job `102115431784`, conclusion **success**:
- **69 JVM tests passed**;
- **29 source/API/security contracts passed**;
- lint **0 errors / 24 warnings**;
- permanent signer/package/version/archive checks passed;
- nonvisual cold/warm launch/no-fatal smoke passed.

Artifacts:
- `SHIELD-TURBO` ID `10062615402`, ZIP `768599` bytes, SHA-256 `de9d9788992c65de8e665e23d850c97d59d6a9206b9046c8ba05f65bf975c468`;
- `SHIELD-TURBO-TESTS` ID `10062679313`, ZIP `94845` bytes, SHA-256 `1d36483400561023311e9f9823ae631ce699063117c8c26ca91063f345fd78f3`.

Delivered APK `Shield-Turbo-v0.5.8.apk`, `2337506` bytes, SHA-256 `e8d61d98fc5b4603c810246babbdb1c1937ae0942b2ea5aad0a8ce44e5fdcb66`.

Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`.

No visual tests ran. Ryan's physical evidence is authoritative.

## Locked decisions

Freeze:
- the v0.5.7 full-screen transparent notice host;
- static top-centre notice text and no-motion rules;
- 500ms presentation fail-open;
- trusted loopback ADB;
- force-stop + verification core;
- target safety exclusions;
- opt-in bounded 30/60/120s max-three scheduler;
- brightness 10-100% behavior;
- APPS direct launch and remote navigation behavior.

Display & Sound and Accessibility remain parked.

## Next step

No CLEAN START performance change is justified. Move on to other Turbo work unless Ryan explicitly wants the longer overall Shield reboot/launcher settle time investigated. If so, collect evidence outside `CleanStartJobService` first.
