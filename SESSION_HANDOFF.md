# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

This is the standalone Nvidia Shield clean-HOME validation app, not unified/AIO yet.

- Package: `com.boop.shieldhome`
- Unified/AIO package `com.boop.alpha1` is separate and untouched.
- Stock Android TV Home remains installed/enabled as recovery and as the Accessibility override trigger.
- No ADB, developer-options, root or Shizuku requirement for normal users.
- Do not merge into unified until Ryan explicitly approves the standalone result after physical Shield testing.

## Protected physical baseline

The protected HOME mechanism remains version 8 / `0.8.0-reboot-rearm`, build `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`.

Physically confirmed on the real Shield and not to be disturbed by media work:

- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override survives reboot;
- stock Android TV Home remains installed/enabled for recovery/trigger;
- banners and grab/reorder work.

Visual baseline remains the physically-good 0.9.4 HOME geometry/chrome plus the 0.9.5 stronger focus-pop candidate. Preserve the accepted floating-square Apps drawer, fixed HOME lanes/labels, no black HOME focus plate, no normal favourite stars and artwork-only focus scaling. Ryan owns visual acceptance; GitHub must not run screenshot/golden/layout/animation judging.

## Now Playing baseline: 0.10.0

0.10.0 introduced generic Android MediaSession Now Playing, Launcher Settings media-access/player controls, and the independent approved headphones BOOP layer. Build `f06cee260c98b2b03ddaa67ed19e505078bf3ac1`, workflow `34277141969`, artifact `10076198619`, APK SHA-256 `69cdf3d136c004c2cfb7ad377f8533a2992cb383eeb82b7926c8284e1985cc88`.

Notification Listener special access is used only as Android's supported authority for querying active media sessions. Notification posted/removed payloads are intentionally ignored. The approved `boop_headphones.png` is reused unchanged; its launcher-owned layer is non-focusable/non-clickable.

## Current candidate: 0.10.1 media-access route hotfix

Ryan physically tested 0.10.0 and reported one scoped fault: selecting **Media access: OFF** opened general Shield Settings instead of the Android TV Notification Access page.

Root cause: on API 30+ the launcher attempted the phone-style per-listener detail action before Android TV's generic Notification Listener settings action. Shield accepted that detail intent into the wrong Settings surface, so the generic TV route was never attempted.

The fix is deliberately tiny:

- `NowPlayingAccessSettingsPlan.routesForSdk(30+)` now orders `GENERIC` before `DETAIL`;
- older Android remains `GENERIC` only;
- `DETAIL` remains the modern fallback if the generic route is unavailable;
- no UI/layout/artwork/animation source changed;
- no permissions, HOME override behavior, media-session logic or launcher visuals changed.

TDD evidence:

- RED commit `65385a8078d8a734b7556514091077e4eeaed566` changed the contract test to require `GENERIC, DETAIL`; workflow `34278118524` failed exactly one test: 79 tests / 1 failure, the new route-order assertion.
- GREEN production fix commit `5c549474c9f87400a5a55a94eecb7a6288ff27dd` swapped only the route order.
- Release version commit `13c695da9ab48f7374d64eb2f6c1342496f982a4` set versionCode 16 / `0.10.1-media-access-route`.
- Final verified build head: `df4445e6a0c6488355002d5ed99ebfb88ca4e9c1`.
- Workflow: `34278312090` SUCCESS.
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10076663643`.
- APK SHA-256: `1f8f9f82871ca80f6e5496a3047068171042edfdc3502f829faa3ebc2fe31ff5`.
- Artifact ZIP SHA-256: `7c54e96b4d9ca5de4e6690f68cccff87b4ee70499631467aa52f02986bb8d9a3`.
- Permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Final workflow verification passed:

- all 79 focused standalone launcher tests;
- signed assembly;
- exact package `com.boop.shieldhome`;
- exact versionCode 16 / `0.10.1-media-access-route`;
- HOME/Leanback entries and protected Accessibility service/router presence;
- Now Playing listener service and packaged headphones resource presence;
- permanent signer match;
- APK archive integrity and artifact upload.

No screenshot, golden-image, appearance, layout or animation acceptance was run. The downloaded APK was independently unpacked and its SHA-256 matched the CI receipt exactly.

**0.10.1 is CI/signer/package green. The corrected Settings destination still requires Ryan's real-Shield confirmation.**

## Next physical check

Install/update to 0.10.1 and test only the reported fault first:

1. Launcher Settings -> **Media access: OFF**.
2. It should open Android TV's Notification Access / Notification Listener special-access page rather than general Shield Settings.
3. Enable BOOP Now Playing there and return to Launcher Settings; `Media access` should report ON.

After that, continue the existing 0.10 Now Playing physical tests. Do not infer visual acceptance from CI and do not merge into unified until Ryan explicitly approves it.
