# BOOP Music Lab status: separate signed APK ready, not installed

Updated 2026-09-13. Owner: `boop-music-lab-side-by-side-v161`. No merge into any other branch is authorized. Other operations continue independently.

**Built and artifact-verified:** BOOP Music Lab, `com.boop.musiclab`, `1 / 0.1.1-v161-audio-prompt`. Full v161-derived app plus conditional music permission entry, with separate application identity, storage and permissions. HOME/ASSIST/boot registration is excluded in the fork. Approved artwork and animation timing are retained. No physical-device acceptance is claimed.

Source `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`; signed GitHub run `34774532761`, SUCCESS; artifact `10322813560` / `BOOP-Music-Lab`. APK SHA256 `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`. Existing permanent signer verified, no replacement key. Downloaded APK and ZIP matched the published build receipt.

Tests: five fork isolation checks; four permission tests with 18 Java decisions; six existing animation timing functions; materialized speed/colour/master checks; 11 canonical-owner/notification-manifest checks; signed assembly and packaged identity/signature/integrity checks all passed. This is focused non-visual build verification, not an assertion that the complete historical test suite or device UI was exercised.

**Not done:** installation, device launch, OS permission grant, emulator use, microphone capture, Visualizer sampling or VU bounce. Nothing was merged or installed over working Unified. No user settings, data, artwork or laptop sources were modified.

Read `SESSION_HANDOFF.md` and `docs/handoffs/2026-09-13-music-lab-fork.md` for exact provenance, hashes and next boundaries. Permission entry: Launcher Settings > Now Playing > Music audio access. Actual bounce remains the agreed future design: music determines height; animation speed independently controls blinks. Only proceed to implementation/integration/installation within Ryan's next explicit request.
