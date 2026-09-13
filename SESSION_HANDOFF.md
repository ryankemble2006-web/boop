# WIP: shared eye colour, then animation speed

Updated 2026-09-13. Owner: `boop-unified-eye-sync-safe-v159` on GitHub.
Accepted device source remains v156 `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. Shield sweep is complete and accepted. Its corrected handoff is on `boop-unified-artist-link-v156@086ae4d383c5c3f176baafe803d01628e30251a9`.

Both interrupted drafts are preserved on GitHub: `wip/boop-colour-v158-recovery@484d292f` and `wip/boop-colour-v159-recovery@7ea7d26d`. Original local worktrees/indexes were left unchanged during their publication. The feature branch starts from v159 recovery.

Current limits: `BoopSharedEyeColourRuntime.java` was truncated in the earlier session. App initialization and settings activity are missing. This snapshot is NOT buildable or deployable. Recovered protocol tests do not prove a finished runtime. The new appearance-logic GitHub workflow establishes a fresh red baseline before completion.

Research and exact preservation boundaries are in docs/superpowers/plans/2026-09-13-shared-colour-and-speed.md. Primary Android/HA docs and source were checked again this session. Reuse the existing local hue store and authenticated HA client. No unauthenticated UDP or old voice/blink modifications. Sharing is opt-in; app background/offline retains local hue and resynchronizes on foreground reconnect. 1x and all authored artwork/motion stay unchanged.

Next: complete tested colour runtime and settings, pass GitHub logic/build/signing, then emulator runtime checks. Only then move to animation speed and device acceptance. Pixel 10 is excluded; no device installs occurred during recovery. Native lyrics stays separate.
