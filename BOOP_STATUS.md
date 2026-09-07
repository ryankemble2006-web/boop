# BOOP Wall v35: Native Chat + eye hue candidate

Updated 2026-09-07. Owning branch `boop-wall-native-chat-eye-hue`.

## Current candidate

- VersionCode 35 / `0.4.15-wall-native-chat-eye-hue`, package `com.boop.alpha1`.
- Built from the user's installed v34 Native Chat lineage, not the older v31 hue experiment.
- Native Chat / OpenAI relay, browser/free-chat mode, wake path, existing caller-owned Wall -> Launcher transition and current idle-blink materialization remain present.
- Adds exactly one persisted hue-only `Eye colour` slider beside voice settings.
- Default accepted cyan/blue is 190 degrees and uses no ColorFilter at all.
- Reuses the exact existing `boop_eyes` bitmap/render Paint for both eyes; no artwork, crop, geometry, black background, mouth or extra visual setting was added.
- Existing permanent BOOP signing identity unchanged.

## Verification evidence

GitHub Actions run `34090520672` completed SUCCESS for build commit `1256fb33f198659d7afd1310e5c8afbadd5d53d3`.

Passed gates include focused source guards, hue math/default-path checks, representative orange/green/pink/purple/cyan hues, Chat mode harness, Member Berry guard, thinking guard, shake detector, shake-eye motion, Android unit tests, effective Native Chat/OpenAI relay markers, all 33 wake mappings, signed v35 build, exact package/version inspection, archive integrity and stable signer continuity.

Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v35` / ID `10006695690`.
Extracted APK SHA-256: `013c4db3b3fc9e21eb2b4bf0a255bfbaf84d9b2c94a06c7a996cc875ff917819`.

## Concurrency reconciliation

While this candidate was being built, `boop-wall-free-chat-wip` advanced from `e10df1cc27d5522fa33fe6722d0d70f817f16289` to `36e31998219c518e96730ff54e96b8e4fdf5b680` with a concurrent implementation of the same hue concept inside materialization. Preserve that work. The combined candidate records it in ancestry and selects the isolated/testable helper + patch implementation rather than blindly stacking duplicate hue code.

## Physical status

Physical Pixel acceptance is pending. Expected install path: in-place upgrade from the user's current v34 `0.4.14-wall-native-chat` to v35 using the same package and signer.

Physical acceptance checklist:
- v35 installs over v34 without uninstall/data loss;
- default cyan/blue remains visually unchanged;
- orange, pink and green preview live on both eyes;
- selected hue survives force-stop/relaunch and device restart;
- wake/sleep, thinking, shake, Member Berry, tap/hold and Launcher swipe still behave normally;
- Native Chat/ChatGPT conversation still works.

## Accepted Wall baseline remains unchanged

Physical accepted Wall checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`. Do not promote or overwrite it until physical acceptance.
