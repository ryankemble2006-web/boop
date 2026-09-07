# BOOP Wall Native Chat + Eye Hue Memory

Updated 2026-09-07.

This file exists to prevent future sessions from repeating the v31/v34 lineage mistake and the failed two-eye summon experiment.

The user's current Wall lineage is Native Chat. Eye-colour builds for this device must continue from that lineage, preserve package `com.boop.alpha1`, use a monotonically higher versionCode, and keep the permanent BOOP signer. The old `boop-wall-eye-hue-wip` v31 build is historical only.

Current combined candidate branch: `boop-wall-native-chat-eye-hue`.
Candidate version: versionCode 37 / `0.4.17-wall-eye-hue-voice`.
Build commit: `07c6751afdbdb50ccf96a9b1809d29c68ffdf2f7`.
Green workflow run: `34093159649`.
Artifact ID: `10007619095`.
APK SHA-256: `31aba85aef107940eeac68d574d7a16b28ae959a79925cb68bc9bd53497ba248`.

Hue contract:
- exactly one hue-only slider, not part of Voice Settings;
- summon by saying `change eye colour` (UK) or `change eye color` (US); the v36 two-eye one-second gesture physically failed and is superseded/removed;
- slider appears underneath the visible eyes for live preview and dismisses when the user taps outside it;
- 0..359 full hue range;
- default 190 degrees represents the accepted cyan/blue and deliberately applies no ColorFilter;
- persistence uses SharedPreferences `boop_eyes` / `hue_degrees`;
- both eyes reuse the exact existing `boop_eyes` bitmap and shared Paint;
- do not change eye geometry/crops/layout/animations/hitboxes, black background, voice behavior outside this local intent, Native Chat, wake behavior, Member Berry, thinking, shake or Launcher swipe;
- no mouth, replacement artwork, RGB channels, brightness, saturation, opacity, effects or themes.

Concurrency note: preserve the earlier concurrent hue implementation on `boop-wall-free-chat-wip@36e3199`; the combined branch already reconciles that history in ancestry. Do not blindly stack duplicate hue implementations.

CI green is not physical green. The protected physical Wall checkpoint remains unchanged until v37 passes the physical voice-summon/colour/persistence/regression checklist.
