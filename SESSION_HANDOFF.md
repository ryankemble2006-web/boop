# BOOP Wall Native Chat + eye hue handoff — 2026-09-07

Owner: isolated combined Wall candidate on `boop-wall-native-chat-eye-hue`.
Package: `com.boop.alpha1`.
Current candidate: versionCode 40 / `0.4.20-wall-landscape-eye-match`.
Baseline requested by Ryan for this tweak: `BOOP-Wall-v39-Sleepy-Close-signed.apk`.

## Current physical result

Ryan installed/tested the v38 local eye-colour intent and reported it was **literally perfect**. Preserve that local intent, slider UX, hue persistence and shared eye-render path.

v39 sleepy-close remains the direct functional baseline for v40. v40 does not alter the sleepy-close, Native Chat, wake, blink, thinking, shake, Member Berry, hue, hitboxes/gestures, or Launcher swipe behavior.

## v40 landscape eye geometry tweak

Ryan reported that the horizontal/landscape eyes did not visually match portrait because the eye gap looked strange.

Root cause: `BoopEyeLayout` enlarged each landscape eye by `LANDSCAPE_EYE_SCALE = 1.20f` but left the centre-to-centre eye spacing at the unscaled portrait value. That changed the face proportions in landscape.

v40 changes only that relationship:
- portrait code path is untouched;
- landscape eye width remains 1.20x the portrait-derived size;
- landscape eye height remains 1.20x the portrait-derived size;
- landscape eye centre distance now also scales by the same 1.20x;
- the entire eye pair therefore preserves the portrait eye-size/spacing proportions while fitting the wider canvas.

## Verification

Production/layout change commit: `8c0d46db4229beae86c19d733bb024510dc05c97`.
Version bump commit: `4a76570b1d28facdaa15d5b6ee781b4e5b26efb3`.
Focused source-test commit: `4f86a7f6f193731a8b493d8cc96fbd99ed020919`.
Workflow update commit: `1c652f92a47d744636c3593a8873271d2fc5c9fe`.
Updated Android layout-contract test commit: `c1bcdb1b2bc48482148f923514c17f73ca2668bd`.

GitHub Actions run `34098623797` completed successfully.
Passed gates include focused source guards, sleepy-close/hue/Chat/shake harnesses, materialization, effective integration checks, Android unit tests including the new landscape geometry contract, permanent signer setup, signed v40 build, exact package/version inspection, signer continuity and artifact upload.

Artifact: `BOOP-Wall-v40-Landscape-Eye-Match`.
Artifact ID: `10009632722`.
Extracted APK SHA-256: `2c5cf84722a05981eb80928cb4e7a835e3d8818fb2999b4c44c926778f336892`.

CI green is not physical green for the new landscape look. Physical check: install v40 over v39, rotate to portrait and landscape, and confirm the horizontal eye size/gap now reads as the same face geometry as portrait. The protected physical Wall checkpoint remains unchanged until Ryan explicitly promotes a newer candidate.
