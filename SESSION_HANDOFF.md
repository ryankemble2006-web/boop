# Music Lab continuation: permission feature moved into Unified v163

Updated 2026-09-13. Ryan reported that Now Playing would not load in this separate app and explicitly requested rolling the work into Unified v162 and bumping to163. That request supersedes the earlier no-merge instruction for the implemented permission feature only.

Continue current delivery on `boop-unified-eye-sync-safe-v159`, reading its LIVE SESSION_HANDOFF.md, BOOP_STATUS.md and BOOP_UNIFIED_MEMORY.md. Integration source `cac499bf9e6ab325faa2d843112f90ace71118b1` on `boop-unified-music-v163` starts from accepted v162 `112d09b5b446d6582954a6d89b3700fe16298ecb`, not from this older lab tree. Only the two permission classes/settings entry were donated; the newer manifest retains all v162 lyrics/media services. Unified package stays com.boop.alpha1, version163 /1.2.163-music-audio-access. Source integration checks passed; the full signed owner build is running at this checkpoint. Read its live receipt for the later artifact/install outcome.

A read-only Shield check established this lab's ShieldNowPlayingListenerService access OFF and Unified's ON. No permission was changed, notification content inspected or device input sent. Missing lab media access is a concrete finding, not an exhaustive runtime diagnosis. Do not silently grant it or replace Unified with this package.

This lab's app source and installed checkpoint remain unchanged: com.boop.musiclab, 1 /0.1.1-v161-audio-prompt, build source f613c5033e54b55bdbfe0087b0e253fd75efa7e9, run34774532761, artifact10322813560, APK9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb. It was successfully installed side by side after the earlier tool-blocked attempt. Installation was hash/version verified; it was not claimed runtime accepted. No lab uninstall was requested or performed during this integration.

Full pre-integration lab handoff/status/memory are preserved at `7b596a3be376b30f06676997d70b7b50bf8494c0`; signed build provenance remains in docs/handoffs/2026-09-13-music-lab-fork.md. The new branch update is documentation only. Do not copy lab packaging or old v161 code into newer Unified, alter another operation, operate phones/emulators or claim laptop checkout synchronization.

Actual Visualizer sampling/music-driven bounce remains UNIMPLEMENTED. The agreed design is still music controlling bounce independently of blink speed, without physical microphone fallback. The moved feature is the conditional permission prompt only. Source/builds/signing stay on GitHub; runtime testing is joint with Ryan.
