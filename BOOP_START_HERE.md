# BOOP Music Lab: current task routing

Updated 2026-09-13. This branch owns the separate **BOOP Music Lab** fork, not production Unified.

Owner: `boop-music-lab-side-by-side-v161`.
App: `com.boop.musiclab`, `1 / 0.1.1-v161-audio-prompt`.
Parent: `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`, based on accepted Unified v161.

Ryan explicitly said **do not merge** because other operations are continuing. This branch produces an independently installable app. Do not write to Unified, Lyrics Lab or main for ordinary Music Lab work, do not install over com.boop.alpha1, and do not treat this older base as the newest combined source.

Fetch LIVE task branch HEAD, then read `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` and `docs/handoffs/2026-09-13-music-lab-fork.md`. Current shared workflow is main's BOOP_START_HERE.md, last read at `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80`; older inherited root maps are historical.

Source edits, non-visual tests, builds, stable signing and handoffs stay on GitHub. Use the dedicated `.github/workflows/build-boop-music-lab.yml`. Do not edit/build app source on the laptop, operate emulators autonomously or run hosted visual tests. Devices are tested together with Ryan only as requested. Physical Pixel10 is excluded. Never silently grant permissions, clear data, replace the signer or change HOME/assistant choices.

The fork APK is built and artifact-verified, not installed or physically accepted. It contains the conditional music audio permission prompt; actual Visualizer-driven bounce is still unimplemented. Preserve artwork and independent blink-speed behavior. Publish scoped handoff/status/memory results and verify LIVE task branch after commits. Integration is deferred until Ryan explicitly requests it.
