# v119 Shield audio-mode switching — permanent receipt

Date: 2026-09-11
Canonical branch: `boop-canonical-rebuild`
Canonical code head before this receipt: `6bfc2e63eaf80395503afca43e80983efa5066d0`
Version: `119` / `1.2.119-audio-mode-switch`
GitHub Actions run: `34550625948` — completed successfully
APK SHA-256: `4431c7f1835b038ceb02317f61cd56c583b7956093699bd889a7ae6328efe2b0`
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

## Physical acceptance

Ryan physically tested v119 on the Nvidia Shield and reported the automatic audio handoff working.

- Native Deezer launch uses NVIDIA native sample-rate matching.
- Google Cast Deezer is detected through `com.google.android.apps.mediashell` when the active session reports `CONTENT_TYPE_MUSIC` and is playing, and enables native sample-rate matching.
- Launching other Shield apps through BOOP defaults back to normal/video mode, covering Kodi/Forki/iPlayer/video paths without a per-app video allowlist.
- Paused Cast sessions do not reclaim the audio mode from an active foreground app.
- BOOP reads the live NVIDIA parameter before writing, so Ryan's manual changes to the Shield toggle remain respected rather than being masked by an in-memory cache.

## NVIDIA control discovered

Exact live parameter:

`nv_param_audio_native_sample_rate_select`

NVIDIA's own TV Settings implementation calls:

`AudioManager.setParameters("nv_param_audio_native_sample_rate_select=1")`

for native sample-rate matching on, and `...=0` for off.

A normal Android app with `android.permission.MODIFY_AUDIO_SETTINGS` can use this path on the Shield. Root, system signing, runtime ADB, PID hardcoding and UI automation are not required.

## Scope

This permanently adopts the verified audio-mode behavior into `boop-canonical-rebuild`. Forki Dev's intentional no-audio visual-development behavior remains separate and unchanged. Existing Startup Manager work from v118 is retained.
