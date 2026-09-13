# Unified v164: installed on Shield, awaiting Ryan's music test

Updated 2026-09-13. Task owner: `boop-unified-v164-music-bounce`.

## Latest request and result

Ryan explicitly requested: "install it please, the shield is free". Installed the exact already-built v164 APK on the identified Shield ONLY through the existing Desktop Commander/ADB connection. Ordinary `adb install -r` returned Success, exit 0. Separate readback completed with exit 0 and verified the installed package/version/hash, unchanged HOME resolution, unchanged UID and first-install timestamp, and unchanged audio/media access. No data clear, uninstall, permission grant, settings reset, signing change, phone/emulator operation, playback input or explicit app-launch command was issued.

Preflight actually found `163 / 1.2.163-music-audio-access` installed, despite the earlier user-reported rollback. Do not label this physical update as 162-to-164 or invent a cause for the discrepancy. The new APK's SOURCE is still directly based on accepted v162, not v163. This continuation changed no app source and performed no rebuild or merge.

RECORD_AUDIO was already granted before installation and remained granted afterwards. Unified's ShieldNowPlayingListenerService media access was enabled before and after. No new grant was made. Installation identity is confirmed; usable Deezer levels, visible bounce and v164 runtime lyrics behavior are not yet accepted by Ryan.

## Exact installed candidate

Package/version: `com.boop.alpha1`, `164 / 1.2.164-music-bounce`.
Source: `f9f65569250b9dc02602101ef4d56195824e0380`.
Signed run: `34778178916`; artifact `10323694771`, `BOOP-Unified-v164-Music-Bounce`.
APK: `BOOP-Unified-v164-Music-Bounce.apk`, 155290450 bytes.
APK SHA256: `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The exact signed artifact was downloaded to a dedicated laptop Downloads directory. Staged hash, source receipt, aapt package/version, actual apksigner verification and the native ShieldLyricsActivity registration were checked before installing. Installed base.apk hash then matched that verified artifact. Private staging/readback files stay on the laptop, outside app source; no local checkout synchronization is claimed.

## Implemented behavior and protected baseline

v164 starts at accepted `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`, not the Music Lab/v163 tree. It remains an unmerged test branch. Do not advance an accepted owner or merge it without Ryan's request.

Android Visualizer output-mix waveform data feeds DC-rejected loudness, bounded fast-rise/soft-fall smoothing and a whole-puppet GL viewport offset in Now Playing. Sampling is gated by visible/playing/permitted state and released when inactive. Missing/stale/constant samples cannot fabricate a bounce. Existing canonical animation speed still controls blinks/expressions independently. No physical-microphone fallback, recorder, BPM inference, audio-focus/volume change or playback command is added by the bounce path.

Conditional permission Activity/flow is retained. Missing permission gets a once-off foreground explanation, not an Activity launched from the floating service. Ryan operates Continue/Not now and Android's choice. Manual entry: Launcher Settings > Now Playing > Music audio access. The installed Shield already had access at this readback.

v162's native Lyrics-button callback, internal lyrics screen, Skip observer and half-size passive licence footer remain unchanged in source. Media manager, launcher routing, startup/voice code, dependencies, original materializers, artwork/shaders and shared animation engine were preserved. Modified existing inputs were limited to version fields, one private permission Activity line, six settings lines and the puppet adapter. The installation did not modify that build.

## Evidence and next step

Build receipt: `docs/handoffs/2026-09-13-v164-music-bounce.md`. Installation receipt: `docs/handoffs/2026-09-13-v164-shield-install.md`.

Prior focused nonvisual tests passed: four v164 groups including 332 envelope assertions; native lyrics data/transport/entry/lifecycle checks; six timing functions; materialized source/art checks; 10 owner/bay contracts; signed assembly and actual APK identity/signature/packaged-class checks. Two inherited v162-only freezes skip future versions; the new v164 preservation gate passed. No full historical-suite rerun, independent reviewer or hosted visual acceptance is claimed. Tests were not rerun for this installation/documentation-only continuation.

Next: Ryan tests music and the preserved lyrics in installed v164. Follow his actual observations; do not claim audio/visual success from install checks. v162's accepted Lyrics/Skip and v161 speed/colour results remain separate inherited acceptance, not v164 runtime evidence. Exact pre-installation root context remains at `677009e53c52df3ec6c92ea75214df5e7c4dba0a`; v162 acceptance is preserved in its original branch and dated handoff.

Source, builds, permanent signing and handoffs stay on GitHub; device testing is joint. Keep Desktop Commander 0.2.47 unchanged. Leave physical Pixel 10 and other tasks alone. Fetch LIVE task HEAD before further writes, preserve concurrent work, and publish scoped documentation with live-head verification. This continuation updates only task documentation; accepted branches/main/labs are not changed.
