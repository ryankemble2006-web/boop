# BOOP Wall Native Chat + Eye Hue Memory

Updated 2026-09-07.

This file exists to prevent future sessions from repeating the v31/v34 lineage mistake, the failed two-eye summon experiment, or the too-strict v37 voice matcher.

The user's current Wall lineage is Native Chat. Eye-colour builds for this device must continue from that lineage, preserve package `com.boop.alpha1`, use a monotonically higher versionCode, and keep the permanent BOOP signer. The old `boop-wall-eye-hue-wip` v31 build is historical only.

Current combined candidate branch: `boop-wall-native-chat-eye-hue`.
Candidate version: versionCode 38 / `0.4.18-wall-eye-hue-local-intent`.
Build commit: `3c29d4b28f5430710d7b189a9cf10a2929ca986d`.
Green workflow run: `34093926250`.
Artifact ID: `10007905660`.
APK SHA-256: `4e82b828c3eafcc6f650b9e76bd5d27c34973845158bb174d70a4c68741ca28e`.

Hue contract:
- exactly one hue-only slider, not part of Voice Settings;
- the v36 two-eye one-second gesture physically failed and is superseded/removed;
- the first v37 voice matcher was too exact and physically failed: wake `BOOP change eye colour` could open Free Chat and tap-to-talk could fall through to assistant handling;
- v38 must handle eye-colour requests exactly like Voice Settings routing: tolerant local intent checked before HA/OpenCode/Native Chat/Free Chat fallback;
- accept UK/US `colour/color`, wake-word prefixes such as `BOOP, change eye colour`, plural eyes, eye hue, and narrow recognizer `I color/colour` homophones;
- slider appears underneath the visible eyes for live preview and dismisses when the user taps outside it;
- 0..359 full hue range;
- default 190 degrees represents the accepted cyan/blue and deliberately applies no ColorFilter;
- persistence uses SharedPreferences `boop_eyes` / `hue_degrees`;
- both eyes reuse the exact existing `boop_eyes` bitmap and shared Paint;
- do not change eye geometry/crops/layout/animations/hitboxes, black background, voice behavior outside this local intent, Native Chat, wake behavior, Member Berry, thinking, shake or Launcher swipe;
- no mouth, replacement artwork, RGB channels, brightness, saturation, opacity, effects or themes.

Concurrency note: preserve the earlier concurrent hue implementation on `boop-wall-free-chat-wip@36e3199`; the combined branch already reconciles that history in ancestry. Do not blindly stack duplicate hue implementations.

CI green is not physical green. The protected physical Wall checkpoint remains unchanged until v38 passes the physical local-intent/colour/persistence/regression checklist.
