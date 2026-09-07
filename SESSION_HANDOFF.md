# BOOP Wall v34: Native Chat + eye hue candidate

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Package: `com.boop.alpha1`. VersionCode 34 / `0.4.14-wall-native-chat`.

## Latest user evidence

Ryan reported Android rejected the previously supplied eye-hue APK as `App not installed`. That APK was v31 from the older Wall resurrection lineage and was therefore a downgrade from the current v34 Native Chat / ChatGPT-enabled Wall. Do not use the v31 eye-hue artifact on the current Wall.

The current Wall lineage is this branch. It contains the three-mode chat menu (OpenCode, ChatGPT, Free Chat), the existing Wall -> Launcher caller-owned animation, wake/voice/HA behavior, and the stable BOOP signer.

## Current scoped eye hue candidate

Implementation commit: `36e31998219c518e96730ff54e96b8e4fdf5b680`.

Protected `source/MainActivity.java` and the Native Chat / relay / voice / HA source remain byte-for-byte untouched. `scripts/materialize-android.sh` injects the eye-colour feature only into the materialized Android build:

- one `Eye colour` hue slider in the existing voice settings overlay;
- persisted hue via private SharedPreferences;
- one shared paint-level hue transform for the existing `boop_eyes` bitmap, so eye geometry, animation transforms, crops, hitboxes and artwork remain unchanged;
- default 190-degree cyan/blue uses no ColorFilter, preserving accepted default pixels exactly;
- existing caller-owned Wall -> Launcher animation materialization remains after the hue injection.

## Verification

Full GitHub Actions run `34090234634` reached and passed all installability and Native Chat checks before the known flaky polish assertion:

- protected Wall source gate: PASS;
- 169 Python/source tests: PASS;
- mocked Cloudflare relay tests: PASS;
- Java relay/chat/local-first/shake/gesture harnesses: PASS;
- materialization and 33 natural-wake mappings: PASS;
- Android unit tests: PASS;
- stable signer preparation: PASS;
- signed Wall v34 build: PASS;
- instrumentation APK build: PASS;
- package/version/archive/signer continuity: PASS;
- disposable Android 16 Pixel 7 Pro emulator boot: PASS;
- signed APK `adb install -r`: **Success**;
- real wake microphone armed: PASS;
- three-second chat menu, OpenCode/ChatGPT/Free Chat selection, persistence across process restart, revert, drag cancellation and pause cancellation: PASS.

The full run then failed only the pre-existing idle-blink midpoint instrumentation assertion: `Production blink did not close at midpoint: openness=0.37050718`. The branch already had prior evidence of this timing-sensitive assertion failing at a different sampled openness before the hue change. Do not alter blink behavior as part of the eye-colour task and do not call this full run green.

A follow-up `[boop-build-only]` run is used only to publish the exact current signed v34 APK after the above build/sign/install evidence. Build-only success must not be misrepresented as a full emulator/instrumentation pass.

## Preservation

The accepted Wall physical checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`. Do not move it until Ryan physically accepts a newer build.

Launcher remains separate as `com.boop.launcher`. Native Chat remains additive and local-first. No provider credential belongs in committed Android source.

## Next safe step

Install the current stable-signed v34 Native Chat + eye hue APK over the existing Wall. Verify the package installer accepts the update, then physically check: ChatGPT mode still appears/works as before, the eye-colour slider changes both eyes live, the selected hue survives force-stop/relaunch, default blue restores exact accepted rendering, and existing wake/sleep/thinking/shake/Member Berry/Wall -> Launcher behavior remains intact.
