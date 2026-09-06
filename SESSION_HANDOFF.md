# BOOP Wall handoff — 2026-09-06

Owner: Wall application; separate from com.boop.launcher and Shield.
Working physical code checkpoint:595e1da, version29/0.4.9-alpha6.5.6-wall,
package com.boop.alpha1. Existing signed build33992704568 and physical Pixel7Pro
acceptance are recorded in BOOP_STATUS.md. Do not replace that checkpoint.

Committed619ba5d was three documentation commits ahead of GitHub a29117b:
routine-authoring design, safety-boundary refinement and plan only. Source was
not behind merely because commit IDs differed. This sync publishes the docs.

The separate laptop task's uncommitted five-file left-swipe draft has been
preserved on boop-wall-launcher-handoff-wip. It is not included in this branch's
committed app baseline. Its original working files are deliberately retained
locally; do not discard them or publish duplicate competing implementations.
Fresh snapshot verification:123 Python source checks PASS; four-case Java gesture
harness PASS. No verified signed swipe APK or physical test. Full touch/hold/
cancellation/multitouch coexistence still needs review. Do not call it finished.

Next: Android develops Launcher from boop-launcher-alpha1. Coordinate the
com.boop.alpha1 -> com.boop.launcher contract using the WIP reference before
changing Wall. Preserve taps, voice, 33 wake phrases, local control and pairing.
