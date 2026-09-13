# Music audio permission prompt: test-first work in progress

Scope approved by Ryan: add a prompt when audio access is missing. Earlier read-only discussion remains the boundary for the actual VU bounce; do not implement the visualizer engine or change animation timing in this task.

Base: live `boop-unified-eye-sync-safe-v159` at `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`, accepted v161. Isolated owning branch: `boop-unified-music-audio-prompt`. Do not overwrite the parallel lyrics integration.

The current app already declares RECORD_AUDIO in the Wall manifest and MODIFY_AUDIO_SETTINGS in the Shield Home manifest. The new entry belongs in the current Unified Shield Home Now Playing settings, not historical shield-overlay settings or the voice activity.

Design: check the real grant; return immediately when granted; otherwise explain and request Android's permission only after Continue. Not now/back cancels. A denial must not loop. Offer app permission settings only as a deliberate user choice. Preserve pending requests across activity recreation. No microphone, recording, visualizer, speech, service start, installation, device operation or permission grant is performed by this task.

This checkpoint adds tests only; implementation and verification are pending. Existing installed v161 acceptance is preserved in the base branch handoff/status/memory. Blinks remain controlled by animation speed; future music loudness will control bounce separately. No BPM lookup or beat tracking.
