# BOOP Wall Native Chat + Eye Hue Memory

Updated 2026-09-07.

This file exists to prevent future sessions from repeating the v31/v34 lineage mistake.

The user is running BOOP Wall `0.4.14-wall-native-chat` / versionCode 34. Any eye-colour build intended to install over that app must continue from the Native Chat/Free Chat Wall lineage and use a versionCode greater than 34 with the existing BOOP signer. The older `boop-wall-eye-hue-wip` v31 experiment is useful implementation history but is not an install candidate for the current device.

Current combined candidate branch: `boop-wall-native-chat-eye-hue`.
Candidate version: versionCode 35 / `0.4.15-wall-native-chat-eye-hue`.
Build commit: `1256fb33f198659d7afd1310e5c8afbadd5d53d3`.
Green workflow run: `34090520672`.
Artifact ID: `10006695690`.
APK SHA-256: `013c4db3b3fc9e21eb2b4bf0a255bfbaf84d9b2c94a06c7a996cc875ff917819`.

Hue contract:
- one hue-only slider labelled `Eye colour` beside voice settings;
- 0..359 full hue range;
- default 190 degrees represents the accepted cyan/blue and deliberately applies no ColorFilter;
- persistence uses SharedPreferences `boop_eyes` / `hue_degrees`;
- both eyes reuse the exact existing `boop_eyes` bitmap and shared Paint;
- do not change eye geometry/crops/layout/animations/hitboxes/gestures, black background, voice behavior, Native Chat, wake behavior, Member Berry, thinking or shake behavior;
- no mouth, replacement artwork, RGB channels, brightness, saturation, opacity, effects or themes.

Concurrency note: while the combined candidate was being prepared, `boop-wall-free-chat-wip` independently advanced to `36e31998219c518e96730ff54e96b8e4fdf5b680` with the same hue concept embedded directly in `scripts/materialize-android.sh`. Preserve that commit. The combined branch reconciles it in ancestry rather than blindly stacking duplicate implementations and keeps the hue helpers/patch isolated for testing.

CI green is not physical green. The protected physical Wall checkpoint remains unchanged until the v35 APK installs over v34 and passes the physical colour/persistence/animation/gesture/Native Chat checklist.
