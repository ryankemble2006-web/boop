# BOOP Wall Native Chat + eye hue handoff — 2026-09-07

Owner: isolated combined Wall candidate on `boop-wall-native-chat-eye-hue`.
Package: `com.boop.alpha1`.
Candidate version: versionCode 35 / `0.4.15-wall-native-chat-eye-hue`.

## Why this branch exists

The user is physically running BOOP Wall `0.4.14-wall-native-chat` / versionCode 34. An earlier eye-hue APK was incorrectly built from the older v31 resurrection lineage and Android rejected it as an app downgrade. Do not reuse that v31 artifact for this device.

This branch starts from the exact v34 Native Chat / Free Chat Wall head `e10df1cc27d5522fa33fe6722d0d70f817f16289`, then adds the eye hue control while preserving the current ChatGPT/Native Chat relay, browser/free-chat mode, idle blink work and caller-owned Wall -> Launcher transition. The protected physical Wall checkpoint remains unchanged.

A concurrent session added the same hue concept directly into `boop-wall-free-chat-wip` at `36e31998219c518e96730ff54e96b8e4fdf5b680`. That work was not discarded or blindly merged. This branch records it as a second parent via reconciliation commit `59aba586367b1e567698d22fde6282d93abb4708`; the selected tree keeps the hue implementation isolated into source helpers plus a materialization patch for clearer testing and later reconciliation.

## Eye hue implementation

- Exactly one `Eye colour` SeekBar is inserted beside the existing voice settings.
- Range is 0..359 hue degrees only.
- Existing default cyan/blue is anchored at 190 degrees and deliberately returns a null ColorFilter, so the default render uses the exact existing bitmap/paint path.
- Non-default values apply a hue-rotation ColorMatrix to the existing shared eye Paint.
- Both eyes, portrait render and shake render already share that Paint, so they remain consistent without changing eye artwork, crop, geometry or animation code.
- Hue persists in SharedPreferences `boop_eyes` / `hue_degrees` and is applied when the face view is materialized.
- No mouth, replacement eye artwork, background change, brightness, saturation, opacity, theme or effects control was added.

## Verification

Signed build commit: `1256fb33f198659d7afd1310e5c8afbadd5d53d3`.
GitHub Actions run: `34090520672` — SUCCESS.
Artifact: `BOOP-Wall-Native-Chat-Eye-Hue-v35`.
Artifact ID: `10006695690`.
Extracted APK SHA-256: `013c4db3b3fc9e21eb2b4bf0a255bfbaf84d9b2c94a06c7a996cc875ff917819`.

The successful gate covered:
- Native Chat marker and OpenAI relay marker still present in effective MainActivity;
- full 33 natural-wake mappings retained;
- exact existing `boop_eyes` bitmap and black face background retained;
- no mouth path introduced;
- hue math for default/cyan/orange/green/pink/purple;
- default 190-degree hue is an unfiltered path;
- existing Chat mode harness;
- Member Berry source guard;
- thinking puppet source guard;
- shake detector and shake-eye motion harnesses;
- Android unit tests;
- stable signed APK build with the current Native Chat relay configuration environment;
- package `com.boop.alpha1`, versionCode 35, versionName `0.4.15-wall-native-chat-eye-hue`;
- signer fingerprint continuity against the existing permanent BOOP signer;
- APK archive integrity.

## Physical status

NOT yet physically accepted on the Wall Pixel. The expected installation path is an in-place upgrade from v34 to v35 with the same package and signer. After installation, physically verify default blue, live orange/pink/green changes, persistence after force-stop/relaunch or device restart, wake/sleep, thinking, shake, Member Berry, tap/hold and Launcher swipe, plus Native Chat conversation.

Do not move or overwrite the protected physical Wall checkpoint merely because CI is green.
