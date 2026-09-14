# Temporary playback-capture feasibility probe

Separate package local.boop.captureprobe, not a BOOP production update. Android11 Shield target; targetSdk30 matches the diagnostic platform scope.

Manual Start requires RECORD_AUDIO permission and fresh MediaProjection consent. Only installed Deezer UID plus MEDIA usage is selected. Reads stereo44100 PCM into memory in 10ms requested blocks. Reports aggregate nonzero/RMS/peak and blocking-read duration once a second; saves no PCM and creates no video display. Automatically stops after60seconds; Stop button and projection revocation also stop capture.

Run with Deezer already on native DIRECT44.1. Compare output route before/during/after. If capture forces a MIXER/resampled output or yields silence, do not integrate it as a native-rate solution. Blocking-read timings are not end-to-end music/visual latency.

Throwaway experiment authorized by Ryan; no production dance or routing changes. Compile and artifact checks run on GitHub. Source-boundary checks are not physical feasibility acceptance. Remove the temporary app after investigation.
