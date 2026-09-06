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

Swipe candidate: branch `boop-wall-launcher-swipe` is based on the live Wall
branch head `f5a86db`; source commit `9587e6c` is published to GitHub. It adds
a deliberate single-finger left swipe from Wall
to the separately installed `com.boop.launcher`: at least 96dp leftward with
1.5x horizontal confidence. Move, cancellation, and multi-touch cannot trigger
it; movement cancels the hidden MemberBerry hold; the swipe is consumed before
tap-to-speak. Missing Launcher shows `BOOP Launcher is not installed.`

Candidate verification: 125 Python source checks pass except the pre-existing
Windows-only Git-Bash PATH issue in the wake-workflow simulation; 26 Android
JVM test reports passed; the focused Java gesture harness passed. Local Android
compilation completed. Local APK assembly is intentionally blocked because the
stable `boopDev` signing key is GitHub-only. The frozen resurrection workflow
also rejects arbitrary Wall source changes by design, so no signed CI APK or
emulator install has been claimed. Preserve the physical checkpoint and do not
call this candidate physically verified.
