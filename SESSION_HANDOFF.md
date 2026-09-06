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

Permanent swipe promotion: `boop-wall-launcher-permanent` merges the reviewed
candidate into the Wall line. Version 30 / `0.4.10-wall-launcher-swipe` uses the
existing stable signer and normal Wall workflow; its new source baseline is
locked by that workflow. It adds a deliberate single-finger left swipe from Wall
to the separately installed `com.boop.launcher`: at least 96dp leftward with
1.5x horizontal confidence. Move, cancellation, and multi-touch cannot trigger
it; movement cancels the hidden MemberBerry hold; the swipe is consumed before
tap-to-speak. Missing Launcher shows `BOOP Launcher is not installed.`

Verification: signed v30 candidate APK from GitHub Actions installed in-place
on the local Pixel 7 Pro emulator. Left swipe opened `com.boop.launcher`; right
and vertical swipes stayed in Wall. This is emulator evidence only, not a new
physical checkpoint. Linux CI source/JVM/signing checks passed for the candidate;
the pre-existing Windows-only Git-Bash PATH issue remains in one local workflow
simulation. Preserve the physical checkpoint until a device acceptance pass.
