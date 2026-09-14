# Voice startup repair v166

Updated 2026-09-14. Owner: `boop-voice-ack-v166`.

Based on exact signed v164 source `f9f65569250b9dc02602101ef4d56195824e0380`, matching the installed Shield APK. Preserve the separate uninstalled v165 bounce adjustment. User authorized this reply repair and testing on Shield, then explicitly substituted Pixel 10 Pro XL for Pixel 7. No installation claimed yet.

## Problem and repair

Remote commands can succeed while the reply arrives before Android TTS finishes initialization. The previous reply dispatcher treats not-ready as utterance completion, closing the one-shot activity. The Android backend now retains only the current reply until initialization, bounded by a 10-second failure timeout. Readiness flushes it once. Cancellation, replacement and destruction invalidate pending work and old playback callbacks. Successful playback remains the normal completion boundary. Natural voice synthesis, voice choice, house-command routing, artwork and music behavior are preserved.

## Evidence

Initial CI run 34860892568 stopped before tests on an upstream voice-model download HTTP 504. Independent source-path RED at `4a204bda6210aa004d2c55fc07bac38490e553af`, run 34861028390, failed five lifecycle scenarios, including the exact delayed-engine lost acknowledgement. Warm speech passed. The regression compiles actual reply/onInit method bodies with the real Android backend and a controlled platform TTS double. It also runs on fully materialized app code before packaging.

GREEN, signed artifact, install and physical spoken acknowledgement results are pending. Existing debug logs include stage labels only, never transcript or house credentials. Keep source and builds on GitHub. Retain pre-install APKs privately and verify update identity/settings. No permission grants, data clears, HOME/assistant role changes or unrelated merge are authorized.
