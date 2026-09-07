# BOOP Wall Native Chat + Eye Hue Memory

Updated 2026-09-07.

The current Wall lineage is Native Chat on `boop-wall-native-chat-eye-hue`. Preserve package `com.boop.alpha1`, monotonically increasing versionCode and the permanent BOOP signer.

## Eye-colour memory

- v38 tolerant local eye-colour intent + single hue slider was physically accepted by Ryan as **literally perfect**. Preserve it.
- Keep persistence in `boop_eyes` / `hue_degrees`, 0..359 hue range, default 190-degree cyan/blue with no ColorFilter, and the existing shared `boop_eyes` bitmap/Paint.
- No mouth, replacement artwork, RGB channels, brightness, saturation, opacity, themes or effects.

## Sleep memory

v39 / `0.4.19-wall-sleepy-close` remains the sleepy-close baseline used for v40. Preserve its accepted idle-blink language, reopen, half-lid droop/pause, slow final close, late fade and unchanged wake cancellation behavior.

## Landscape eye geometry memory

Ryan reported that horizontal eyes looked unlike portrait because the gap felt strange. Root cause in v39: landscape eye width/height were scaled by `LANDSCAPE_EYE_SCALE = 1.20f`, but eye centre distance was not. That compressed the visual gap ratio.

v40 / `0.4.20-wall-landscape-eye-match` fixes only this geometry contract:
- portrait path unchanged;
- landscape width = portrait-derived width × 1.20;
- landscape height = portrait-derived height × 1.20;
- landscape centre distance = portrait-derived centre distance × 1.20;
- therefore eye size and spacing scale together and preserve the portrait face proportions.

Green build head: `c1bcdb1b2bc48482148f923514c17f73ca2668bd`.
Workflow run: `34098623797`.
Artifact: `BOOP-Wall-v40-Landscape-Eye-Match`.
Artifact ID: `10009632722`.
APK SHA-256: `2c5cf84722a05981eb80928cb4e7a835e3d8818fb2999b4c44c926778f336892`.

CI verified the new landscape geometry contract plus preserved hue, sleepy-close, Chat, Member Berry, thinking, shake, Native Chat/OpenAI materialization, Android unit tests, exact package/version identity and stable signer continuity.

CI green is not physical green. Ryan's portrait/landscape visual comparison is authoritative. Do not promote the protected checkpoint solely from this build.

Concurrency note: preserve the earlier concurrent hue implementation on `boop-wall-free-chat-wip@36e3199`; do not stack duplicate hue logic.
