# BOOP unified status

Updated 2026-09-08. Branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Signed v45 test candidate

Code commit `6dab12aa3232e821fed52b64e39f65e499b6c574`, version 45 / `1.1.2-unified-assist-repair`. Run `34198363929` completed successfully; artifact `BOOP-Unified` ID `10044846308`. Extracted APK SHA-256 `77fe8d06223bdaa6a07e232baeb2ddb9162845e98e022477be559fb377915a6b`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Non-visual verification passed: 57 Shield focused tests + 64 unified wake/routing/assistant tests with zero failures/errors/skips, Launcher lint, compilation, package identity, manifest integration, permanent signature and ZIP integrity. No visual, screenshot/golden, aesthetic source-string, emulator launch/install or physical-device acceptance ran.

Included repairs: canonical locked phone/Wall eyes and shared blink helpers on Shield; idempotent Shield density scaling; HA physical-device-only Home filtering with device-inherited room membership and fail-closed loose entities; authoritative room-switch teardown/rebuild; preserved coordinator-owned wake reload/re-arm and BOOP fallback; official Android assistant-role / VoiceInteractionService path for the Shield remote microphone button without a second microphone stack.

The Shield first-start assistant choice is reversible. BOOP never silently changes Android defaults or disables Google. A local key fallback is intentionally absent until hardware proves firmware sends a usable assist key without privileged/ADB hacks.

Physical acceptance is still required for remote-button activation, actual audio from the Shield remote microphone, clean one-shot recording/cancel/repeat/return behaviour, BOOP/custom acoustic wake, exact eyes/blink, repeated-open scale stability, room switching and device-only Home cards. Opening BOOP by itself is not remote-mic success.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic install/grants, signing/package identity change, Windows synchronization or unattended monitoring.
