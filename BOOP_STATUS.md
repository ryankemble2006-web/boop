# BOOP Wall v40: Native Chat + eye hue + sleepy close + landscape eye match

Updated 2026-09-07. Owning branch `boop-wall-native-chat-eye-hue`.

## Current candidate

- VersionCode 40 / `0.4.20-wall-landscape-eye-match`, package `com.boop.alpha1`.
- Directly continues the v39 `BOOP-Wall-v39-Sleepy-Close-signed.apk` baseline requested by Ryan.
- v38 eye-colour local intent/slider UX remains physically accepted and unchanged.
- v39 sleepy-close behavior remains unchanged in v40.
- v40 changes only landscape eye geometry: the centre-to-centre spacing now scales by the same `LANDSCAPE_EYE_SCALE` as eye width/height, preserving portrait face proportions in landscape.
- Portrait layout path is unchanged.
- Native Chat/OpenAI relay, Chat-mode hold, hue persistence/rendering, wake, idle blink, sleepy close, Member Berry, thinking, shake, hitboxes/gestures and Launcher swipe are preserved.

## Verification evidence

GitHub Actions run `34098623797` completed successfully for build head `c1bcdb1b2bc48482148f923514c17f73ca2668bd`.

Passed: focused source guards, preserved Java harnesses, materialization, effective integration checks, Android unit tests including updated `BoopEyeLayoutTest`, permanent BOOP signer setup, signed v40 build, exact package/version inspection, signer continuity and artifact upload.

Artifact: `BOOP-Wall-v40-Landscape-Eye-Match` / ID `10009632722`.
APK SHA-256: `2c5cf84722a05981eb80928cb4e7a835e3d8818fb2999b4c44c926778f336892`.

## Physical status

- v38 eye-colour intent/slider: physically accepted by Ryan; preserve.
- v39 sleepy-close: CI green, physical acceptance not promoted in this record.
- v40 landscape eye match: CI green, physical acceptance pending. Compare portrait and landscape and judge whether the horizontal eye gap/face proportions now match.

Accepted protected Wall checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`; do not overwrite it solely from CI.
