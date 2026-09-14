# v170 Johnny audio routing repair

Updated 2026-09-14. Owner: `boop-dance-levels-v167`.

## Reproduced cause and limits

BOOP classified the silent Johnny package as a normal video launch. A live Johnny tile launch changed native sample-rate mode from 1 to 0 even though Johnny had no active audio player. Deezer's existing DIRECT 44.1 kHz track survived that flag change; restarting Deezer outside BOOP then created a MIXER 48 kHz track. BOOP logged original fast output-mix bounce active. Ryan confirmed the original response returned, but observed slight latency. The earlier pre-reboot successful route was not recorded, so this reproduces a mechanism rather than proving the precise historical reboot sequence.

Normal native mode was restored by launching Deezer through BOOP and recreating its stream. DIRECT 44.1 kHz returned. Starting the selected Johnny DreamService with the system Start now control preserved that same output, with Deezer the only active audio player. Johnny's emulator-ready log and bound DreamService were verified; exiting returned normally. No screensaver settings were changed.

## Bounded fix

`AudioModePolicy.forLaunch("local.johnnycastaway.shield")` now returns IGNORE. The launcher sends no audio-mode write for that tile. Native Deezer, video apps and Chromecast classification remain unchanged. Johnny's own app is unchanged: no active playback hookup was found in the inspected v6 source and none appeared during the live screensaver test.

This protects audio routing; it does not enable Android Visualizer on DIRECT 44.1 kHz. v169's synthetic fallback remains when that route blocks capture. Do not call the original dance restored at native rate or latency solved.

## Verified build and installation

Red run34867929509 failed the exact silent-Johnny assertion. Green run34868133982 passed routing, original-worker/curve, voice, natural-voice, lyrics, renderer and independent-animation checks. Two inherited one-time lyrics freeze checks remain explicitly skipped. The preservation gate permits only the exact Johnny exclusion beyond previous approved changes. Read-only source review found no blocker.

- Signed source: `82ae808f69686aea788e300430aee412293764ae`.
- Artifact: `10358455609`, `BOOP-Unified-v170-Johnny-Audio-Neutral`.
- Package: `com.boop.alpha1`, code170, `1.2.170-johnny-audio-neutral`.
- APK SHA256: `4874e00956ffdae05681eef9c4795a71f511d3dcf3c2971eb9714f0a6158bc60`.
- Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Shield installation succeeded. Installed APK hash matches the artifact; all16 saved preference hashes are unchanged. After installation, opening Johnny from the BOOP tile reached Johnny MainActivity, preserved the same DIRECT44.1 output and produced no native-mode0 write. AudioFlinger history still ended at restored native mode1. Android animator scale remains0. The earlier actual system DreamService start/stop also preserved the same music output. These are device routing checks, not Ryan's final acoustic/visual acceptance.

Phone remains accepted v166. Johnny APK was not changed. No local app source builds/tests, emulator, hosted visual tests, permissions or system configuration changes. Raw diagnostics remain private. Original bounce on MIXER48 was user-confirmed with slight latency; native44.1 original capture remains unresolved.
