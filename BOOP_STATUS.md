# BOOP task status: conditional music audio permission prompt

Updated 2026-09-13. Task branch: `boop-unified-music-audio-prompt`. Base: `boop-unified-eye-sync-safe-v159@593ad609ff87f651d5273bd17f5a2c0ca3ef5198`.

The music-only permission entry is implemented in source at `c1c53e84f20e2211b729dc2f5a541018fc2908b8`: Launcher Settings > Now Playing > Music audio access. It checks the live grant, asks only when missing, preserves an in-flight request and leaves denial/cancellation optional. Existing uses-permission declarations are unchanged. No voice callback, audio capture, animation, artwork or playback code changed.

Verification source/CI configuration: `746a7d3ab0c8601c174ca690c2b27397266e673e`. Exact test/compile outcomes are recorded in `docs/handoffs/2026-09-13-music-audio-permission.md`. Android device prompt behaviour is NOT user-accepted or assistant-tested here. No emulator/hosted visual test was used.

NOT merged into the accepted owner branch. NOT installed. No APK produced or signed by this task; version/signing configuration unchanged. The historical accepted v161 APK/build identity and both-device speed/two-way-colour acceptance remain in SESSION_HANDOFF.md and the preserved acceptance receipt. No claim of a new physical checkpoint.

The desired bounce remains design-only: actual music level determines bounce; the existing animation-speed control still determines blinks. No BPM/beat tracking, microphone fallback or visualizer sampling added. Deezer/Shield audio-data availability remains to be tested jointly after authorized implementation.

Source, tests and handoff publishing stay on GitHub. Preserve all concurrent work and fetch LIVE Unified before integration. Leave physical Pixel 10, device permissions, artwork, user settings and signing alone.
